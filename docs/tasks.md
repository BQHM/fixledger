# FixLedger 开发任务拆分

## 1. 开发原则

按照 `AGENTS.md` 的当前阶段执行原则：

1. 先补文档，再写代码。
2. 先完成核心闭环：设备档案 → 保修记录 → 耗材提醒 → 维修记录 → 首页看板。
3. Redis、文件存储、AI、通知等增强能力解耦实现。
4. AI 功能默认支持关闭和 Mock 模式。
5. 每个阶段完成后同步 README、接口文档和任务进度。

## 2. 总体阶段

| 阶段 | 名称 | 目标 |
| --- | --- | --- |
| P0 | 文档与脚手架 | 完成开发资料、项目结构、基础配置 |
| P1 | 认证与家庭空间 | 完成登录、注册、家庭空间、数据隔离 |
| P2 | 设备档案核心 | 完成设备分类、设备档案、设备详情 |
| P3 | 保修与附件 | 完成保修记录、附件上传、保修提醒基础 |
| P4 | 耗材与维修 | 完成耗材周期、更换记录、维修状态流转 |
| P5 | 提醒与看板 | 完成定时提醒、站内通知、首页统计 |
| P6 | AI 辅助 | 完成 Mock AI、票据提取、故障建议、维修总结 |
| P7 | 工程化完善 | 完成测试、Docker、日志、README、演示数据 |
| P8 | 产品化体验重构 | 从后台模块视角转向家庭场景视角 |
| P9 | 系统性完善阶段 | 文档对齐、代码质量、测试、安全、演示和产品体验系统打磨 |
| P10 | 文档对齐 | 让需求、架构、接口、数据库、UI、README 与当前实现一致 |
| P11 | 代码质量治理 | 统一异常、日志、注释、分页、安全边界和分层职责 |
| P12 | 测试体系补强 | 补强 Service、Controller、核心流程和边界场景测试 |
| P13 | 安全与数据隔离 | 强化家庭空间隔离、附件鉴权、JWT 和敏感信息保护 |
| P14 | 演示体验完善 | 准备演示数据、演示账号、README、截图和面试讲解稿 |
| P15 | 产品体验升级与可选增强 | 继续弱化后台感，评估文件存储、AI Provider、通知和 CI/CD |

## 2.1 全阶段小版本计划总览

为保证全程开发留痕，后续每个大版本都必须拆成小版本，并在本文件保留计划、范围、验收标准和完成记录。新增、调整或废弃小版本时，不删除历史计划，只在对应阶段补充“变更说明”或“完成记录”。

| 大版本 | 小版本 | 计划目标 | 当前状态 |
| --- | --- | --- | --- |
| P0 文档与脚手架 | P0.1 完善项目文档 | 完成 README、AGENTS 和 docs 基础文档 | 已完成 |
| P0 文档与脚手架 | P0.2 后端脚手架 | 创建 Spring Boot 3.x + JDK 21 后端基础工程 | 已完成 |
| P0 文档与脚手架 | P0.3 前端脚手架 | 创建 Vue3 + Vite + TypeScript 前端基础工程 | 已完成 |
| P0 文档与脚手架 | P0.4 基础设施配置 | 补齐环境变量、应用配置和 Docker 草稿 | 已完成 |
| P1 认证与家庭空间 | P1.1 通用后端基础 | 统一响应、异常、分页和审计字段 | 已完成 |
| P1 认证与家庭空间 | P1.2 用户认证 | 注册、登录、JWT、退出登录和当前用户 | 已完成 |
| P1 认证与家庭空间 | P1.3 家庭空间 | 默认家庭空间、成员关系和空间权限校验 | 已完成 |
| P1 认证与家庭空间 | P1.4 前端认证页面 | 登录注册页、Token Store、路由守卫和家庭入口 | 已完成 |
| P2 设备档案核心 | P2.1 设备分类 | 分类增删改查和家庭内唯一性约束 | 已完成，默认分类初始化待增强 |
| P2 设备档案核心 | P2.2 设备档案后端 | 设备创建、分页、详情、状态流转和逻辑删除 | 已完成 |
| P2 设备档案核心 | P2.3 设备档案前端 | 设备列表、筛选、新增编辑和详情基础信息 | 已完成 |
| P3 保修与附件 | P3.1 保修后端 | 保修记录增删改查、即将过保查询和日期校验 | 已完成 |
| P3 保修与附件 | P3.2 文件附件后端 | 本地文件存储、上传、查询、下载和删除 | 已完成 |
| P3 保修与附件 | P3.3 保修与附件前端 | 保修管理、附件上传列表和下载 | 已完成 |
| P4 耗材与维修 | P4.1 耗材后端 | 耗材周期、更换记录和下次提醒日期计算 | 已完成 |
| P4 耗材与维修 | P4.2 维修后端 | 维修记录、状态流转、费用统计和逻辑删除 | 已完成 |
| P4 耗材与维修 | P4.3 耗材与维修前端 | 耗材管理、更换弹窗、维修列表和详情 | 已完成 |
| P5 提醒与看板 | P5.1 Redis 基础 | RedisService、Key 常量、提醒去重和统计缓存 | 已完成，验证码缓存为二期可选 |
| P5 提醒与看板 | P5.2 提醒任务 | 保修/耗材扫描、提醒列表、未读、已读和忽略 | 已完成 |
| P5 提醒与看板 | P5.3 首页看板 | 总览、分类分布、费用趋势和提醒日历 | 已完成 |
| P6 AI 辅助 | P6.1 AI 基础设施 | AiClient、Mock、OpenAI-compatible、配置和分析表 | 已完成 |
| P6 AI 辅助 | P6.2 发票文本提取 | 票据文本解析、分析留痕和前端可编辑确认 | 已完成 |
| P6 AI 辅助 | P6.3 故障排查建议 | 按设备上下文生成故障建议并关联维修记录 | 已完成 |
| P6 AI 辅助 | P6.4 维修总结 | 基于维修历史生成设备维护总结 | 已完成 |
| P7 工程化完善 | P7.1 测试 | 核心业务 Service 和 Mock AI 测试 | 已完成 |
| P7 工程化完善 | P7.2 Docker 与部署 | 前后端、MySQL、Redis 一键启动；P9.7.1 后补充 RustFS | 已完成 |
| P7 工程化完善 | P7.3 演示数据 | 初始化演示用户、家庭、设备和业务数据 | 已完成 |
| P7 工程化完善 | P7.4 文档收尾 | README、接口、数据库和任务状态同步 | 已完成 |
| P8 产品化体验重构 | P8.1 主导航场景化 | 一级导航从后台模块转为家庭场景入口 | 已完成 |
| P8 产品化体验重构 | P8.2 我的家首页 | 家庭健康分、本周事项、房间概览和提醒日历 | 已完成 |
| P8 产品化体验重构 | P8.3 设备护照 | 设备卡片墙、设备护照摘要和生命周期时间线 | 计划中 |
| P8 产品化体验重构 | P8.4 凭证盒 | 附件分类、凭证完整度和家庭凭证收纳体验 | 计划中 |
| P9 系统性完善阶段 | P9.1 文档与实现对齐 | 核对 docs、README 与当前代码实现一致 | 已完成 |
| P9 系统性完善阶段 | P9.2 代码质量治理 | 治理异常、日志、分层、事务、分页和注释 | 已完成 |
| P9 系统性完善阶段 | P9.2.1 代码质量基线扫描与首批修复 | 扫描异常、返回值、事务、日志和敏感信息，完成首批异常契约修复 | 已完成 |
| P9 系统性完善阶段 | P9.2.2 提醒扫描事务边界治理 | 拆清 Redis 去重、扫描查询、提醒和通知写库的事务边界 | 已完成 |
| P9 系统性完善阶段 | P9.2.3 日志、敏感信息与认证退出治理 | 收敛开发日志、JWT 退出黑名单、异常信息脱敏和文件名安全 | 已完成 |
| P9 系统性完善阶段 | P9.3 测试体系补强 | 补强 Service、Controller、核心规则和边界测试 | 已完成 |
| P9 系统性完善阶段 | P9.4 安全与数据隔离 | 审查家庭空间隔离、附件鉴权、JWT 和敏感信息 | 已完成 |
| P9 系统性完善阶段 | P9.5 演示体验与面试材料 | 准备演示数据、README、演示路径和讲解稿 | 已完成 |
| P9 系统性完善阶段 | P9.6 产品体验继续打磨 | 继续推进设备护照、家庭日历、凭证盒和智能助手 | 已完成 |
| P9 系统性完善阶段 | P9.7 可选增强能力 | 评估对象存储、真实 AI、通知渠道、CI/CD 等增强项 | 进行中，RustFS 已接入 |
| P9 系统性完善阶段 | P9.7.1 RustFS 文件存储接入 | 将上传文件从本地存储切换为 RustFS/S3 兼容对象存储 | 已完成 |
| P9 系统性完善阶段 | P9.7.2 CI 与可选增强评估 | 增加质量门禁并明确真实 AI、通知、预览等增强边界 | 计划中 |
| P9 系统性完善阶段 | P9.8 Skills 文档规范对齐 | 按 skills 规范补齐规格、ADR、任务模板、边界和验证口径 | 已完成 |
| P9 系统性完善阶段 | P9.9 P9 全量验收收尾 | 汇总 P9 验收、验证命令、面试口径和后续 P10 方向 | 计划中 |
| P10 文档对齐 | P10.1 需求文档复核 | 深度复核 `requirements.md` 与当前产品边界 | 计划中 |
| P10 文档对齐 | P10.2 架构文档复核 | 深度复核 `architecture.md` 与当前工程实现 | 计划中 |
| P10 文档对齐 | P10.3 接口与数据库文档复核 | 深度复核 `api.md`、`database.md` 与代码/数据库 | 计划中 |
| P10 文档对齐 | P10.4 UI 与 README 复核 | 深度复核 `ui.md`、`README.md` 与演示体验 | 计划中 |
| P11 代码质量治理 | P11.1 后端规范扫描 | 扫描异常、日志、事务、配置和 Entity 暴露风险 | 计划中 |
| P11 代码质量治理 | P11.2 前端规范扫描 | 扫描 API 封装、类型、路由守卫和页面职责 | 计划中 |
| P11 代码质量治理 | P11.3 注释与命名复核 | 复核类注释、方法注释、命名和无效注释 | 计划中 |
| P11 代码质量治理 | P11.4 构建与静态检查 | 固化后端测试、前端构建和格式检查流程 | 计划中 |
| P12 测试体系补强 | P12.1 Service 核心业务测试 | 补强保修、耗材、维修、提醒和 AI 业务规则 | 计划中 |
| P12 测试体系补强 | P12.2 Controller 与权限测试 | 补强参数校验、认证拦截和越权访问测试 | 计划中 |
| P12 测试体系补强 | P12.3 前端构建与冒烟测试 | 固化前端构建、关键页面和接口联调检查 | 计划中 |
| P12 测试体系补强 | P12.4 Docker 启动验证 | 固化一键启动后的健康检查和排障说明 | 计划中 |
| P13 安全与数据隔离 | P13.1 家庭空间隔离审计 | 审查设备、保修、耗材、维修、提醒和附件归属校验 | 计划中 |
| P13 安全与数据隔离 | P13.2 附件安全审计 | 审查文件类型、大小、访问鉴权和删除策略 | 计划中 |
| P13 安全与数据隔离 | P13.3 认证与敏感信息审计 | 审查 JWT、密码、Token、日志和响应脱敏 | 计划中 |
| P13 安全与数据隔离 | P13.4 边界参数审计 | 审查分页上限、ID 越权、日期和金额边界 | 计划中 |
| P14 演示体验完善 | P14.1 演示数据整理 | 固化可重复演示的用户、家庭、设备和业务数据 | 计划中 |
| P14 演示体验完善 | P14.2 面试演示路径 | 整理 5-10 分钟功能演示和技术讲解路线 | 计划中 |
| P14 演示体验完善 | P14.3 README 展示增强 | 增加启动、账号、截图、功能亮点和排障说明 | 计划中 |
| P14 演示体验完善 | P14.4 常见问答材料 | 整理架构、数据库、Redis、AI、文件存储和安全问答 | 计划中 |
| P15 产品体验升级与可选增强 | P15.1 设备护照深化 | 完善设备详情生命周期表达和可视化体验 | 计划中 |
| P15 产品体验升级与可选增强 | P15.2 凭证盒深化 | 完善凭证分类、完整度和附件预览体验 | 计划中 |
| P15 产品体验升级与可选增强 | P15.3 存储与 AI 增强 | 评估 RustFS/MinIO、真实 AI Provider 和 Mock 兜底 | 计划中 |
| P15 产品体验升级与可选增强 | P15.4 自动化与通知增强 | 评估 CI/CD、邮件、Webhook 和操作日志 | 计划中 |

