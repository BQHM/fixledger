# FixLedger 面试讲解指南

## 1. 一句话介绍

FixLedger 是一个面向家庭场景的设备生命周期管理系统，用来统一管理家电和数码设备的设备档案、保修记录、耗材更换、维修过程、提醒任务和凭证附件。

如果面试官只给 30 秒，可以这样说：

> 我做的是一个家庭设备管家系统，不是企业资产后台。它围绕一台设备从购买、上传发票、记录保修、配置耗材周期、生成提醒、记录维修到后续 AI 辅助总结的完整生命周期展开。技术上使用 Spring Boot 3 + JDK 21 + MyBatis Plus + MySQL + Redis + RustFS + Vue3，重点体现分层设计、数据隔离、定时任务、文件存储、AI 解耦和 Docker 一键启动。

## 2. 项目背景

日常家庭里会有很多设备：净水器、空气净化器、扫地机器人、路由器、耳机、电脑、手机、冰箱、洗衣机等。这些设备的购买记录、发票、说明书、保修卡、维修单和耗材更换时间通常分散在电商平台、相册、聊天记录和纸质文件里。

FixLedger 要解决的问题是：

- 设备是否还在保修期内不好查。
- 发票、保修卡、说明书和维修凭证不好找。
- 净水器滤芯、空气净化器滤网等耗材容易忘记更换。
- 维修过程、维修费用和处理结果没有长期记录。
- 家庭成员之间设备信息不共享。

项目边界要讲清楚：FixLedger 不做商城、不做企业固定资产盘点、不做泛后台模板，也不把 AI 作为核心卖点。它只聚焦家庭设备生命周期管理。

## 3. 技术栈

后端：

| 技术 | 用途 | 面试口径 |
| --- | --- | --- |
| JDK 21 | 后端运行和开发 | LTS 版本，适合体现较新的 Java 实践 |
| Spring Boot 3.3.x | Web 应用框架 | 生态成熟，适合快速搭建 REST API |
| Spring Security + JWT | 登录认证和接口鉴权 | 无状态认证，退出登录通过 Redis 黑名单让旧 Token 失效 |
| MyBatis Plus | ORM 和分页 CRUD | 简化常规 CRUD，复杂查询仍可扩展 XML |
| MySQL 8 | 主业务数据库 | 保存设备、保修、耗材、维修、提醒和附件元数据 |
| Redis 7 | 去重、Token 黑名单、缓存钩子 | 提醒去重、退出登录黑名单、首页刷新标记/缓存钩子 |
| Spring Scheduler | 定时任务 | 定期扫描保修到期和耗材更换提醒 |
| RustFS / S3 兼容存储 | 文件内容存储 | Docker 默认对象存储，本地文件保留测试兜底 |
| 自定义 AiClient | AI 辅助能力抽象 | Mock 和 OpenAI-compatible 可替换，AI 失败不影响核心流程 |

前端：

| 技术 | 用途 | 面试口径 |
| --- | --- | --- |
| Vue 3 + TypeScript | 前端页面和类型约束 | 组件化开发，减少接口字段误用 |
| Vite | 构建工具 | 启动和构建速度快 |
| Element Plus | 基础组件 | 提供表单、表格、弹窗等通用能力 |
| Pinia | 状态管理 | 管理 Token、当前用户、家庭空间 |
| Vue Router | 路由管理 | 登录守卫和页面跳转 |
| Axios | HTTP 请求 | 统一封装接口、Token、错误处理 |
| ECharts | 数据可视化 | 首页统计、分类分布、费用趋势 |

## 4. 架构分层

后端分层是：

```text
Controller -> Service -> Mapper
                ↕
        Infrastructure
```

各层职责：

- Controller：只做路由、参数校验、读取当前用户、调用 Service，返回 `Result<T>`。
- Service：负责业务编排、状态流转、事务边界和跨模块校验。
- Mapper：负责 MyBatis Plus 数据访问，简单 CRUD 用 BaseMapper，复杂查询预留 XML。
- Infrastructure：封装 Redis、文件存储、AI、通知、定时任务等技术细节。

面试口径：

> 我把业务模块和技术基础设施拆开。业务代码不直接操作 StringRedisTemplate、RustFS SDK 或 AI SDK，而是通过 RedisService、FileStorageService、AiClient 这些稳定接口调用。这样后续从 RustFS 换 MinIO，或者从 Mock AI 换真实 Provider，不需要改业务流程。

