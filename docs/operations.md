# FixLedger 生产运维说明

## 1. 默认安全边界

- 后端容器内只暴露 `/actuator/health`、`/actuator/info`，生产 Gateway 不代理 Actuator。
- `metrics`、`prometheus` 在生产 Profile 中关闭，避免普通公网入口泄露运行信息。
- 需要采集指标时应增加独立监控网络和受保护的生产监控 Profile，不通过公网 Gateway 临时开放。
- 不建议在生产环境长期打开 MyBatis SQL DEBUG 日志。

## 2. 关键指标

| 指标 | 含义 | 主要标签 |
| --- | --- | --- |
| `http.server.requests` | Spring MVC 请求量和延迟 | method、uri、status、outcome |
| `fixledger.dashboard.summary.cache.requests` | 首页摘要缓存结果 | result=hit/miss/invalid/disabled |
| `fixledger.dashboard.summary.load` | 首页摘要数据库回源耗时 | 无业务 ID 标签 |
| `fixledger.export.requests` | 同步导出结果 | type、result |
| `fixledger.export.duration` | 同步导出生成耗时 | type |

禁止把 `familyId`、`userId`、`deviceId`、文件名、异常正文作为指标标签。

## 3. 首页缓存排障

1. 查看缓存命中率，持续低命中时检查是否存在高频写入或 TTL 配置过短。
2. 查看 `fixledger.dashboard.summary.load` 与 `http.server.requests` 的 P95/P99。
3. Redis 不可用时首页会回源 MySQL；同时检查 Redis 连接日志与 MySQL连接池负载。
4. 缓存值反序列化失败时会删除该 Key 并回源，不需要人工清理全部缓存。

缓存 Key：`fixledger:dashboard:summary:{familyId}`，默认 TTL 为 2 分钟。

## 4. 慢查询复核

优先检查以下访问路径：

| 场景 | 主要条件 | 索引 |
| --- | --- | --- |
| 设备默认列表 | family_id + updated_at | idx_fl_device_asset_list |
| 保修临期统计 | family_id + end_date | idx_fl_warranty_record_end_date |
| 耗材状态统计 | family_id + status | idx_fl_consumable_item_status |
| 维修状态统计 | family_id + status | idx_fl_maintenance_status |
| 维修费用范围 | family_id + completed_at | idx_fl_maintenance_completed_at |
| 凭证业务聚合 | family_id + biz_type + biz_id | idx_fl_file_resource_biz |

出现慢查询时使用 MySQL `EXPLAIN ANALYZE` 检查扫描行数和索引选择，再决定是否加索引。
不要只根据 SQL 文本长度或主观判断增加重复索引。

## 5. 导出容量边界

- 同步设备清单和维修费用 CSV 默认最多 5000 行。
- 服务读取 `maxSyncRows + 1` 条用于判断超限，不会静默截断第 5001 行之后的数据。
- 超限时先缩小维修日期范围；设备清单持续超限才评估异步导出。

异步化触发条件：真实环境持续出现同步超限，或导出 P95 超过 3 秒。届时再增加
`fl_export_record`、后台 Worker、对象存储文件、过期清理和失败重试，不在当前家庭规模下提前引入。

## 6. 数据库迁移

本地演示和 H2 测试继续使用 `schema.sql`；生产 Profile 关闭 SQL 初始化，只执行
`backend/src/main/resources/db/migration/` 下的 Flyway 迁移。

- 空数据库：Flyway 依次执行 V1 完整基线与后续版本。
- 已有非空数据库：首次启动在版本 0 建立 baseline；V1 跳过已存在表，V2 使用
  `information_schema` 与动态 SQL 幂等补齐通知列和查询索引。
- 每个已发布迁移文件不可修改；后续表结构变化必须新增 `V3__...sql` 等版本。
- 迁移前必须执行同批次 MySQL/RustFS 备份，禁止通过删除 Docker 数据卷升级。

查看迁移状态：

```sql
SELECT installed_rank, version, description, success, installed_on
FROM flyway_schema_history
ORDER BY installed_rank;
```

若迁移失败，先保持应用停止并检查失败 SQL；不要手工修改 `flyway_schema_history`。修复方式应是恢复备份
或在确认数据库状态后新增修复迁移。

## 7. 前端生产包体