## 2.2 开发留痕规则

后续所有阶段都必须保证“计划可追溯、过程可解释、结果可验证”。本文件作为开发留痕主索引，其他文档作为专项说明。

执行规则：

1. 开始大版本或小版本前，先在 `docs/tasks.md` 新增或更新对应计划。
2. 涉及需求变化，同步 `docs/requirements.md`。
3. 涉及架构、部署、依赖或技术选型变化，同步 `docs/architecture.md` 和 `README.md`。
4. 涉及接口变化，同步 `docs/api.md`。
5. 涉及表结构、索引或初始化数据变化，同步 `docs/database.md`。
6. 涉及页面、导航、交互或产品表达变化，同步 `docs/ui.md`。
7. 完成小版本后，在对应小版本下补充验证结果、遗留问题和下一步建议。
8. 不删除历史阶段记录；如果计划调整，保留原计划并补充“调整说明”。

小版本记录模板：

```text
### PX.Y 小版本名称

目标：
- 说明本小版本要解决的问题。

范围：
- 说明本小版本会改哪些模块、文档或页面。

验收标准：
- 说明如何判断完成。

验证记录：
- 记录运行过的测试、构建、Docker 命令或人工验证结果。

调整说明：
- 如果范围变化，记录变化原因和影响。
```

### 2.2.1 Skills 执行规范

本项目后续执行必须同时遵守 `AGENTS.md` 和 Codex skills 工作流。`AGENTS.md` 负责项目业务边界和编码约束，skills 负责“先规格、再计划、再任务、再实现、再验证、再留档”的过程规范。

适用规则：

1. 新功能或跨多文件变更先更新 `docs/spec.md` 或对应专题文档，再进入实现。
2. 重大架构、依赖、部署、AI、文件存储、权限边界变化必须在 `docs/decisions/` 新增或更新 ADR。
3. 每个新小版本必须使用任务模板记录 `Acceptance`、`Verify`、`Files`，避免只写“做了什么”。
4. 文档要解释“为什么这样做”，不是只复述代码和字段。
5. 每个阶段结束时记录验证证据：测试命令、构建命令、Docker 检查、人工冒烟或未验证原因。
6. 如果需求存在不确定性，先在文档的 `Open Questions` 或任务的“调整说明”中显式记录，不带着隐含假设开发。

新任务推荐模板：

```text
- [ ] Task: 说明本次要完成的独立任务
  - Acceptance: 可观察、可测试的完成条件
  - Verify: 具体验证命令或人工检查方式
  - Files: 预计触达的文件或目录
```

## 2.3 当前执行批次

当前处于 P0 文档与后端脚手架阶段，本轮优先完成后端基础工程，不进入具体业务模块。

本轮范围：

- 补充 `docs/tasks.md` 当前阶段说明。
- 创建 `backend/` Spring Boot 3.x + JDK 21 Maven 脚手架。
- 建立后端基础包结构和 `FixLedgerApplication`。
- 实现统一响应、统一异常、分页结构和 MyBatis Plus 基础配置。
- 补齐 `application.yml`、`application-dev.yml`、`application-test.yml` 和 `.env.example` 示例配置。

验证结果：

- 后端脚手架 Maven 测试已通过。
- 使用 JDK 21.0.11 与 Maven 3.9.15 完成验证。

验证结果：

- P1 后端认证与家庭空间 Maven 测试已通过。
- 当前测试覆盖注册默认家庭空间、登录令牌、密码错误、未认证拦截和家庭空间权限校验。

验证结果：

- P2 设备分类与设备档案 Maven 测试已通过。
- 当前测试覆盖分类增删改查约束、设备创建详情、分页筛选、状态流转、逻辑删除基础和家庭空间隔离。

暂不进入范围：

- 用户注册登录、JWT 过滤器和家庭空间业务实现。
- 设备、保修、耗材、维修等业务表和接口实现。
- 前端脚手架、Docker Compose、真实 Redis/MySQL 联调。

## 2.4 当前执行批次

当前进入 P1 认证与家庭空间阶段，本轮优先完成后端认证闭环和家庭空间数据隔离基础。

本轮范围：

- 创建 `sys_user`、`fl_family_space`、`fl_family_member` 对应 Entity、Mapper 和初始化 SQL。
- 实现密码加密、注册、登录、退出登录、获取当前用户接口。
- 实现 JWT 生成、解析、认证过滤器和当前用户上下文。
- 实现注册后自动创建默认家庭空间。
- 实现家庭空间列表、创建、修改、成员查询和家庭空间权限校验服务。
- 补充认证与家庭空间相关测试，并运行 Maven 验证。

验证结果：

- P1 后端认证与家庭空间 Maven 测试已通过。
- 当前测试覆盖注册默认家庭空间、登录令牌、密码错误、未认证拦截和家庭空间权限校验。

验证结果：

- P2 设备分类与设备档案 Maven 测试已通过。
- 当前测试覆盖分类增删改查约束、设备创建详情、分页筛选、状态流转、逻辑删除基础和家庭空间隔离。

暂不进入范围：

- Redis JWT 黑名单和刷新 Token。
- 家庭成员邀请、角色管理后台和复杂 RBAC。
- 设备分类、设备档案和前端页面实现。
## 2.5 当前执行批次

当前进入 P2 设备档案核心阶段，本轮优先完成设备分类和设备档案后端能力。

本轮范围：

- 创建 `fl_device_category`、`fl_device_asset` 对应 Entity、Mapper 和初始化 SQL。
- 实现设备分类列表、新增、修改、删除接口。
- 实现设备创建、分页查询、详情查询、修改、逻辑删除和状态流转接口。
- 所有设备相关接口复用 P1 家庭空间权限校验，确保 `familyId` 数据隔离。
- 补充设备分类和设备档案 Service / Controller 测试，并运行 Maven 验证。

验证结果：

- P2 设备分类与设备档案 Maven 测试已通过。
- 当前测试覆盖分类增删改查约束、设备创建详情、分页筛选、状态流转、逻辑删除基础和家庭空间隔离。

暂不进入范围：

- 保修、附件、耗材、维修等详情 Tab 的真实聚合数据。
- 默认设备分类初始化自动化。
- 前端设备页面实现。
## 2.6 当前执行批次

当前进入 P3 保修与附件阶段，本轮优先完成保修记录和本地附件管理后端能力。

本轮范围：