## 5. 核心业务闭环

推荐按这条业务线讲项目：

```text
注册/登录
  ↓
进入默认家庭空间
  ↓
创建设备档案
  ↓
录入保修记录并上传发票/保修卡/说明书
  ↓
配置耗材和更换周期
  ↓
定时任务生成保修/耗材提醒，Redis 做去重
  ↓
设备故障时创建维修记录并进行状态流转
  ↓
首页展示家庭健康、提醒日历、费用趋势
  ↓
AI 辅助票据提取、故障建议和维修总结
```

这条线可以体现：

- 不是单表 CRUD，而是设备生命周期闭环。
- MySQL 保存业务事实。
- Redis 解决重复提醒和短期缓存。
- RustFS 保存附件内容。
- AI 只做辅助，不影响核心数据。
- 前端从“后台管理”转成“家庭设备管家”。

## 6. 数据库设计

核心表可以按领域讲：

| 领域 | 表 | 说明 |
| --- | --- | --- |
| 用户认证 | `sys_user` | 用户账号、密码哈希、状态、最后登录时间 |
| 家庭空间 | `fl_family_space`、`fl_family_member` | 家庭空间和成员关系，是数据隔离核心 |
| 设备档案 | `fl_device_category`、`fl_device_asset` | 分类和设备基础信息 |
| 保修 | `fl_warranty_record` | 保修类型、开始结束日期、提前提醒天数 |
| 耗材 | `fl_consumable_item`、`fl_consumable_replace_record` | 更换周期、上次更换、下次提醒、更换记录 |
| 维修 | `fl_maintenance_record` | 故障描述、状态流转、费用和处理结果 |
| 提醒通知 | `fl_reminder_task`、`fl_notification_record` | 提醒任务和站内通知记录 |
| 文件 | `fl_file_resource` | 附件元数据和 RustFS 对象 Key |
| AI | `fl_ai_analysis` | AI 调用结果、状态和留痕 |

设计重点：

- 需要家庭隔离的业务表都有 `family_id`。
- 常用查询字段建索引，例如 `family_id`、`device_id`、`status`、`end_date`、`next_remind_date`。
- 金额用 `DECIMAL`，不用浮点数。
- 核心数据优先逻辑删除，避免破坏设备生命周期历史。
- 文件表只存元数据和对象 Key，不存公开访问 URL。

面试口径：

> 这个项目的权限隔离不是简单靠 user_id，而是以家庭空间 family_id 为核心。一个用户可以属于多个家庭，设备、保修、耗材、维修、提醒、附件和 AI 分析都归属到某个家庭，查询和更新都要同时校验家庭成员关系。

## 7. 接口设计

接口统一约定：

- 路径前缀：`/api`。
- 认证方式：`Authorization: Bearer <accessToken>`。
- 响应结构：`Result<T>`。
- 分页结构：`PageResponse<T>`。
- 分页上限：`1 <= pageSize <= 100`。
- 错误码按领域分段，例如通用 1xxx、认证 2xxx、设备 4xxx、文件 9xxx、AI 10xxx。

典型接口：

| 功能 | 接口 |
| --- | --- |
| 登录 | `POST /api/auth/login` |
| 退出登录 | `POST /api/auth/logout` |
| 当前用户 | `GET /api/auth/me` |
| 家庭列表 | `GET /api/families` |
| 设备分页 | `GET /api/families/{familyId}/devices` |
| 设备详情 | `GET /api/families/{familyId}/devices/{deviceId}` |
| 保修记录 | `GET /api/families/{familyId}/devices/{deviceId}/warranties` |
| 耗材更换 | `POST /api/families/{familyId}/consumables/{consumableId}/replace-records` |
| 手动扫描提醒 | `POST /api/families/{familyId}/reminders/scan` |
| 附件上传 | `POST /api/families/{familyId}/files` |
| AI 故障建议 | `POST /api/families/{familyId}/ai/troubleshooting` |

面试口径：

> Controller 不直接处理业务逻辑，只负责接收参数和调用 Service。Service 会拿当前登录用户和路径里的 familyId 做家庭成员校验，然后再处理业务。这样接口层清晰，权限逻辑集中，测试也更容易写。

## 8. Docker 一键启动