- Element Plus 只安装当前使用的组件，ECharts 只注册饼图、折线图及必要组件。
- Vite 按 Vue、ECharts 和通用依赖拆分稳定 vendor，业务页面继续使用路由懒加载。
- P29 基线中入口和首页 JS 分块分别为 `1074.33 KiB`、`1025.64 KiB`；优化后入口业务块为
  `442.80 KiB`，首页页面块与图表 vendor 合计 `484.67 KiB`，构建不再出现大分块警告。
- 后续升级 Element Plus、ECharts 或 Vite 时需要重新执行生产构建，不能只调高警告阈值。

## 8. 生产环境准备

生产文件职责：

| 文件 | 作用 | 是否入库 |
| --- | --- | --- |
| `.env.production.example` | 变量名和格式模板 | 是 |
| `.env.production` | 真实域名、镜像和凭据 | 否 |
| `docker-compose.prod.yml` | 独立生产拓扑 | 是 |
| `deploy/certs/fullchain.pem` | TLS 证书链 | 否 |
| `deploy/certs/privkey.pem` | TLS 私钥 | 否 |
| `backups/<批次>/` | 数据库、附件和校验清单 | 否 |

真实环境至少使用 32 字符随机 JWT，数据库、Redis 与对象存储使用不同的高强度密码。应用镜像、
MySQL、Redis、RustFS、Nginx 和备份工具镜像都必须使用固定版本或摘要，禁止 `latest`。

部署前检查：

```powershell
./scripts/check-production-readiness.ps1 `
  -Strict -ProductionEnvFile .env.production -ValidateSecrets
```

检查会解析最终 Compose JSON，确认只有 Gateway 发布 80/443、数据网络为 internal、后端使用
`prod` Profile、镜像不使用 `latest`，并检查 Flyway、Swagger、HTTPS、安全头、脚本、真实凭据和证书。

## 9. 备份与恢复

创建备份：

```powershell
./scripts/backup-production.ps1 -EnvFile .env.production
```

备份过程会进入短暂维护窗口，停止 Gateway、后端和 RustFS，保存 MySQL 逻辑转储和 RustFS 数据卷，
然后恢复服务。每个批次包含：

- `mysql.sql`
- `rustfs-data.tar.gz`
- `manifest.json`
- `SHA256SUMS`

恢复会删除当前数据库和附件卷内容，必须先在隔离环境验证备份，并显式确认：

```powershell
./scripts/restore-production.ps1 `
  -EnvFile .env.production `
  -BackupName 20260724-170000 `
  -ConfirmRestore
```

恢复脚本先校验 SHA-256，再停止应用、重建 MySQL 数据库、恢复 RustFS、清空 Redis 业务库并重启。
Redis 不保存业务事实，不进行备份。

## 10. 发布与回滚

发布前把当前 `.env.production` 复制到主机受限目录作为上一版本清单，再修改应用镜像标签。标准发布：

```powershell
./scripts/deploy-production.ps1 -EnvFile .env.production
```

脚本依次执行真实环境 readiness、发布前备份、镜像拉取、Compose 更新和生产健康检查。只有已经通过
独立备份时才可使用 `-SkipBackup`。

应用故障优先只回退镜像，不恢复数据：

```powershell
./scripts/rollback-production.ps1 `
  -PreviousEnvFile releases/previous.env.production
```

只有确认迁移或应用造成数据损坏时，才同时恢复指定批次：

```powershell
./scripts/rollback-production.ps1 `
  -PreviousEnvFile releases/previous.env.production `
  -RestoreBackupName 20260724-170000 `
  -ConfirmDataRestore
```

迁移采用只前进策略。普通回滚要求上一应用版本兼容当前数据库；破坏性变更必须先经过“新增兼容
结构 -> 双写/切换 -> 后续版本清理”的多阶段发布，不能依赖 Flyway 自动降级。

## 11. 证书与发布后检查

证书续期后执行：

```powershell
docker compose --env-file .env.production -f docker-compose.prod.yml `
  exec gateway nginx -s reload
```

发布后运行：

```powershell
./scripts/check-production-health.ps1 -EnvFile .env.production
```

健康检查确认 6 个服务运行、后端内部健康端点为 `UP`、公网首页可访问，并验证公网 Swagger 返回
404。随后人工走一遍登录、设备列表、附件下载、提醒和数据导出主流程，观察错误率与响应时间。
