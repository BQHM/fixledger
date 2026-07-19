# FixLedger P14-P24 演示体验指南

## 1. 目标

P14-P24 的目标是把 FixLedger 整理成可以稳定展示、可以被追问、也可以快速恢复的演示项目。演示时不要把它讲成后台模板，而要围绕家庭设备生命周期：设备档案、保修凭证、耗材提醒、维修记录、附件归档、家庭协作、操作日志、家庭数据导出和 AI 辅助。

演示原则：

- 先讲家庭场景，再讲技术实现。
- 先走核心闭环，再回答增强能力。
- AI、对象存储、通知、操作日志和 CI/CD 都作为支撑能力，不抢核心业务主线。
- 明确哪些能力已实现，哪些是后续增强，避免把规划包装成已完成。

## 2. 启动前检查

推荐使用 Docker Compose 演示，只需要 Docker Desktop。首次启动会拉取 MySQL、Redis、RustFS、Maven/JDK、Node.js 和 Nginx 镜像。

```powershell
cd D:\work\work_space\Project\FixLedger
if (!(Test-Path .env)) { Copy-Item .env.example .env }
```

`.env.example` 已经为演示环境准备了默认值：

| 配置 | 演示默认值 | 说明 |
| --- | --- | --- |
| `SQL_INIT_MODE` | `always` | 后端启动时执行 schema 和演示数据脚本 |
| `SQL_DATA_LOCATIONS` | `classpath:db/demo-data.sql` | 加载 P7/P14 演示数据 |
| `FILE_STORAGE_TYPE` | `rustfs` | Docker 演示默认使用 RustFS/S3 兼容存储 |
| `AI_ENABLED` | `false` | 默认不依赖真实 AI Key |
| `AI_PROVIDER` | `mock` | 使用 Mock AI 结果保证离线可演示 |

## 3. 一键启动

```powershell
docker compose up -d --build
docker compose ps
```

访问地址：

| 服务 | 地址 | 用途 |
| --- | --- | --- |
| 前端 | `http://localhost:5173` | 面试主演示入口 |
| 后端健康检查 | `http://localhost:8080/actuator/health` | 验证 Spring Boot 服务状态 |
| OpenAPI | `http://localhost:8080/swagger-ui.html` | 展示接口契约和分层能力 |
| RustFS 控制台 | `http://localhost:9001` | 说明对象存储能力，不建议作为主线演示 |

演示账号：

```text
用户名：demo
密码：fixledger123
默认家庭空间：演示家庭，family_id=1
```

登录页不会默认暴露密码，需要点击“填入演示账号”或手动输入，避免演示账号被误解为生产密钥硬编码。

## 4. 演示数据地图

演示数据位于 `backend/src/main/resources/db/demo-data.sql`，使用固定主键和 `ON DUPLICATE KEY UPDATE`，方便重复执行。

| 数据 | 重点讲法 | 可演示页面 |
| --- | --- | --- |
| `演示家庭` | 所有设备、凭证、提醒和协作日志都挂在家庭空间下，体现 `family_id` 隔离 | 我的家、我的家庭 |
| `小米净水器 S1` | 核心样例设备，覆盖设备详情、保修、滤芯、维修、发票和导出清单 | 设备档案、设备详情、耗材、维修、凭证盒 |
| `戴森吸尘器 V12` | 展示清洁设备、保修卡附件和 HEPA 滤网即将更换 | 设备档案、耗材提醒、凭证盒 |
| `华硕路由器 AX86U` | 展示维修中状态、即将过保和 AI 故障建议 | 我的家、维修记录、智能助手 |
| 提醒任务 | 覆盖耗材即将更换、保修即将到期，说明 Redis 去重 | 我的家、家庭日历、提醒中心 |
| AI 分析记录 | 展示 Mock 故障建议留痕，强调 AI 只辅助不改核心数据 | 智能助手、设备详情 |

注意：`demo-data.sql` 中的附件是元数据示例，真实文件内容需要通过页面上传后写入 RustFS。演示时可以展示附件列表和上传流程；如果没有提前上传真实文件，不要把下载作为必须成功的主流程。

## 5. 5 分钟演示路线

