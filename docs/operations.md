# FixLedger 性能与可观测性运维说明

## 1. 默认安全边界

- `/actuator/health`、`/actuator/info` 默认公开。
- `metrics`、`prometheus` 默认不暴露，避免普通公网入口泄露运行信息。
- 需要采集时设置 `MANAGEMENT_ENDPOINTS=health,info,metrics,prometheus`，并在反向代理或容器网络限制来源。
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

## 6. 已有数据库索引升级

新建数据库会通过 `schema.sql` 自动创建 `idx_fl_device_asset_list`。已有 Docker MySQL 数据卷不会因为
`CREATE TABLE IF NOT EXISTS` 自动补索引，升级后先检查：

```sql
SELECT COUNT(*) AS index_exists
FROM information_schema.statistics
WHERE table_schema = DATABASE()
  AND table_name = 'fl_device_asset'
  AND index_name = 'idx_fl_device_asset_list';
```

仅当结果为 `0` 时执行：

```sql
ALTER TABLE fl_device_asset
  ADD INDEX idx_fl_device_asset_list (family_id, updated_at);
```

执行后用 `SHOW INDEX FROM fl_device_asset` 确认索引存在。不要通过删除 Docker 数据卷来应用索引，
避免丢失已经录入的家庭设备、保修、耗材和维修数据。

## 7. 前端生产包体

- Element Plus 只安装当前使用的组件，ECharts 只注册饼图、折线图及必要组件。
- Vite 按 Vue、ECharts 和通用依赖拆分稳定 vendor，业务页面继续使用路由懒加载。
- P29 基线中入口和首页 JS 分块分别为 `1074.33 KiB`、`1025.64 KiB`；优化后入口业务块为
  `442.80 KiB`，首页页面块与图表 vendor 合计 `484.67 KiB`，构建不再出现大分块警告。
- 后续升级 Element Plus、ECharts 或 Vite 时需要重新执行生产构建，不能只调高警告阈值。