面试演示建议直接使用 Docker Compose。

```powershell
cd D:\work\work_space\Project\FixLedger
docker compose up -d --build
docker compose ps
```

访问地址：

| 服务 | 地址 |
| --- | --- |
| 前端 | `http://localhost:5173` |
| 后端 | `http://localhost:8080` |
| OpenAPI | `http://localhost:8080/swagger-ui.html` |
| RustFS API | `http://localhost:9000` |
| RustFS 控制台 | `http://localhost:9001` |

演示账号：

```text
用户名：demo
密码：fixledger123
默认家庭空间 ID：1
```

如果 Docker Hub 拉镜像失败，可以说明：

> 项目本身已经 Docker Compose 化，网络问题通常是基础镜像拉取失败。可以配置 Docker Desktop 镜像加速，或者在 `.env` 里覆盖 `MAVEN_IMAGE`、`JRE_IMAGE`、`NODE_IMAGE`、`NGINX_IMAGE`。

## 9. AI 为什么是辅助能力

AI 能做：

- 从发票文本提取设备名称、购买日期、价格、商家。
- 根据故障描述生成初步排查建议。
- 根据维修历史生成维护总结。

AI 不能做：

- 不能自动覆盖用户设备数据。
- 不能作为设备、保修、耗材、维修主流程的前置依赖。
- 不能接收密码、Token、身份证号等敏感信息。

设计方式：

- 所有 AI 调用通过 `AiClient`。
- 支持 Mock Provider，开发测试不依赖真实 API Key。
- AI 失败返回兜底建议，并记录 `fl_ai_analysis`。
- Prompt 模板放在 `resources/prompts/`。

面试口径：

> AI 在这个项目里不是主业务，而是减少录入和整理成本。即使 AI 关闭，用户仍然能创建设备、上传凭证、配置提醒和记录维修。这样可以避免把核心业务绑死在外部大模型服务上。

## 10. 文件为什么用 RustFS，同时保留本地存储

当前 Docker 默认使用 RustFS，原因是：

- RustFS 兼容 S3 API，适合本地 Docker 演示对象存储。
- 发票、保修卡、说明书 PDF、维修单这类附件更适合放对象存储，不适合直接塞数据库。
- 后端通过 `FileStorageService` 抽象文件存储，业务层不关心底层是 RustFS、MinIO 还是本地文件。
- 测试环境保留本地文件存储，避免测试依赖外部对象存储服务。

文件安全边界：

- 上传限制大小、扩展名和 MIME 类型。
- 文件名拒绝路径穿越字符。
- 元数据写 MySQL，内容写 RustFS。
- 下载必须先经过后端家庭空间鉴权，再读取对象流返回。
- 前端不直接访问 RustFS 对象地址。

面试口径：

> MySQL 存文件元数据，RustFS 存文件内容。这样既能保证业务查询和权限控制在数据库里完成，又避免数据库存大文件。下载时仍走后端鉴权，所以对象存储地址不会直接暴露给前端。

## 11. Redis 用在哪里

当前 Redis 主要有三个落地点：

| 场景 | Key | 说明 |
| --- | --- | --- |
| 提醒去重 | `fixledger:reminder:dedupe:{type}:{bizId}:{date}` | 避免同一事项同一天重复生成提醒 |
| 首页刷新标记 / 缓存钩子 | `fixledger:dashboard:summary:{familyId}` | 当前写入刷新标记，后续可扩展为首页热点数据短期缓存 |
| JWT 黑名单 | `fixledger:auth:blacklist:{tokenId}` | 退出登录后让旧 Token 立即失效 |

面试口径：

> Redis 没有替代 MySQL。重要业务事实仍然以 MySQL 为准，Redis 只做短期去重、缓存和 Token 黑名单，而且 Key 集中定义、TTL 显式设置。

## 12. 安全设计

当前安全重点：

- 密码使用 BCrypt 哈希存储。
- 除登录注册外，接口需要 JWT 认证。
- JWT 包含 `jti`，退出登录写 Redis 黑名单。
- 带 `familyId` 的业务接口统一校验家庭成员关系。
- 详情查询按 `id + family_id` 查询，防止猜 ID 越权。
- 文件上传校验类型、大小、文件名路径字符。
- 错误响应不暴露底层 JSON 解析或堆栈细节。
- 日志不输出密码、Token、API Key。