| 时间 | 操作 | 讲解重点 |
| --- | --- | --- |
| 0:00-0:40 | 登录 `demo / fixledger123` | 项目定位是家庭设备管家，不是企业资产后台 |
| 0:40-1:30 | 打开“我的家” | 家庭健康分、本周事项、提醒日历和维修中设备构成入口 |
| 1:30-2:20 | 打开“设备档案”并进入净水器详情 | 一台设备聚合保修、耗材、维修和附件，形成生命周期记录 |
| 2:20-3:10 | 打开“耗材管理” | 更换周期以天存储，记录更换后重新计算下次提醒日期 |
| 3:10-3:50 | 打开“维修记录” | 维修状态流转、费用统计排除已取消记录，并可导出费用报表 |
| 3:50-4:30 | 打开“凭证盒” | 文件内容进 RustFS，MySQL 只保存元数据和对象 Key，下载走后端鉴权 |
| 4:30-5:00 | 打开“智能助手”或“家庭设置” | AI 默认 Mock，失败不影响核心业务；家庭设置可展示协作入口和最近操作日志 |

## 6. 10 分钟演示路线

在 5 分钟路线基础上增加技术追问点：

1. 打开 OpenAPI，说明接口统一 `/api`、统一 `Result<T>`、分页上限和领域错误码。
2. 在提醒中心点击“扫描提醒”，说明定时任务由后端生成提醒，前端不是提醒来源。
3. 打开 Docker Compose 文件，说明 MySQL、Redis、RustFS、后端和前端一键编排。
4. 说明 Redis 的三个关键用途：提醒去重、JWT 黑名单、首页刷新标记/缓存钩子。
5. 说明 P13 安全收口：家庭空间隔离、附件扩展名/MIME/魔数校验、JWT fail-safe、金额和分页边界。
6. 打开“家庭设置”，演示家庭所有者可以邀请已注册用户、调整角色和移除成员，普通成员只读。
7. 打开最近协作日志或 OpenAPI `/api/system/operation-logs`，说明关键协作操作会落库并按家庭权限过滤。
8. 说明 P23 CI/CD：GitHub Actions 支持手动触发，前端类型检查、`dist-ci` 构建、smoke、安全审计和生产准备检查分步执行。
9. 在设备档案页点击“导出清单”，或在维修记录页按日期筛选后点击“导出费用报表”，说明 P24 的 CSV 导出做了家庭权限校验、批量补齐名称和 CSV 安全转义。
10. 说明当前取舍：不做商城、不做企业固定资产盘点，不把真实 AI/OCR/邮件/Webhook/生产域名作为 MVP 阻塞项。

## 6.1 P20-P24 追加演示点

家庭协作：

```text
家庭设置 -> 邀请成员 -> 输入已注册用户名或邮箱 -> 选择角色 -> 确认
家庭设置 -> 成员列表 -> 调整角色 / 移除成员
```

演示口径：

- 只有 `OWNER` 可以管理成员，普通成员只能查看。
- 系统会阻止移除当前登录用户，也会阻止家庭空间失去最后一个 `OWNER`。
- 协作操作会写入操作日志，方便后续追踪家庭成员对设备数据的管理行为。

智能录入：

```text
智能助手 -> 票据文本提取 -> 带入新增设备
```

演示口径：

- AI 解析只生成草稿，用户仍要确认后保存。
- 设备名称、购买日期、购买渠道、价格和建议分类可以带入新增设备表单。
- 没有真实 AI Key 时使用 Mock Provider，不影响核心业务演示。

生产准备检查：

```powershell
./scripts/check-production-readiness.ps1 -Strict
```

演示口径：

- 该脚本检查 Docker Compose、环境变量模板、CI、前端脚本和 JDK 21 配置。
- CI 中的前端构建使用 `dist-ci`，避免本地旧 `dist` 文件锁影响自动化验证。

家庭数据导出：

```text
设备档案 -> 导出清单
维修记录 -> 选择费用日期 -> 导出费用报表
```

演示口径：

- 导出接口是文件下载型接口，不使用 `Result<T>` JSON 包装，但仍然必须认证并校验家庭成员权限。
- 设备导出会批量补齐分类名，维修费用导出会批量补齐设备名，避免 N+1 查询。
- CSV 会增加 UTF-8 BOM 和公式注入防护，方便用 Excel 或 Numbers 打开。

## 7. 快速恢复和重置

保留数据停止：

```powershell
docker compose down
```

重新执行后端初始化脚本：

```powershell
docker compose restart backend
```

完全重置演示数据和对象存储卷，仅限本地演示环境慎用：

```powershell
docker compose down -v
docker compose up -d --build
```

本机运行后端但仍使用 RustFS 时，需要把 S3 endpoint 从容器名改成本机地址：