- 创建 `fl_warranty_record`、`fl_file_resource` 对应 Entity、Mapper 和初始化 SQL。
- 实现设备保修记录新增、查询、修改、删除和即将过保分页查询。
- 实现 `FileStorageService` 本地文件存储、文件上传、附件查询、下载和逻辑删除。
- 所有保修与附件接口复用 P1 家庭空间权限校验和 P2 设备归属校验。
- 补充保修与附件相关测试，并运行 Maven 验证。

验证结果：

- P3 后端保修与附件 Maven 测试已通过。
- 当前测试覆盖保修增删改查、即将过保分页、日期校验、家庭空间隔离、附件上传查询下载、文件类型校验和非成员下载拦截。

暂不进入范围：

- 对象存储、临时访问 URL 和文件预览。P9.7.1 已完成 RustFS 对象存储接入，临时访问 URL 和文件预览仍为后续增强。
- 保修提醒任务生成和 Redis 去重。
- 前端保修管理页、附件上传组件。
## 2.7 当前执行批次

当前进入 P4 耗材与维修阶段，本轮优先完成耗材周期、更换记录和维修状态流转后端能力。

本轮范围：

- 创建 `fl_consumable_item`、`fl_consumable_replace_record`、`fl_maintenance_record` 对应 Entity、Mapper 和初始化 SQL。
- 实现耗材新增、查询、修改、删除、记录更换和即将更换分页查询。
- 实现耗材更换后自动刷新 `lastReplacedDate` 与 `nextRemindDate`。
- 实现维修记录创建、分页查询、详情、修改、状态流转、删除和费用统计。
- 所有 P4 接口复用 P1 家庭空间权限校验和 P2 设备归属校验。
- 补充耗材与维修相关测试，并运行 Maven 验证。

验证结果：

- P4 后端耗材与维修 Maven 测试已通过。
- 当前测试覆盖耗材增删改查、下次提醒日期计算、即将更换查询、耗材更换记录、维修记录增删改查、维修状态流转、维修费用统计、家庭空间隔离和 P4 附件类型。
- 后端完整 Maven 测试已通过，当前共 52 个测试用例，失败 0、错误 0。

暂不进入范围：

- Redis 提醒任务生成、去重和通知记录。
- 前端耗材页、维修页和设备详情 Tab。
- AI 故障建议和维修总结。
## 2.8 当前执行批次

当前进入 P5 提醒与看板阶段，本轮优先完成后端提醒生成、提醒中心和首页统计能力。

本轮范围：

- 创建 `fl_reminder_task`、`fl_notification_record` 对应 Entity、Mapper 和初始化 SQL。
- 实现 `RedisService`、Redis Key 常量、提醒去重和首页统计缓存。
- 实现保修提醒扫描、耗材提醒扫描、提醒查询、未读数量、已读和忽略。
- 实现首页总览、设备分类分布、维修费用趋势和提醒日历接口。
- 所有 P5 接口复用 P1 家庭空间权限校验。
- 补充提醒去重、看板统计和 Controller 测试，并运行 Maven 验证。

验证结果：

- P5 后端提醒与看板 Maven 测试已通过。
- 当前测试覆盖 Redis 去重、保修提醒扫描、耗材提醒扫描、提醒查询、未读数量、已读/忽略、首页总览、分类分布、维修费用趋势和提醒日历。
- 后端完整 Maven 测试已通过，当前共 61 个测试用例，失败 0、错误 0。

暂不进入范围：

- 邮件、短信、Webhook 等真实外部通知渠道。
- 前端提醒中心、首页 ECharts 和提醒日历页面。
- AI Mock 与 AI 分析结果展示。
## 2.9 当前执行批次

当前进入 P6 AI 辅助阶段，本轮优先完成 Mock AI、AI 分析留痕和三个辅助接口。

本轮范围：

- 创建 `fl_ai_analysis` 对应 Entity、Mapper 和初始化 SQL。
- 定义 `AiClient` 接口、Mock AI Client 和 OpenAI Compatible Client 基础实现。
- 编写票据提取、故障排查建议和维修总结 Prompt 模板。
- 实现票据文本提取、故障排查建议和维修记录总结接口。
- AI 结果只返回建议并保存分析记录，不直接创建或覆盖设备、维修等核心数据。
- 补充 AI Mock、AI 分析记录和 Controller 测试，并运行 Maven 验证。

验证结果：

- P6 后端 AI 辅助 Maven 测试已通过。
- 当前测试覆盖 Mock 票据提取、AI 分析留痕、故障排查建议、故障建议关联维修记录、维修总结、无维修记录兜底、家庭空间权限和 AI Controller 认证。
- 后端完整 Maven 测试已通过，当前共 72 个测试用例，失败 0、错误 0。

暂不进入范围：

- 前端 AI 助手页和 AI 结果确认表单。
- 真实大模型 Key 联调和流式输出。
- OCR 图片识别、多轮对话和复杂异步 AI 任务。

## 2.10 当前执行批次

当前进入 P7 工程化完善阶段，本轮优先完成后端工程化闭环，保证后端、MySQL、Redis 和演示数据可以通过 Docker Compose 一键启动。

本轮范围：

- 更新 `docs/tasks.md` 当前阶段说明。
- 编写 `backend/Dockerfile` 和 `backend/.dockerignore`。
- 编写根目录 `docker-compose.yml`，编排 MySQL、Redis 和后端服务。
- 增加 `db/demo-data.sql` 演示数据，并支持通过 `SQL_INIT_MODE=always` 初始化。
- 更新 `.env.example`、`README.md`、`docs/database.md` 和 `docs/api.md` 的工程化说明。
- 运行后端 Maven 测试和 Docker Compose 配置校验。

暂不进入范围：

- 前端 Dockerfile 和前端服务编排已在 2.10 批次补齐。
- 生产级 HTTPS、域名、CI/CD 和日志采集平台。
- MinIO、邮件、Webhook 等二期增强部署。
## 2.11 当前执行批次

当前进入前端 MVP 页面阶段，本轮优先完成 Vue3 前端脚手架、基础布局、认证页面和核心业务页面骨架，并与已完成的后端接口保持路径一致。

本轮范围：

- 创建并整理 `frontend/` Vue3 + TypeScript + Vite 工程。
- 配置 Vue Router、Pinia、Axios、Element Plus、ECharts。
- 实现登录页、注册页、主布局、路由守卫和用户 Store。
- 实现首页看板、设备档案、保修管理、耗材管理、维修记录、维修详情、提醒中心、附件库、AI 助手和家庭设置页面。
- 抽取 API 请求封装和 TypeScript 类型，页面不直接拼接 Axios URL。
- 表格页保留分页、筛选、加载状态；关键写操作保留二次确认或表单校验。
- 运行前端构建验证。

实现顺序：

1. 先补充本任务文档，明确本轮前端页面范围。
2. 补齐缺失页面和路由引用，避免构建时出现空路由。
3. 补齐前端 API 方法与后端 P1-P6 接口对应关系。
4. 对核心页面执行 `npm run build` 验证。

暂不进入范围：

- 前端端到端测试、复杂图表深度交互和移动端专项适配。
- 生产级 Nginx HTTPS、域名和 CI/CD 发布流水线。
- 真实文件预览、OCR、多轮 AI 对话等二期增强能力。

## 2.12 当前执行批次

当前补齐 Docker 一键启动完整项目能力，本轮目标是让面试演示环境可以通过一条 Compose 命令启动前端、后端、MySQL 和 Redis。

本轮范围：

- 先更新 `docs/tasks.md` 和 `README.md`，明确 Docker 一键启动范围。
- 补充 `frontend/Dockerfile`、`frontend/nginx.conf` 和 `frontend/.dockerignore`。
- 调整 `docker-compose.yml`，默认编排 MySQL、Redis、后端和前端，不再依赖额外 profile。
- 精简 `backend/Dockerfile`，避免运行镜像构建阶段额外安装 curl。
- 补充 `.env.example` 的前端端口示例。
- 运行 Docker Compose 配置校验。

验证结果：

- docker compose config --quiet 已通过。
- docker compose config --services 已确认默认服务包含 mysql、redis、backend、frontend。
- docker compose up -d --build 已进入前后端镜像构建阶段，但当前本机 Docker Hub token 请求超时，需配置镜像加速或代理后重试。

暂不进入范围：

- 生产级 HTTPS、域名、CI/CD 和日志采集平台。
- Docker Hub 镜像源、代理和加速器的本机配置。
- MinIO、邮件、Webhook 等二期增强部署。

## 2.13 当前执行批次

当前进入 P8 产品化体验重构阶段。本轮不再做局部字段和图标微调，而是把前端从“管理平台信息架构”调整为“家庭设备管家场景入口”。

本轮范围：

- 先更新 `docs/ui.md` 和 `docs/tasks.md`，明确 P8 场景化重构方向。
- 重构 `MainLayout.vue`，将一级导航调整为“我的家 / 家庭日历 / 设备护照 / 凭证盒 / 智能助手 / 我的家庭”。
- 首页继续复用现有后端接口，但页面表达从“首页看板”升级为“我的家”。
- 首页新增家庭健康分、本周设备事项和房间设备概览。
- 保留家庭提醒日历、分类分布、维修费用趋势等已有能力，但弱化后台统计感。
- 运行前端构建验证并重建前端 Docker 容器。

暂不进入范围：

- 新增后端聚合接口，先用现有接口在前端组合。
- 设备详情完整改造成设备护照。
- 凭证完整度、索赔准备度和维修值不值得等二期产品亮点。

## 2.14 当前执行批次

当前正式进入 P9 系统性完善阶段。本阶段不再优先堆新功能，而是把已经完成的 MVP 打磨成适合面试展示、结构一致、质量可靠、体验清晰的项目。