面试口径：

> 安全不是只靠前端隐藏按钮。我的权限控制放在后端 Service 层，所有家庭数据都会先校验当前用户是否属于该家庭，再按 family_id 过滤数据。

## 13. 测试与质量

当前后端测试覆盖：

- 认证：未登录、登录成功、退出后旧 Token 失效、无效 Token。
- 家庭空间：非成员访问被拒绝。
- 设备：创建、分页、详情、状态流转、非成员访问。
- 保修：日期校验、即将过保、家庭隔离。
- 耗材：更换周期、下次提醒日期、非成员访问。
- 维修：状态流转、费用统计排除取消记录。
- 提醒：Redis 去重、数据库失败释放去重键。
- 文件：非法扩展名、非法 MIME、路径穿越、非成员下载。
- AI：Mock 调用、家庭隔离、失败兜底。

验证命令：

```powershell
cd D:\work\work_space\Project\FixLedger\backend
mvn test -q

cd D:\work\work_space\Project\FixLedger\frontend
npm run build

cd D:\work\work_space\Project\FixLedger
docker compose config --quiet
```

## 14. 5-10 分钟演示路线

详细操作手册见 `docs/demo-guide.md`。面试现场建议先按 5 分钟路线跑核心闭环，再根据追问切到 10 分钟技术路线。

### 14.1 5 分钟核心路线

| 时间 | 操作 | 讲解重点 |
| --- | --- | --- |
| 0:00-0:40 | 打开 `http://localhost:5173`，登录 `demo / fixledger123` | FixLedger 是家庭设备管家，不是企业资产后台 |
| 0:40-1:30 | 进入“我的家” | 家庭健康分、本周事项、提醒日历和房间概览体现家庭场景 |
| 1:30-2:20 | 进入“设备护照”，打开“小米净水器 S1” | 一台设备聚合设备档案、保修、耗材、维修和附件 |
| 2:20-3:10 | 进入“耗材管理” | 更换周期以天存储，记录更换后重新计算下次提醒日期 |
| 3:10-3:50 | 进入“维修记录” | 维修状态流转，费用统计排除已取消记录 |
| 3:50-4:30 | 进入“凭证盒” | RustFS 存文件内容，MySQL 存元数据和对象 Key，图片/PDF 预览与下载都走后端鉴权 |
| 4:30-5:00 | 进入“智能助手” | AI 默认 Mock，只辅助生成建议，不覆盖核心业务数据 |

### 14.2 10 分钟技术路线

1. 打开 OpenAPI，说明统一响应、分页上限、错误码分域和 REST 路径设计。
2. 在提醒中心触发手动扫描，说明真实提醒由后端定时任务生成，不依赖前端触发。
3. 打开 Docker Compose，说明前端、后端、MySQL、Redis、RustFS 的一键编排。
4. 展示 `docs/tasks.md` 的 P12/P13/P14 验证记录，说明测试、安全和演示留痕。
5. 说明 Redis 的职责边界：提醒去重、JWT 黑名单、首页刷新标记/缓存钩子。
6. 说明 P13 安全收口：家庭空间隔离、附件扩展名/MIME/魔数校验、JWT fail-safe、金额和分页边界。

### 14.3 现场兜底口径

- Docker Hub 拉取失败：说明 Compose 文件和健康检查已具备，网络问题可通过镜像加速或 `.env` 镜像变量解决。
- RustFS 附件下载失败：先通过页面上传真实文件；初始化 SQL 中的附件主要用于展示元数据，不强行演示下载。
- AI 不可用：保持 Mock 模式，强调核心业务不依赖真实 AI Provider。
- 提醒日期和当前日期不完全匹配：使用初始化提醒记录或手动扫描，重点讲定时任务和 Redis 去重机制。

## 15. 当前已完成和后续计划

已完成：

- P0-P7：后端脚手架、认证、家庭空间、设备、保修、附件、耗材、维修、提醒、看板、AI、Docker 和测试。
- P8：产品体验从后台模块转向“我的家、家庭日历、设备护照、凭证盒、智能助手”。
- P9：文档对齐、代码质量、安全治理、测试补强、RustFS、Skills 文档规范、面试材料、设备护照体验、CI 和全量验收。
- P10：需求、架构、接口、数据库、UI 和 README 深度对齐，确保文档能对应当前代码和演示体验。
- P11：完成异常、日志、事务、配置、前端 API 封装、路由守卫和静态检查治理。
- P12：补强 Service、Controller、前端 smoke 和 Docker 健康检查，核心闭环可验证。
- P13：完成家庭空间隔离、附件安全、JWT 敏感信息和边界参数专项审计。
- P14：补齐演示指南、演示数据地图、README 展示说明、5-10 分钟路线和高频问答。