```powershell
docker compose up -d mysql redis rustfs
$env:FILE_S3_ENDPOINT="http://localhost:9000"
cd backend
mvn spring-boot:run
```

如果只想演示核心业务，不演示对象存储，可以改用本地文件存储：

```powershell
$env:FILE_STORAGE_TYPE="local"
$env:FILE_STORAGE_ROOT="./uploads"
```

## 8. 排障预案

| 问题 | 处理方式 | 面试口径 |
| --- | --- | --- |
| Docker Hub 镜像拉取失败 | 配置镜像加速或在 `.env` 覆盖 `MAVEN_IMAGE`、`JRE_IMAGE`、`NODE_IMAGE`、`NGINX_IMAGE` | 这是外部网络问题，不影响项目 Compose 化设计 |
| 后端健康检查失败 | `docker compose logs -f backend` 查看 MySQL、Redis、RustFS 连接和 SQL 初始化日志 | 后端依赖由 Compose `depends_on` 和健康检查编排 |
| 登录失败 | 确认 `.env` 中 `SQL_INIT_MODE=always`、`SQL_DATA_LOCATIONS=classpath:db/demo-data.sql`，然后重启 backend | 演示账号来自初始化脚本，不是写死在前端 |
| 附件下载失败 | 先通过页面上传一个真实 PDF/JPG/PNG，再下载；SQL 自带附件主要是元数据样例 | 文件权限和存储路径由后端校验，RustFS 只存对象内容 |
| AI 返回不稳定 | 保持 `AI_ENABLED=false`、`AI_PROVIDER=mock` | AI 是辅助能力，Mock 保证核心流程离线可演示 |
| 操作日志为空 | 先在家庭设置页执行一次邀请、角色调整或移除成员操作 | 操作日志记录关键协作行为，不记录所有读操作 |
| CSV 导出没有数据 | 先确认当前家庭有设备或已完成且有费用的维修记录，维修导出可清空日期筛选再试 | 导出按家庭权限和业务口径过滤，不为演示伪造数据 |
| 生产检查失败 | 根据脚本输出补齐缺失配置或脚本项，再重新执行 `-Strict` | 发布前检查显式化，避免部署条件只靠口头记忆 |
| 日期提醒不符合当前日期 | 使用已初始化提醒记录，或在提醒中心手动扫描 | 定时任务和 Redis 去重是重点，不依赖前端临时造数据 |

## 9. 高频问答速答

| 问题 | 简短回答 |
| --- | --- |
| 为什么不是普通后台？ | 页面和数据围绕家庭设备生命周期组织，不以通用表格 CRUD 为核心。 |
| 为什么用家庭空间隔离？ | 家庭成员共享设备数据，`family_id` 比单纯 `user_id` 更符合场景，也能支持多家庭。 |
| Redis 宕机会怎样？ | MySQL 是业务事实；首页缓存可回源，提醒去重会有重复风险；JWT 黑名单在 P13 按 fail-safe 处理。 |
| 为什么 RustFS 而不是把文件放数据库？ | 数据库存元数据，对象存储放大文件，后端统一鉴权，后续可替换 MinIO/S3。 |
| AI 失败怎么办？ | 不回滚设备、保修、耗材、维修等核心流程，返回兜底建议并记录分析状态。 |
| 家庭协作如何防越权？ | 后端先校验当前用户是否属于家庭且是否为 `OWNER`，操作日志查询也按家庭成员关系过滤。 |
| 导出接口为什么不返回 Result？ | CSV 是文件下载响应，需要浏览器按附件处理；后端仍保留认证、权限和业务异常处理。 |
| 为什么暂不做 OCR/真实邮件/Webhook？ | 它们需要外部服务和密钥，不是 MVP 核心闭环；当前先证明架构留好扩展点。 |

## 10. 演示完成检查

- [ ] 可以通过 `docker compose up -d --build` 启动完整环境。
- [ ] 可以登录 `demo / fixledger123`。
- [ ] 可以从“我的家”进入设备、耗材、维修、凭证和 AI 路线。
- [ ] 可以在“家庭设置”演示成员邀请、角色调整、移除和最近协作日志。
- [ ] 可以在“设备档案”或“维修记录”演示 CSV 导出。
- [ ] 可以解释 MySQL、Redis、RustFS、Scheduler 和 AI Mock 的职责边界。
- [ ] 可以说明 P13 安全与数据隔离、P22 操作日志/通知抽象、P23 CI/生产检查和 P24 数据导出已经收口。