本轮范围：

- 先在 `docs/tasks.md` 固化 P9-P15 后续完善路线。
- 以 P9 为总阶段，按“文档对齐 → 代码质量 → 测试补强 → 安全边界 → 演示包装 → 产品体验”的顺序推进。
- 每次涉及需求、接口、表结构、页面或部署说明变化时，先同步 `docs/` 对应文档。
- 保留 P0-P8 开发记录，用于面试讲解项目从 MVP 到系统性完善的演进过程。

验收标准：

- 后续任务有清晰分期、目标、范围和验收标准。
- `docs/tasks.md` 能直接回答“项目完成后还会如何系统性打磨”。
- 不重开文档体系，继续维护现有 `docs/` 和 `README.md`。

暂不进入范围：

- 暂不新增大业务模块。
- 暂不引入商城、企业资产盘点等偏离家庭设备生命周期的方向。
- 暂不把 AI 作为核心业务依赖。
## 3. P0 文档与脚手架
### P0.1 完善项目文档

- [x] 编写 `README.md`。
- [x] 编写 `AGENTS.md`。
- [x] 编写 `docs/requirements.md`。
- [x] 编写 `docs/architecture.md`。
- [x] 编写 `docs/database.md`。
- [x] 编写 `docs/api.md`。
- [x] 编写 `docs/ui.md`。
- [x] 编写 `docs/tasks.md`。

验收标准：

- 文档之间项目定位一致。
- 技术栈一致为 JDK 21 + Spring Boot 3.x + Vue3。
- 核心模块和表结构一致。

### P0.2 后端脚手架

- [x] 创建 `backend/` Maven 项目。
- [x] 配置 JDK 21。
- [x] 引入 Spring Boot 3.x。
- [x] 引入 Spring Web。
- [x] 引入 Spring Security。
- [x] 引入 MyBatis Plus。
- [x] 引入 MySQL Driver。
- [x] 引入 Redis。
- [x] 引入 Validation。
- [x] 引入 Lombok。
- [x] 引入 MapStruct。
- [x] 引入 Knife4j 或 SpringDoc OpenAPI。
- [x] 创建基础包结构。
- [x] 创建 `FixLedgerApplication`。

验收标准：

- 后端可以启动。
- `/actuator/health` 或基础健康接口可访问。
- Knife4j / OpenAPI 页面可访问。

### P0.3 前端脚手架

- [x] 创建 `frontend/` Vue3 + Vite 项目。
- [x] 配置 TypeScript。
- [x] 引入 Element Plus。
- [x] 引入 Pinia。
- [x] 引入 Vue Router。
- [x] 引入 Axios。
- [x] 引入 ECharts。
- [x] 配置基础布局。
- [x] 配置路由守卫。

验收标准：

- 前端可以启动。
- 登录页、首页空页面可访问。
- API 请求封装可用。

### P0.4 基础设施配置

- [x] 编写 `.env.example`。
- [x] 编写 `application.yml`。
- [x] 编写 `application-dev.yml`。
- [x] 编写 `application-test.yml`。
- [x] 编写 `.gitignore`。
- [x] 编写 Docker Compose 草稿。

验收标准：

- 配置中不包含真实密钥。
- 本地开发配置清晰。

## 4. P1 认证与家庭空间

### P1.1 通用后端基础

- [x] 实现 `Result<T>`。
- [x] 实现 `ErrorCode`。
- [x] 实现 `BusinessException`。
- [x] 实现 `GlobalExceptionHandler`。
- [x] 实现分页请求和分页响应模型。
- [x] 实现基础审计字段自动填充。

验收标准：

- 所有接口统一返回 `Result<T>`。
- 业务异常不会返回堆栈给前端。

### P1.2 用户认证

- [x] 创建用户相关表。
- [x] 实现密码加密。
- [x] 实现注册接口。
- [x] 实现登录接口。
- [x] 实现 JWT 生成与解析。
- [x] 实现认证过滤器。
- [x] 实现退出登录。
- [x] 实现获取当前用户接口。

验收标准：

- 注册后可登录。
- 未登录不能访问受保护接口。
- 密码不明文存储。

### P1.3 家庭空间

- [x] 创建家庭空间表和成员表。
- [x] 注册后自动创建默认家庭空间。
- [x] 查询家庭空间列表。
- [x] 创建家庭空间。
- [x] 修改家庭空间。
- [x] 实现家庭空间权限校验。

验收标准：

- 用户只能访问自己所属家庭空间。
- 后续所有业务接口都可以复用家庭空间校验。

### P1.4 前端认证页面

- [x] 登录页。
- [x] 注册页。
- [x] 用户 Store。
- [x] Token 存储。
- [x] 路由守卫。
- [x] 顶部用户信息。
- [x] 家庭空间切换入口。

验收标准：

- 登录成功跳转首页。
- 未登录访问首页跳转登录。

## 5. P2 设备档案核心

### P2.1 设备分类

- [x] 创建设备分类表。
- [ ] 新家庭空间初始化默认分类。
- [x] 查询分类列表。
- [x] 新增分类。
- [x] 修改分类。
- [x] 删除分类。

验收标准：

- 分类名称在家庭空间内唯一。
- 分类下存在设备时不能删除。

### P2.2 设备档案后端

- [x] 创建设备档案表。
- [x] 创建设备接口。
- [x] 分页查询设备。
- [x] 查询设备详情。
- [x] 修改设备。
- [x] 删除设备。
- [x] 修改设备状态。
- [x] 实现设备详情聚合 DTO。

验收标准：

- 设备必须归属于家庭空间。
- 设备列表支持关键词、分类、状态筛选。
- 删除为逻辑删除。

### P2.3 设备档案前端

- [x] 设备列表页。
- [x] 设备筛选区。
- [x] 新增设备页。
- [x] 编辑设备页。
- [x] 设备详情页基础信息。
- [x] 设备状态标签。

验收标准：

- 可以完成设备增删改查。
- 设备详情页能展示基础信息。

## 6. P3 保修与附件

### P3.1 保修后端

- [x] 创建保修记录表。
- [x] 创建设备保修记录。
- [x] 查询设备保修记录。
- [x] 修改保修记录。
- [x] 删除保修记录。
- [x] 查询即将过保设备。
- [x] 校验保修日期。

验收标准：

- 保修结束日期不能早于开始日期。
- 即将过保查询准确。

### P3.2 文件附件后端

- [x] 创建文件资源表。
- [x] 实现 `FileStorageService` 接口。
- [x] 实现本地文件存储。
- [x] 实现文件上传接口。
- [x] 实现附件查询接口。
- [x] 实现附件下载接口。
- [x] 实现附件删除接口。
- [x] 校验文件大小和类型。

验收标准：

- 可以上传发票、保修卡、说明书。
- 非家庭成员不能下载附件。

### P3.3 保修与附件前端

- [x] 保修管理页。
- [x] 设备详情页保修 Tab。
- [x] 附件上传组件。
- [x] 附件列表组件。
- [x] 附件下载。

验收标准：

- 设备详情页可以查看保修和附件。
- 可以上传并下载附件。

## 7. P4 耗材与维修

### P4.1 耗材后端

- [x] 创建耗材项表。
- [x] 创建耗材更换记录表。
- [x] 新增耗材项。
- [x] 查询设备耗材。
- [x] 修改耗材项。
- [x] 删除耗材项。
- [x] 记录耗材更换。
- [x] 自动计算下次提醒日期。
- [x] 查询即将更换耗材。

验收标准：

- 更换耗材后更新 `lastReplacedDate` 和 `nextRemindDate`。
- 即将更换耗材查询准确。

### P4.2 维修后端

- [x] 创建维修记录表。
- [x] 创建维修记录。
- [x] 分页查询维修记录。
- [x] 查询维修详情。
- [x] 修改维修记录。
- [x] 维修状态流转。
- [x] 删除维修记录。
- [x] 维修费用统计。

验收标准：

- 状态只能按规则流转。
- 已取消维修不计入费用统计。
- 维修完成后可更新设备状态。

### P4.3 耗材与维修前端

- [x] 耗材管理页。
- [x] 耗材更换弹窗。
- [x] 设备详情页耗材 Tab。
- [x] 维修记录页。
- [x] 维修详情页。
- [x] 设备详情页维修 Tab。
- [x] 维修状态流转弹窗。

验收标准：

- 可以管理设备耗材。
- 可以创建并完成维修记录。

## 8. P5 提醒与看板

### P5.1 Redis 基础

- [x] 实现 `RedisService`。
- [x] 定义 Redis Key 常量。
- [ ] 接入验证码缓存（二期可选）。
- [x] 接入提醒去重。
- [x] 接入首页统计缓存。

验收标准：

- Redis Key 有统一前缀。
- 所有临时 Key 有 TTL。

### P5.2 提醒任务

- [x] 创建提醒任务表。
- [x] 创建通知记录表。
- [x] 实现保修提醒扫描任务。
- [x] 实现耗材提醒扫描任务。
- [x] 实现提醒去重。
- [x] 查询提醒列表。
- [x] 未读提醒数量。
- [x] 标记已读。
- [x] 忽略提醒。
- [x] 手动触发扫描接口。

验收标准：

- 同一事项同一天不重复提醒。
- 提醒生成失败不影响业务数据。

### P5.3 首页看板

- [x] 首页总览接口。
- [x] 设备分类分布接口。
- [x] 维修费用趋势接口。
- [x] 提醒日历接口。
- [x] 首页看板页面。
- [x] ECharts 图表。
- [x] 最近提醒列表。
- [x] 最近维修记录。