P14 对演示边界的取舍：

- 已做 Docker 一键启动、RustFS、Mock AI、CI、测试记录和安全审计，因为它们能直接提升演示可信度。
- 暂缓 OCR、复杂 PDF 解析、邮件/Webhook、对象存储临时 URL、真实 AI Provider、多端 Refresh Token 和完整 RBAC，因为它们会引入外部服务、密钥、访问有效期和更复杂的会话策略；P15 已先用后端鉴权下载流完成图片/PDF Blob 预览，P16.2 已补充说明书第一版关键词搜索。
- 初始化 SQL 中的附件数据是元数据样例；如果要现场下载文件，应提前通过页面上传真实文件。

后续建议：

- P16 后续：继续做智能归档增强。当前已完成公开首页、米家风格视觉基线、设备护照深化、凭证盒预览、凭证盒后端聚合和说明书第一版关键词搜索；后续可继续接 OCR、真实 AI Provider、通知渠道、操作日志和自动化部署流水线。
- 家庭成员邀请、移除、角色调整。
- 默认设备分类自动初始化。
- 设备二维码标签、说明书 OCR/复杂 PDF 解析和导出报表。

## 16. 高频问答

### Q1：这个项目是不是普通后台管理系统？

不是。普通后台通常按模块表格组织，而 FixLedger 按家庭场景和设备生命周期组织：我的家、家庭日历、设备护照、凭证盒、智能助手。核心关注保修、耗材、维修和凭证归档，而不是企业资产盘点。

### Q2：为什么不用 MinIO，而用 RustFS？

RustFS 和 MinIO 都是 S3 兼容对象存储。当前选择 RustFS 是因为本地 Docker 演示轻量、接口兼容 S3；后端通过 `FileStorageService` 和 S3 SDK 抽象，后续换 MinIO 不需要改业务接口。

### Q3：JWT 退出登录怎么处理？

JWT 无状态，旧 Token 默认到期前都能用。项目给每个 Token 加 `jti`，退出时把 `jti` 放入 Redis 黑名单，TTL 设置为 Token 剩余有效期。过滤器每次解析 Token 后检查黑名单，所以退出后旧 Token 立即失效。

### Q4：Redis 宕机会不会影响核心数据？

核心业务事实在 MySQL。Redis 用于去重、缓存和 Token 黑名单。提醒去重失败可能影响重复提醒风险，首页缓存失败可以回源数据库；认证黑名单依赖 Redis，因此生产环境需要保证 Redis 可用性，后续可增加更完善的降级和监控。

### Q5：AI 不可用怎么办？

AI 是辅助能力。设备、保修、耗材、维修和提醒不依赖 AI。AI 失败时返回兜底建议并记录分析状态，不会回滚核心业务。

### Q6：家庭空间如何防越权？

所有带 `familyId` 的业务接口在 Service 层调用 `familyService.checkFamilyMember(userId, familyId)`。详情查询也会同时带 `id` 和 `family_id`，避免用户猜测其他家庭数据 ID。

### Q7：文件为什么不直接存在数据库？

数据库适合存结构化元数据，不适合存大文件。文件内容放 RustFS，MySQL 只存对象 Key、原文件名、MIME、大小和业务归属。这样便于扩展容量，也不影响权限查询。


### Q8：演示时附件下载失败怎么办？

先确认是否通过页面上传过真实文件。初始化 SQL 里的附件记录主要用于展示元数据和归档关系，真实对象内容需要上传后进入 RustFS。面试时可以把这个点讲成“数据库存元数据，对象存储存内容，后端负责鉴权转发”。

### Q9：为什么 P14 不直接提交截图？

静态截图容易在 UI 迭代后过期。P14 选择沉淀可重复启动、数据地图、演示路线和排障预案；如果后续要补截图，应按 `docs/demo-guide.md` 先启动真实环境，再截取当前页面。