验收标准：

- 首页能展示设备、提醒、维修、费用核心数据。
- 统计按家庭空间隔离。

## 9. P6 AI 辅助

### P6.1 AI 基础设施

- [x] 定义 `AiClient` 接口。
- [x] 实现 `MockAiClient`。
- [x] 实现 OpenAI Compatible Client。
- [x] 创建 AI 配置类 `AiProperties`。
- [x] 创建 Prompt 模板目录。
- [x] 创建 AI 分析结果表。

验收标准：

- 未配置真实 API Key 时使用 Mock。
- AI 调用失败不会影响核心业务。

### P6.2 发票文本提取

- [x] 编写 Prompt。
- [x] 实现发票文本提取接口。
- [x] 保存 AI 分析结果。
- [x] 前端展示可编辑结果。
- [x] 支持填入新增设备表单。

验收标准：

- AI 结果必须用户确认后才保存。

### P6.3 故障排查建议

- [x] 编写 Prompt。
- [x] 根据设备上下文生成建议。
- [x] 保存 AI 分析结果。
- [x] 前端维修详情页展示。

验收标准：

- AI 失败返回兜底提示。
- 故障建议可关联维修记录。

### P6.4 维修总结

- [x] 编写 Prompt。
- [x] 查询设备维修历史。
- [x] 生成维护总结。
- [x] 设备详情页展示。

验收标准：

- 没有维修记录时返回友好提示。

## 10. P8 产品化体验重构

### P8.1 主导航场景化

- [x] 将首页看板改为“我的家”。
- [x] 将设备档案改为“设备护照”。
- [x] 将附件库改为“凭证盒”。
- [x] 将 AI 助手改为“智能助手”。
- [x] 保修、耗材、维修、提醒弱化为场景内入口或高级记录。

验收标准：

- 第一眼不再像普通后台菜单。
- 一级入口围绕家庭、日历、设备、凭证和助手组织。

### P8.2 我的家首页

- [x] 家庭健康分。
- [x] 本周设备事项。
- [x] 家庭提醒日历。
- [x] 房间设备概览。
- [x] 保留辅助统计图表。

验收标准：

- 首页回答“这个家现在怎么样”和“本周要处理什么”。
- 后台统计感弱化，家庭工具感增强。

### P8.3 设备护照

- [ ] 设备列表默认改成按房间分组的卡片墙。
- [ ] 设备详情顶部改成设备护照摘要。
- [ ] 聚合保修、耗材、维修、附件为生命周期时间线。

### P8.4 凭证盒

- [ ] 附件库改名为凭证盒。
- [ ] 按发票、保修卡、说明书、维修单分类展示。
- [ ] 设计凭证完整度。
## 11. P7 工程化完善

### P7.1 测试

- [x] 用户认证 Service 测试。
- [x] 家庭空间权限测试。
- [x] 设备档案 Service 测试。
- [x] 保修日期校验测试。
- [x] 耗材下次提醒日期计算测试。
- [x] 维修状态流转测试。
- [x] 提醒去重测试。
- [x] AI Mock 测试。

验收标准：

- 核心业务测试通过。
- AI 测试不依赖真实服务。

### P7.2 Docker 与部署

- [x] 编写后端 Dockerfile。
- [x] 编写前端 Dockerfile。
- [x] 编写 docker-compose.yml。
- [x] 编排 MySQL、Redis、后端和前端；P9.7.1 补充 RustFS。
- [x] 补充部署说明。

验收标准：

- 可以一键启动完整项目。

### P7.3 演示数据

- [x] 初始化默认用户。
- [x] 初始化默认家庭空间。
- [x] 初始化设备分类。
- [x] 初始化示例设备。
- [x] 初始化保修记录。
- [x] 初始化耗材和维修记录。

验收标准：

- 新环境启动后可以直接演示核心流程。

### P7.4 文档收尾

- [x] 更新 README 快速开始。
- [x] 更新 README 效果展示。
- [x] 更新接口文档。
- [x] 更新数据库文档。
- [x] 更新任务完成状态。

## 12. P9 系统性完善阶段

P9 是项目完成 MVP 之后的系统性打磨阶段，目标是让 FixLedger 不只是“能跑”，而是做到文档一致、代码可靠、测试可证明、演示顺畅、产品表达清晰。

### P9.1 文档与实现对齐

- [x] 梳理 `docs/`、`README.md` 与当前代码实现的差异。
- [x] 更新 `docs/requirements.md`，确认家庭设备生命周期管理的核心需求边界。
- [x] 更新 `docs/architecture.md`，补齐 Docker、AI Client、文件存储、Redis 和定时任务说明。
- [x] 更新 `docs/api.md`，核对接口路径、认证方式、分页规则、错误码和响应结构。
- [x] 更新 `docs/database.md`，核对表结构、索引、初始化数据和核心字段含义。
- [x] 更新 `docs/ui.md`，同步“我的家 / 家庭日历 / 设备护照 / 凭证盒 / 智能助手”产品表达。
- [x] 更新 `README.md`，补充项目介绍、技术栈、Docker 启动、演示账号和面试展示路径。

目标：

- 让项目说明、真实代码、数据库脚本、接口实现、页面导航和 Docker 启动方式保持一致。

范围：

- 已对齐 `docs/requirements.md`、`docs/architecture.md`、`docs/api.md`、`docs/database.md`、`docs/ui.md`、`docs/tasks.md` 和 `README.md`。

验收标准：

- 文档与当前实现不冲突。
- 面试时可以按文档讲清需求、架构、接口、数据库和页面设计。

验证记录：

- 已读取后端 Controller 路由、前端路由、Docker Compose、应用配置、数据库初始化脚本和演示数据脚本。
- 已执行 `git diff --check` 校验文档格式。

调整说明：

- 系统管理员、RBAC、操作日志、家庭成员邀请、邮件/Webhook 和 Refresh Token 明确调整为后续增强。
- P9.1 时文件存储仍对齐为本地文件系统；P9.7.1 已推进 RustFS 对象存储接入。
- 首页表达对齐为“我的家”，但路由仍保持 `/dashboard`。

### P9.2 代码质量治理

- [ ] 扫描并处理不符合规范的 `RuntimeException`、裸 `Map` 返回、直接返回 Entity 等问题。
- [ ] 检查 Controller、Service、Mapper、Infrastructure 分层职责是否清晰。
- [ ] 统一 `BusinessException`、`ErrorCode` 和 `GlobalExceptionHandler` 使用方式。
- [ ] 检查分页、参数校验、文件校验和前后端请求封装是否一致。
- [ ] 检查日志是否结构化，且不输出密码、Token、API Key 等敏感信息。
- [ ] 检查事务边界，避免在事务中调用 AI、文件存储、邮件或 Webhook 等外部能力。

验收标准：

- 代码符合 `AGENTS.md` 分层、异常、事务、日志和命名规范。
- 关键业务方法有必要注释，但不堆砌无意义注释。


#### P9.2.1 代码质量基线扫描与首批修复

目标：

- 先建立代码质量治理基线，找出最容易影响面试评价和后续维护的问题，并优先修复风险明确、改动范围小的问题。

为什么先做这一步：

- MVP 完成后，项目已经从“能跑”进入“能讲清、能维护、能证明”的阶段。
- 面试官常问的不是“有没有增删改查”，而是异常怎么统一、事务放在哪里、权限怎么兜底、日志会不会泄露敏感信息。
- 先做小范围基线扫描，可以避免后续继续在不规范代码上叠功能。

任务拆分：

- [x] Task: 扫描异常和返回值规范
  - Acceptance: 找出 `RuntimeException`、裸 `Map` 返回、Controller 直接返回 Entity 或绕过 `Result<T>` 的位置，并形成处理结论。
  - Verify: 使用 `rg` 扫描关键模式；人工复核 Controller 和异常处理路径。
  - Files: `backend/src/main/java`。
- [x] Task: 扫描事务和外部调用边界
  - Acceptance: 找出 `@Transactional` 是否只放在 Service 层，事务内是否存在 AI、文件存储、通知等外部调用风险。
  - Verify: 使用 `rg` 扫描 `@Transactional`、`FileStorageService`、`AiClient`、`Notification` 调用点；人工复核调用链。
  - Files: `backend/src/main/java`。
- [x] Task: 扫描日志和敏感信息风险
  - Acceptance: 找出 `printStackTrace`、`System.out`、日志直接输出密码/Token/API Key 或丢失堆栈的问题。
  - Verify: 使用 `rg` 扫描日志和敏感关键词；人工确认是否为真实风险。
  - Files: `backend/src/main/java`、`frontend/src`。
- [x] Task: 修复首批低风险高价值问题
  - Acceptance: 只修复边界明确、不会扩大范围的问题；不混入 RustFS 未提交实现和新功能。
  - Verify: 后端执行 `mvn test`，如涉及前端再执行 `npm run build`。
  - Files: `backend/src/main/java/com/fixledger/common/exception/GlobalExceptionHandler.java`、`backend/src/test/java/com/fixledger/common/exception/GlobalExceptionHandlerTest.java`、`docs/api.md`、`docs/tasks.md`。

扫描结论：

- 异常规范：未发现业务代码直接 `throw new RuntimeException(...)`；`BusinessException extends RuntimeException` 属于 Spring 事务回滚所需的受控业务异常基类。
- 返回值规范：Controller 基本统一返回 `Result<T>`；`FileResourceController.downloadFile()` 使用 `ResponseEntity<Resource>` 是合理例外，因为文件下载成功响应是二进制流，不能再包一层 JSON。
- 事务边界：`@Transactional` 当前集中在 Service 层；AI 调用未放入事务；附件上传方法未加事务，避免在数据库事务中调用 RustFS/S3 这类外部存储。
- 后续风险：`ReminderServiceImpl.scanFamily(...)` 在事务内使用 Redis 去重，当前可接受，但后续 P9.2.2 应评估是否把 Redis 去重与数据库写入边界拆得更清晰。
- 日志与敏感信息：未发现 `System.out`、`System.err`、`printStackTrace` 或明显输出密码、Token、API Key 的日志；`ReminderScheduler` 捕获 `Exception` 后记录完整堆栈是为了单个家庭扫描失败不影响其他家庭。

首批修复：

- 将 `GlobalExceptionHandler` 中 `BusinessException` 的 HTTP 状态从固定 `400` 调整为按 `ErrorCode` 语义映射。
- 新增 `GlobalExceptionHandlerTest`，验证 `BAD_REQUEST -> 400`、`FORBIDDEN -> 403`、`DEVICE_NOT_FOUND -> 404`、`AI_SERVICE_UNAVAILABLE -> 503`。
- 在 `docs/api.md` 补充错误响应契约，明确业务错误码和 HTTP 状态是两层含义。

验收标准：

- P9.2.1 至少输出一份代码质量问题清单和处理结论。
- 首批修复后项目测试通过，且没有引入新的业务功能。
- 能形成面试口径：为什么统一异常、为什么事务只放 Service、为什么外部服务不能放事务里、为什么日志不能输出敏感信息。

验证记录：

- 已执行关键模式扫描：`RuntimeException`、`throw new Exception`、`catch (Exception)`、`printStackTrace`、`System.out`、`ResponseEntity`、`Map`、`@Transactional`、`FileStorageService`、`AiClient`、`Notification`、敏感关键词。
- 已执行 `mvn -q -Dtest=GlobalExceptionHandlerTest test`。
- 已执行 `mvn test -q`，后端测试共 73 个，失败 0，错误 0，跳过 0。

调整说明：

- 当前工作区仍有 RustFS 相关未提交代码，本小版本不修改这些未提交文件，避免把“文件存储增强”和“代码质量治理”混成一个不可审查的大改动。
- 本小版本只完成首轮代码质量基线和异常契约修复，后续 P9.2.2 继续处理事务边界、提醒去重和注释质量。

#### P9.2.2 提醒扫描事务边界治理

目标：

- 将提醒扫描从“大事务包住扫描、Redis 去重和写库”调整为“扫描和 Redis 去重不占用数据库事务，只在创建提醒和站内通知时开启最小事务”。

为什么做这一步：

- 提醒扫描属于定时/批处理逻辑，可能一次扫描多个保修和耗材记录，如果整个扫描过程都放在事务中，会长时间占用数据库连接。
- Redis 去重是外部基础设施能力，作用是降低重复扫描和并发插入风险，不应该被数据库事务包住。
- 真正需要原子性的部分是同一条提醒和对应站内通知的数据库写入，因此事务应该缩小到这两个写库动作。

任务拆分：

- [x] Task: 拆分提醒扫描事务边界
  - Acceptance: `scanFamily(...)` 不再整体加 `@Transactional`；Redis 去重发生在创建提醒事务之前。
  - Verify: 阅读 `ReminderServiceImpl` 调用链，确认 `RedisService.setIfAbsent` 不在扫描大事务中执行。
  - Files: `backend/src/main/java/com/fixledger/modules/reminder/service/ReminderServiceImpl.java`。
- [x] Task: 保留提醒和通知写库原子性
  - Acceptance: 创建提醒任务和站内通知仍在同一个数据库事务内，避免只写提醒不写通知。
  - Verify: 使用测试覆盖数据库写入失败时 Redis 去重键会释放，避免失败后长时间误判重复。
  - Files: `backend/src/main/java/com/fixledger/modules/reminder/service/ReminderCreationService.java`、`backend/src/test/java/com/fixledger/modules/reminder/ReminderServiceTransactionBoundaryTest.java`。
- [x] Task: 回归提醒核心测试
  - Acceptance: 原有提醒扫描、去重、未读、已读、忽略等行为保持不变。
  - Verify: 执行提醒相关测试和后端完整测试。
  - Files: `backend/src/test/java/com/fixledger/modules/reminder`。

验收标准：

- Redis 去重不在提醒扫描大事务中执行。
- 提醒任务和站内通知写库仍保持同事务提交或回滚。
- Redis 去重键在数据库写入失败时释放，允许后续扫描重试。
- 原有提醒测试通过，后端完整测试通过。

验证记录：

- 已移除 `ReminderServiceImpl.scanFamily(...)` 上的大事务，扫描查询和 Redis 去重不再整体占用数据库事务。
- 已新增 `ReminderCreationService.createReminderIfAbsent(...)`，只把提醒任务和站内通知写入放入同一个数据库事务。
- 已新增 `ReminderServiceTransactionBoundaryTest`，覆盖扫描入口无大事务、写库失败释放 Redis 去重键、Redis 命中去重时不进入写库事务。
- 已执行 `mvn -q -Dtest=ReminderServiceTransactionBoundaryTest test`。
- 已执行 `mvn -q '-Dtest=ReminderServiceTest,ReminderServiceTransactionBoundaryTest' test`。
- 已执行 `mvn test -q`，后端测试共 76 个，失败 0，错误 0，跳过 0。

面试口径：

- 可以说明“事务不是越大越安全”，事务应该只包住必须保证原子性的数据库写入；Redis、AI、文件存储、通知渠道等外部能力要和核心数据库事务解耦。

#### P9.2.3 日志、敏感信息与认证退出治理

目标：

- 把 P9.2 的质量治理继续往安全和可运维方向收口，重点处理开发日志过细、退出登录未使 Token 失效、请求错误信息过度暴露、上传文件名安全等问题。

为什么做这一步：

- 面试时“登录退出是否真的退出”“日志会不会打出密码或 Token”“异常是否会暴露内部实现”都是高频追问。
- JWT 是无状态令牌，单纯前端删除 Token 只是不再携带令牌，并不能让已经签发的 Token 立即失效；需要后端黑名单兜底。
- 开发日志过细会把 SQL 参数、密码哈希、AI 分析内容等写入日志，不利于安全和演示。

任务拆分：

- [x] Task: 收敛开发与测试日志级别
  - Acceptance: 默认 dev/test 不再输出 MyBatis SQL 参数级 debug 日志，避免密码哈希、AI 内容和业务参数长期落日志。
  - Verify: 检查 `application-dev.yml`、`application-test.yml` 的日志级别；执行后端测试确认不影响功能。
  - Files: `backend/src/main/resources/application-dev.yml`、`backend/src/main/resources/application-test.yml`。
- [x] Task: 实现 JWT 退出黑名单
  - Acceptance: 调用退出登录后，同一个 JWT 再访问受保护接口返回未认证；黑名单 Key 使用 `fixledger:` 前缀并设置 TTL。
  - Verify: 补充 Controller 测试覆盖登录、退出、旧 Token 失效。
  - Files: `backend/src/main/java/com/fixledger/common/security`、`backend/src/main/java/com/fixledger/modules/auth`、`backend/src/test/java/com/fixledger/modules/auth`。
- [x] Task: 请求错误和文件名安全收口
  - Acceptance: JSON 解析、参数类型错误等响应不暴露底层异常细节；上传文件名拒绝路径穿越字符。
  - Verify: 补充异常处理和文件上传安全测试。
  - Files: `backend/src/main/java/com/fixledger/common/exception/GlobalExceptionHandler.java`、`backend/src/main/java/com/fixledger/modules/file/service/FileResourceServiceImpl.java`。

验收标准：

- 退出登录后的 Token 不可继续访问接口。
- Redis 黑名单 Key 集中定义，TTL 不超过 Token 剩余有效期。
- 日志不输出密码、Token、API Key、完整请求体或过细 SQL 参数。
- 错误响应对前端友好，但不暴露后端内部异常堆栈或解析细节。

面试口径：

- 可以说明“JWT 天然无状态，退出登录如果只靠前端删除 Token，旧 Token 在过期前仍可用；因此后端用 Redis 黑名单保存 Token 指纹，并按剩余有效期设置 TTL”。

验证记录：

- 已将 dev/test 日志级别从 `debug` 收敛到 `info`，避免 SQL 参数和业务内容在本地默认日志中过度输出。
- 已新增 JWT `jti`，并通过 `JwtBlacklistService` 将退出登录后的 Token ID 写入 Redis 黑名单，TTL 使用令牌剩余有效期。
- 已将请求解析异常统一返回“请求参数格式不正确”，避免把 JSON 解析细节或类型转换细节暴露给前端。
- 已在附件上传中拒绝包含 `..`、`/`、`\` 的文件名，降低路径穿越风险。
- 已执行 `mvn -q '-Dtest=AuthControllerTest,FileResourceServiceTest,GlobalExceptionHandlerTest' test`，目标测试通过。
- 已执行 `mvn test -q`，后端测试共 80 个，失败 0，错误 0，跳过 0。
### P9.3 测试体系补强

目标：

- 不是追求测试数量，而是补齐最能证明项目质量的风险测试：认证退出、家庭空间越权、附件安全、提醒去重、状态流转和异常契约。

任务拆分：

- [x] Task: 认证与安全测试补强
  - Acceptance: 覆盖未登录、登录成功、退出后旧 Token 失效、无效 Token 不能访问受保护接口。
  - Verify: `mvn -q -Dtest=AuthControllerTest test`。
  - Files: `backend/src/test/java/com/fixledger/modules/auth/AuthControllerTest.java`。
- [x] Task: 附件与文件安全测试补强
  - Acceptance: 覆盖非法扩展名、非法 MIME、路径穿越文件名、非家庭成员下载附件。
  - Verify: `mvn -q -Dtest=FileResourceServiceTest test`。
  - Files: `backend/src/test/java/com/fixledger/modules/file/FileResourceServiceTest.java`。
- [x] Task: 全量质量门禁固化
  - Acceptance: 后端完整测试、前端构建和 Docker Compose 配置校验有固定命令，并在 P9 收尾记录结果。
  - Verify: `mvn test`、`npm run build`、`docker compose config --quiet`。
  - Files: `docs/tasks.md`、`README.md`。

验收标准：

- 核心业务规则有测试证明。
- 面试时可以说明测试覆盖了哪些关键风险：认证、权限、文件、安全、提醒和状态流转。

验证记录：

- 已补充并回归认证安全测试：未登录、登录成功、退出后旧 Token 失效、无效 Token 拦截。
- 已补充并回归附件安全测试：非法扩展名、非法 MIME、路径穿越文件名、非家庭成员下载附件。
- 已确认现有测试覆盖家庭空间越权、提醒 Redis 去重、提醒事务边界、设备状态流转、维修状态流转和费用统计排除取消记录。
- 已新增 `docs/security-test-review.md`，汇总测试覆盖点、安全审查结论和后续延期项。
- 已执行 `mvn -q '-Dtest=AuthControllerTest,FileResourceServiceTest,GlobalExceptionHandlerTest' test`，目标测试通过。
- 已执行 `mvn test -q`，Surefire 报告汇总 80 个测试，失败 0，错误 0，跳过 0。

面试口径：

- 可以说明“测试不是为了凑数量，而是证明认证、权限、文件上传、提醒去重和状态流转这些高风险规则不会回归”。

### P9.4 安全与数据隔离

目标：

- 将 P9.2 与 P9.3 已发现和已覆盖的风险收敛为安全审查结论，保证项目能讲清“认证、授权、数据隔离、文件安全、日志脱敏”。

任务拆分：

- [x] Task: 家庭空间权限审查
  - Acceptance: 设备、保修、耗材、维修、提醒、附件、AI 和看板接口均通过 `familyId` 校验当前用户是否为家庭成员。
  - Verify: 使用 `rg` 和人工复核 Service 调用链；关键越权路径已有测试覆盖。
  - Files: `backend/src/main/java/com/fixledger/modules`。
- [x] Task: 文件上传与下载安全审查
  - Acceptance: 文件大小、扩展名、MIME、业务对象归属、附件下载权限和逻辑删除均有约束。
  - Verify: 文件服务测试通过；接口仍由后端鉴权后转发下载。
  - Files: `backend/src/main/java/com/fixledger/modules/file`。
- [x] Task: JWT 与敏感信息审查
  - Acceptance: 密码只存哈希；登录响应不返回密码哈希；退出登录进入 Redis 黑名单；日志不打印 Token/API Key。
  - Verify: 认证测试通过；敏感关键词扫描无真实密钥。
  - Files: `backend/src/main/java/com/fixledger/common/security`、`backend/src/main/java/com/fixledger/modules/auth`。

验收标准：

- 不同家庭用户不能越权访问设备、保修、耗材、维修、提醒和附件。
- 敏感信息不进入接口响应和日志。
- 安全边界能形成可复述的面试答案。

验证记录：

- 已使用 Service 实现扫描确认：除账号级认证模块外，`asset`、`warranty`、`consumable`、`maintenance`、`reminder`、`dashboard`、`file`、`ai` 等模块均执行家庭成员校验或 family_id 归属查询。
- 已复核附件服务：上传前校验家庭成员、业务类型、业务对象归属、文件大小、扩展名、MIME 和路径字符；下载前再次按 `familyId + fileId` 查询。
- 已复核认证链路：JWT 使用 jti，退出登录写入 Redis 黑名单，过滤器识别黑名单后清空安全上下文。
- 已执行敏感关键词扫描，未发现日志直接输出密码、Token、API Key；`.env.example` 与 README 中仅存在示例值。
- 已将完整结论整理到 `docs/security-test-review.md`。

面试口径：

- 可以说明“权限不是靠前端控制，而是在后端 Service 层统一校验家庭成员，并且详情查询同时带上业务 ID 和 family_id，防止猜 ID 越权”。

### P9.5 演示体验与面试材料

目标：

- 把项目整理成“能运行、能演示、能解释”的面试项目，而不是只有代码的仓库。

任务拆分：

- [x] Task: 面试讲解文档
  - Acceptance: 新增面试指南，覆盖项目背景、技术栈、架构分层、数据库设计、接口设计、Docker、AI、文件、Redis、完成情况和后续计划。
  - Verify: 人工检查能按 5-10 分钟顺序讲完整。
  - Files: `docs/interview-guide.md`、`README.md`。
- [x] Task: 演示路径整理
  - Acceptance: README 明确演示账号、访问地址、Docker 命令和推荐演示路线。
  - Verify: `docker compose config --quiet`；手工按路线检查页面入口。
  - Files: `README.md`、`docs/tasks.md`。

验收标准：

- 项目可以从零启动并直接演示核心闭环。
- 面试时可以围绕业务闭环和技术设计连贯表达。

验证记录：

- 已新增 `docs/interview-guide.md`，覆盖项目背景、技术栈、架构分层、数据库设计、接口设计、Docker 启动、AI 定位、文件存储、Redis、安全、测试、已完成内容和后续计划。
- 已在 README 文档导航中加入面试指南和 P9 测试安全审查记录。
- 已在 README 的 Docker 一键启动部分补充推荐演示路线：我的家、设备护照、设备详情、耗材、维修、凭证盒、智能助手。
- 已同步修正 README、`docs/architecture.md`、`docs/api.md` 中关于 JWT 黑名单的旧口径，保持文档与 P9.2.3 实现一致。

面试口径：

- 可以按“业务背景 -> 技术栈 -> 分层架构 -> 数据库设计 -> 接口设计 -> Docker -> Redis/RustFS/AI -> 安全测试 -> 后续计划”的顺序讲完整项目。

### P9.6 产品体验继续打磨

目标：

- 在不大改接口的前提下，把前端表达继续从“后台 CRUD”收口到“家庭设备管家”。

任务拆分：

- [x] Task: 设备护照入口场景化
  - Acceptance: 设备列表优先按房间/场景展示设备卡片墙，同时保留筛选、分页和编辑入口。
  - Verify: `npm run build`；浏览器检查 `/devices` 移动端和桌面端布局。
  - Files: `frontend/src/views/devices/DeviceListView.vue`、`docs/ui.md`。
- [x] Task: 演示登录体验收口
  - Acceptance: 登录页不默认填入密码，改为显式“填入演示账号”动作，避免把演示密码误认为真实密钥。
  - Verify: `npm run build`。
  - Files: `frontend/src/views/auth/LoginView.vue`。
- [x] Task: 已完成体验确认
  - Acceptance: 设备详情已是设备护照，家庭日历已有真实图钉风格，附件库已按凭证盒表达，AI 已表达为智能助手。
  - Verify: 人工检查页面和文档描述一致。
  - Files: `docs/ui.md`、`docs/tasks.md`。

验收标准：

- 页面表达更像家庭设备管家，而不是普通后台管理平台。
- 新增体验仍然服务于家庭设备生命周期管理。

验证记录：

- 已先更新 `docs/ui.md`，记录 `/devices` 从普通设备表格升级为设备护照卡片墙的设计原因、页面结构和交互要求。
- 已将 `frontend/src/views/devices/DeviceListView.vue` 改为默认按房间分组的设备护照卡片墙，保留筛选、分页、编辑、状态流转、删除和高级清单视图。
- 已将 `frontend/src/views/auth/LoginView.vue` 的账号密码默认值改为空，并增加“填入演示账号”按钮，避免把演示密码误解为生产密钥硬编码。
- 设备详情、家庭日历、凭证盒和智能助手在 P8/P9 已完成第一轮场景化表达；P9.6 重点补齐设备护照入口。
- 已执行 `npm run build`，前端类型检查和 Vite 构建通过。

面试口径：

- 可以说明“普通后台会先给表格；FixLedger 面向家庭，所以先按房间展示设备卡片，表格只保留为高级视图。这样既保留工程上的分页和编辑能力，又让产品第一眼不是管理平台”。
- 可以说明“登录页不默认暴露演示密码，改成用户主动点击填入演示账号，这是安全意识和演示便利之间的平衡”。

### P9.7.1 RustFS 文件存储接入

目标：

- 将附件上传从默认本地文件存储升级为 RustFS 对象存储，保持业务层仍通过 `FileStorageService` 调用。

范围：

- 更新 `docs/tasks.md`、`docs/architecture.md`、`docs/database.md`、`docs/api.md`、`README.md` 和 `.env.example` 的 RustFS 说明。
- 在后端新增 S3/RustFS 存储实现，支持上传、下载和 Bucket 自动创建。
- 更新 `docker-compose.yml`，新增 `rustfs` 服务并让后端默认使用 RustFS。
- 保留本地文件存储实现，便于测试和无对象存储环境兜底。

验收标准：

- Docker Compose 启动后包含 RustFS 服务，并默认将后端文件存储切到 RustFS。
- 后端配置 `FILE_STORAGE_TYPE=rustfs` 时，上传文件写入 RustFS Bucket；`s3`、`minio` 可复用同一套 S3 兼容实现。
- 附件元数据仍写入 `fl_file_resource`，业务接口路径和响应结构不变。
- 后端测试通过，且不依赖真实外网服务。

验证记录：

- 已补充 RustFS/S3 配置、Docker Compose 服务和文档说明。`FILE_S3_ACCESS_KEY` / `FILE_S3_SECRET_KEY` 同时作为后端访问 RustFS 与 RustFS 容器账号。
- 已保留 `application-test.yml` 使用本地文件存储，保证后端测试不依赖 RustFS。`FileStorageServiceConditionTest` 覆盖 `local`、`rustfs`、`minio` 存储实现选择。
- 已执行 `docker compose config --services` 和 `docker compose config --quiet`，服务包含 `mysql`、`redis`、`rustfs`、`backend`、`frontend`。
- 已在 JDK 21 环境执行后端 `mvn test`，共 72 个测试用例，失败 0、错误 0。
- 已执行 `git diff --check`，未发现空白符错误。

调整说明：

- RustFS 兼容 S3 API，因此后端按 S3 Client 抽象接入，不直接依赖 RustFS 私有协议。
- 业务接口路径和响应结构保持不变，只有底层 `FileStorageService` 实现切换。对象 Key 使用 `families/{familyId}/{bizType}/{yyyy}/{MM}/{uuid}.{extension}`，由后端鉴权后转发下载。


### P9.8 Skills 文档规范对齐

目标：

- 按 `using-agent-skills`、`spec-driven-development`、`planning-and-task-breakdown`、`documentation-and-adrs` 和 `code-review-and-quality` 的规范，补齐项目文档的规格、决策、任务、边界和验证口径。

任务拆分：

- [x] Task: 新增项目级规格文档
  - Acceptance: `docs/spec.md` 包含 Objective、Tech Stack、Commands、Project Structure、Code Style、Testing Strategy、Boundaries、Success Criteria、Open Questions。
  - Verify: 人工检查章节完整；通过 `rg` 确认关键章节存在。
  - Files: `docs/spec.md`。
- [x] Task: 新增架构决策记录
  - Acceptance: `docs/decisions/` 包含核心技术栈、RustFS 文件存储、AI 辅助定位、家庭场景 UI 的 ADR。
  - Verify: 人工检查每个 ADR 包含 Context、Decision、Consequences、Verification。
  - Files: `docs/decisions/README.md`、`docs/decisions/0001-core-stack-and-layering.md`、`docs/decisions/0002-rustfs-file-storage.md`、`docs/decisions/0003-ai-as-auxiliary-capability.md`、`docs/decisions/0004-household-scene-first-ui.md`。
- [x] Task: 调整任务留痕规范
  - Acceptance: `docs/tasks.md` 明确 skills 执行规范，并要求后续小版本记录 Acceptance、Verify、Files。
  - Verify: 人工检查 P9.8 任务记录和模板可复用。
  - Files: `docs/tasks.md`。
- [x] Task: 修正文档交叉引用和过期表述
  - Acceptance: README、需求、架构、接口、数据库、UI 文档都能指向规格文档或 ADR，不再把当前 RustFS 能力描述成未完成的 MinIO 预留。
  - Verify: 使用 `rg` 检查过期关键词；执行 `git diff --check`。
  - Files: `README.md`、`docs/requirements.md`、`docs/architecture.md`、`docs/api.md`、`docs/database.md`、`docs/ui.md`。

验收标准：

- 文档符合 skills 要求的规格、任务、ADR 和验证结构。
- 后续开发可以先读 `docs/spec.md` 和 `docs/decisions/` 理解项目边界与关键取舍。
- 文档仍保持 FixLedger 的家庭设备生命周期定位，不退化成泛后台或 AI 产品。

验证记录：

- 已读取并应用 `using-agent-skills`、`spec-driven-development`、`planning-and-task-breakdown`、`documentation-and-adrs`、`code-review-and-quality`。
- 已执行文档关键词检查、控制字符检查和 `git diff --check`，结果见本次回复。

调整说明：

- P9.8 只调整项目文档，不改业务代码、不改数据库脚本、不改前端页面。
- 旧阶段历史记录保留，不重写开发过程；后续新增任务按 skills 模板执行。

### P9.7 可选增强能力

目标：

- 只补强对面试展示和工程质量最有价值的可选能力，真实 AI、邮件/Webhook、文件预览等高复杂度能力先保留为 P10 计划。

任务拆分：

- [x] Task: RustFS 对象存储接入
  - Acceptance: Docker 默认使用 RustFS，业务层仍通过 `FileStorageService`。
  - Verify: `docker compose config --quiet`、后端测试。
  - Files: `docker-compose.yml`、`backend/src/main/java/com/fixledger/infrastructure/file`。
- [ ] Task: CI 质量门禁
  - Acceptance: GitHub Actions 自动执行后端测试、前端构建和 Docker Compose 配置校验。
  - Verify: 本地执行同等命令；工作流文件语法可读。
  - Files: `.github/workflows/ci.yml`、`README.md`。
- [ ] Task: 可选增强边界说明
  - Acceptance: 明确真实 AI、邮件/Webhook、文件预览、OCR、Refresh Token 进入后续 P10，不阻塞 P9 验收。
  - Verify: 文档中能清晰说明为什么先不做。
  - Files: `docs/tasks.md`、`docs/interview-guide.md`。

验收标准：

- 增强能力不影响核心 MVP 使用。
- 可选能力有清晰开关、配置和失败兜底。

### P9.9 P9 全量验收收尾

目标：

- 汇总 P9 的文档、代码、测试、安全、演示和体验结果，正式关闭系统性完善阶段。

任务拆分：

- [ ] Task: 全量验证
  - Acceptance: 后端测试、前端构建、Docker Compose 配置校验全部通过。
  - Verify: `mvn test`、`npm run build`、`docker compose config --quiet`。
  - Files: `docs/tasks.md`。
- [ ] Task: P9 完成状态归档
  - Acceptance: `docs/tasks.md` 当前状态、验证记录和下一阶段建议更新为 P10。
  - Verify: 人工检查 P9 小版本状态和最终验证记录一致。
  - Files: `docs/tasks.md`。

验收标准：

- P9 各小版本均有明确完成或延期说明。
- 项目进入 P10 时边界清晰，不把可选增强误认为 MVP 缺口。

## 13. MVP 验收清单

MVP 完成需要满足：

- [x] 后端可启动。
- [x] 前端可启动。
- [x] MySQL 表结构可初始化。
- [x] Redis 可连接。
- [x] 用户可注册登录。
- [x] 用户有默认家庭空间。
- [x] 用户可管理设备分类。
- [x] 用户可管理设备档案。
- [x] 用户可管理保修记录。
- [x] 用户可上传附件。
- [x] 用户可管理耗材。
- [x] 用户可记录耗材更换。
- [x] 用户可管理维修记录。
- [x] 系统可生成提醒。
- [x] 首页可展示统计数据。
- [x] AI Mock 功能可用。
- [x] README 可指导本地启动。

## 14. 推荐开发顺序

```text
1. 后端脚手架
2. 前端脚手架
3. 统一响应和异常
4. 登录注册
5. 家庭空间
6. 设备分类
7. 设备档案
8. 保修记录
9. 附件上传
10. 耗材管理
11. 维修记录
12. 提醒任务
13. 首页看板
14. AI Mock
15. AI 真实 Provider
16. Docker 和测试
```

## 15. 面试展示优先级

优先打磨这些能力：

1. 设备详情聚合页。
2. 保修和耗材提醒。
3. 维修状态流转。
4. Redis 提醒去重。
5. AI 故障排查建议。
6. 我的家场景首页。
7. 设备护照和凭证盒。

## 16. 风险与控制

| 风险 | 控制方式 |
| --- | --- |
| 功能范围变大 | 先完成核心闭环，其他放二期 |
| AI 接口不可用 | 默认 Mock Provider |
| 文件存储复杂 | Docker 默认 RustFS，测试保留本地文件，MinIO/S3 通过同一抽象可替换 |
| 前端页面过多 | 优先设备详情、首页、列表页 |
| 定时任务难测试 | 提供手动扫描接口 |
| 权限遗漏 | 所有业务接口统一校验 familyId |

## 17. 当前状态

截至当前文档版本：

- README 已完成。
- AGENTS 规范已完成。
- docs 开发资料已完成第一版。
- 后端 P0-P7 已完成：脚手架、认证与家庭空间、设备分类与设备档案、保修记录与附件、耗材与维修、提醒与看板、AI 辅助、后端工程化。
- 前端 MVP 页面已完成：登录注册、主布局、首页看板、设备档案、设备详情、保修管理、耗材管理、维修记录、维修详情、提醒中心、附件库、AI 助手和家庭设置。
- 后端完整 Maven 测试已通过，当前共 76 个测试用例，失败 0、错误 0、跳过 0。
- 前端 `npm run build` 已通过，Vue 类型检查和 Vite 构建均成功。
- Docker Compose 已支持一键启动前端、后端、MySQL、Redis 和 RustFS，前端通过 Nginx 代理后端 API。
- P8 产品化体验重构已启动：首页升级为“我的家”，导航从后台模块转向家庭场景入口。
- 已完成一次项目注释审查：补充后端核心类/公开方法 Javadoc，以及前端 API、Store、路由和字典工具注释。
- P9 系统性完善阶段已正式启动：P9.1 文档与实现对齐、P9.2 代码质量治理、P9.3 测试体系补强、P9.4 安全与数据隔离、P9.7.1 RustFS 文件存储接入、P9.8 Skills 文档规范对齐已完成，后续按演示材料、产品体验、CI 门禁和全量验收推进。

下一步建议：

```text
进入 P9.6 产品体验继续打磨：将设备护照入口改成更生活化的房间卡片墙，并收口演示登录体验。
```
