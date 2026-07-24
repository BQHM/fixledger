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
| P14 | 演示体验完善 | 准备演示数据、演示账号、README、页面展示说明和面试讲解稿 |
| P15 | 产品体验升级与可选增强 | 继续弱化后台感，评估文件存储、AI Provider、通知和 CI/CD |
| P16 | 凭证聚合与智能归档增强 | 凭证盒聚合接口、说明书搜索和智能归档基础 |
| P17 | 前端体验重做 | 根据反馈推倒重做前端视觉，改为简洁实用的浅色产品工具风 |
| P18 | 核心体验补强 | 补齐注册后默认分类、家庭初始化和核心闭环的小缺口 |
| P19 | 演示级体验复核 | 强化演示路径、静态契约和空状态可用性 |
| P20 | 家庭成员协作闭环 | 已注册用户邀请、角色调整、移除和前端协作入口 |
| P21 | 凭证智能化增强 | 票据解析到设备草稿的可确认录入闭环 |
| P22 | 通知与操作日志增强 | 操作日志落库查询、通知通道抽象和审计可追踪 |
| P23 | 生产化与 CI/CD | 强化 CI 门禁、生产检查脚本和部署说明 |
| P24 | 家庭数据导出 | 设备资产清单和维修费用报表 CSV 导出 |
| P25 | 设备二维码标签 | 生成设备标签，扫码查看设备档案或只读设备卡片 |
| P26 | OCR 与智能归档 | 发票图片识别、说明书内容解析和 AI 草稿确认 |
| P27 | 移动端与 PWA | 优化手机端布局、触控体验和离线访问能力 |
| P28 | 外部通知渠道 | 邮件/Webhook 投递、失败记录和重试策略 |
| P29 | 性能与可观测性 | 首页缓存、慢查询治理、关键指标和导出异步化预案 |
| P30 | 生产发布收口 | 生产配置、备份恢复、HTTPS、回滚和发布说明 |

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
| P2 设备档案核心 | P2.1 设备分类 | 分类增删改查和家庭内唯一性约束 | 已完成，P18.1 已补齐默认分类初始化 |
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
| P9 系统性完善阶段 | P9.7 可选增强能力 | 评估对象存储、真实 AI、通知渠道、CI/CD 等增强项 | 已完成 |
| P9 系统性完善阶段 | P9.7.1 RustFS 文件存储接入 | 将上传文件从本地存储切换为 RustFS/S3 兼容对象存储 | 已完成 |
| P9 系统性完善阶段 | P9.7.2 CI 与可选增强评估 | 增加质量门禁并明确真实 AI、通知、预览等增强边界 | 已完成 |
| P9 系统性完善阶段 | P9.8 Skills 文档规范对齐 | 按 skills 规范补齐规格、ADR、任务模板、边界和验证口径 | 已完成 |
| P9 系统性完善阶段 | P9.9 P9 全量验收收尾 | 汇总 P9 验收、验证命令、面试口径和后续 P10 方向 | 已完成 |
| P10 文档对齐 | P10.1 需求文档复核 | 深度复核 `requirements.md` 与当前产品边界 | 已完成 |
| P10 文档对齐 | P10.2 架构文档复核 | 深度复核 `architecture.md` 与当前工程实现 | 已完成 |
| P10 文档对齐 | P10.3 接口与数据库文档复核 | 深度复核 `api.md`、`database.md` 与代码/数据库 | 已完成 |
| P10 文档对齐 | P10.4 UI 与 README 复核 | 深度复核 `ui.md`、`README.md` 与演示体验 | 已完成 |
| P11 代码质量治理 | P11.1 后端规范扫描 | 扫描异常、日志、事务、配置和 Entity 暴露风险 | 已完成 |
| P11 代码质量治理 | P11.2 前端规范扫描 | 扫描 API 封装、类型、路由守卫和页面职责 | 已完成 |
| P11 代码质量治理 | P11.3 注释与命名复核 | 复核类注释、方法注释、命名和无效注释 | 已完成 |
| P11 代码质量治理 | P11.4 构建与静态检查 | 固化后端测试、前端构建和格式检查流程 | 已完成 |
| P12 测试体系补强 | P12.1 Service 核心业务测试 | 补强保修、耗材、维修、提醒和 AI 业务规则 | 已完成 |
| P12 测试体系补强 | P12.2 Controller 与权限测试 | 补强参数校验、认证拦截和越权访问测试 | 已完成 |
| P12 测试体系补强 | P12.3 前端构建与冒烟测试 | 固化前端构建、关键页面和接口联调检查 | 已完成 |
| P12 测试体系补强 | P12.4 Docker 启动验证 | 固化一键启动后的健康检查和排障说明 | 已完成 |
| P13 安全与数据隔离 | P13.1 家庭空间隔离审计 | 审查设备、保修、耗材、维修、提醒和附件归属校验 | 已完成 |
| P13 安全与数据隔离 | P13.2 附件安全审计 | 审查文件类型、大小、访问鉴权和删除策略 | 已完成 |
| P13 安全与数据隔离 | P13.3 认证与敏感信息审计 | 审查 JWT、密码、Token、日志和响应脱敏 | 已完成 |
| P13 安全与数据隔离 | P13.4 边界参数审计 | 审查分页上限、ID 越权、日期和金额边界 | 已完成 |
| P14 演示体验完善 | P14.1 演示数据整理 | 固化可重复演示的用户、家庭、设备和业务数据 | 已完成 |
| P14 演示体验完善 | P14.2 面试演示路径 | 整理 5-10 分钟功能演示和技术讲解路线 | 已完成 |
| P14 演示体验完善 | P14.3 README 展示增强 | 增加启动、账号、页面展示、功能亮点和排障说明 | 已完成 |
| P14 演示体验完善 | P14.4 常见问答材料 | 整理架构、数据库、Redis、AI、文件存储和安全问答 | 已完成 |
| P15 产品体验升级与可选增强 | P15.1 设备护照深化 | 完善设备详情生命周期表达和可视化体验 | 已完成首批米家风格改造 |
| P15 产品体验升级与可选增强 | P15.1.1 公开首页 | 增加类似米家官网气质的产品首页和演示入口 | 已完成 |
| P15 产品体验升级与可选增强 | P15.2 凭证盒深化 | 完善凭证分类、完整度和附件预览体验 | 已完成 |
| P15 产品体验升级与可选增强 | P15.3 存储与 AI 增强 | 评估 RustFS/MinIO、真实 AI Provider 和 Mock 兜底 | 已完成本轮收口 |
| P15 产品体验升级与可选增强 | P15.4 自动化与通知增强 | 评估 CI/CD、邮件、Webhook 和操作日志 | 已完成本轮收口 |
| P16 凭证聚合与智能归档增强 | P16.1 凭证盒后端聚合接口 | 后端统一聚合设备凭证、说明书、保修、维修和耗材附件 | 已完成 |
| P16 凭证聚合与智能归档增强 | P16.2 说明书全文搜索 | 第一版说明书文件名和文本型 PDF 关键词搜索 | 已完成 |
| P17 前端体验重做 | P17.1 简洁实用视觉重做 | 废弃米家风大卡片和暖橙 Hero，重做全前端视觉 | 已完成 |
| P17 前端体验重做 | P17.2 Liquid Glass 视觉修正 | 去掉旧绿色，改为银白玻璃、系统蓝、淡紫 tint 的新潮实用 UI | 已完成 |
| P17 前端体验重做 | P17.3 系统级玻璃质感细化 | 统一 surface/elevated/chip/tint 材质、控件状态和首屏摘要面板质感 | 已完成 |
| P17 前端体验重做 | P17.4 纯 CSS 液体玻璃细节补强 | 参考 CSS Liquid Glass 文档，增强伪元素高光、内反射和登录首页层级 | 已完成 |
| P17 前端体验重做 | P17.5 米家官网式公开首页返工 | 参考 `home.mi.com` 的浅色留白和产品预览节奏，重做公开首页第一屏 | 已完成 |
| P17 前端体验重做 | P17.6 登录注册入口统一 | 登录/注册页同步公开首页浅色产品入口风格，降低玻璃拟态和蓝紫割裂感 | 已完成 |
| P17 前端体验重做 | P17.7 全前端体验收口 | 全局 token、主布局和高频页面统一为浅色产品工具风，清理蓝紫割裂和旧设备护照文案 | 已完成 |
| P18 核心体验补强 | P18.1 默认设备分类初始化 | 注册默认家庭和手动创建家庭后自动生成常用设备分类 | 已完成 |
| P18 核心体验补强 | P18.2 当前家庭上下文自愈 | 登录态恢复时修正失效家庭 ID，避免后续接口使用脏上下文 | 已完成 |
| P18 核心体验补强 | P18.3 新建设备默认分类体验 | 新增设备时基于默认分类预选兜底分类，减少首次录入阻力 | 已完成 |
| P18 核心体验补强 | P18.4 新用户空数据引导 | 总览和凭证盒在无设备时引导创建第一台设备 | 已完成 |
| P18 核心体验补强 | P18.5 空数据加载态收口 | 避免加载前闪现空状态，并区分凭证盒搜索无结果和家庭无设备 | 已完成 |
| P19 演示级体验复核 | P19.1 演示契约检查增强 | smoke 覆盖核心演示入口、空状态和智能录入跳转 | 已完成 |
| P19 演示级体验复核 | P19.2 演示文档与 README 收口 | 明确 Docker 演示路径、账号、功能边界和排障入口 | 已完成 |
| P20 家庭成员协作闭环 | P20.1 家庭成员后端闭环 | 邀请已注册用户、调整角色、移除成员并补权限测试 | 已完成 |
| P20 家庭成员协作闭环 | P20.2 家庭设置前端协作入口 | 家庭设置页支持邀请、角色调整和移除成员 | 已完成 |
| P21 凭证智能化增强 | P21.1 票据草稿分类预填 | AI 票据解析结果带入设备名称、购买信息和建议分类 | 已完成 |
| P22 通知与操作日志增强 | P22.1 操作日志基础能力 | 关键家庭协作操作落库并提供分页查询接口 | 已完成 |
| P22 通知与操作日志增强 | P22.2 通知通道抽象 | 将站内通知写入收敛到通知服务，保留邮件/Webhook 扩展点 | 已完成 |
| P23 生产化与 CI/CD | P23.1 CI 门禁增强 | 拆分前端类型检查、构建、smoke 和安全审计，并支持手动触发 | 已完成 |
| P23 生产化与 CI/CD | P23.2 生产检查脚本与部署说明 | 增加生产准备检查脚本和部署前检查文档 | 已完成 |
| P24 家庭数据导出 | P24.1 导出后端接口 | 支持设备资产清单和维修费用报表 CSV 下载，并校验家庭权限 | 已完成 |
| P24 家庭数据导出 | P24.2 前端下载入口 | 设备页和维修页提供导出按钮，复用当前筛选上下文 | 已完成 |
| P25 设备二维码标签 | P25.1 标签生成接口 | 为设备生成可打印二维码标签，扫码进入设备详情或只读卡片 | 计划中 |
| P25 设备二维码标签 | P25.2 前端打印入口 | 设备详情页和列表页提供二维码预览、下载和打印入口 | 计划中 |
| P26 OCR 与智能归档 | P26.1 OCR 抽象与 Mock | 增加 OCR Client 抽象和 Mock 实现，不阻塞核心上传流程 | 计划中 |
| P26 OCR 与智能归档 | P26.2 凭证识别草稿 | 发票图片/说明书文本识别后生成设备或保修草稿，用户确认后保存 | 计划中 |
| P27 移动端与 PWA | P27.1 响应式主流程 | 登录、我的家、设备详情、凭证盒和维修记录适配手机端 | 已完成 |
| P27 移动端与 PWA | P27.2 PWA 基础 | 增加安装入口、离线壳和基础缓存策略 | 已完成 |
| P28 外部通知渠道 | P28.1 通知投递实现 | 在 `NotificationService` 后扩展邮件/Webhook 实现和配置项 | 已完成 |
| P28 外部通知渠道 | P28.2 失败追踪与重试 | 记录外部通知失败原因，提供可控重试，不影响核心事务 | 已完成 |
| P29 性能与可观测性 | P29.1 热点查询治理 | 首页统计缓存、慢查询复核和常用索引验证 | 已完成 |
| P29 性能与可观测性 | P29.2 观测与异步预案 | 关键接口指标、导出异步化边界和生产日志说明 | 已完成 |
| P30 生产发布收口 | P30.1 生产配置清单 | 环境变量、HTTPS、反向代理、备份和恢复步骤 | 计划中 |
| P30 生产发布收口 | P30.2 发布与回滚演练 | 固化发布前检查、版本标记、回滚清单和演示验收 | 计划中 |

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
- [x] 新家庭空间初始化默认分类。
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
- [x] Task: CI 质量门禁
  - Acceptance: GitHub Actions 自动执行后端测试、前端构建和 Docker Compose 配置校验。
  - Verify: 本地执行同等命令；工作流文件语法可读。
  - Files: `.github/workflows/ci.yml`、`README.md`。
- [x] Task: 可选增强边界说明
  - Acceptance: 明确真实 AI、邮件/Webhook、文件预览、OCR、Refresh Token 进入后续 P10，不阻塞 P9 验收。
  - Verify: 文档中能清晰说明为什么先不做。
  - Files: `docs/tasks.md`、`docs/interview-guide.md`。

验收标准：

- 增强能力不影响核心 MVP 使用。
- 可选能力有清晰开关、配置和失败兜底。

验证记录：

- 已新增 `.github/workflows/ci.yml`，在 `push main` 和面向 `main` 的 Pull Request 上自动执行后端测试、前端构建和 Docker Compose 配置校验。
- 已在 README 补充 CI 质量门禁说明，明确本地验证与仓库自动验证的关系。
- 已在 `docs/interview-guide.md` 补充可选增强取舍：真实 AI、OCR、邮件/Webhook、附件在线预览和 Refresh Token 进入 P10+ / P15，不阻塞 P9 验收。
- 已执行 `docker compose config --quiet`，Compose 配置校验通过。

调整说明：

- P9.7 已完成 RustFS 和 CI 两类高价值增强；真实 AI Provider、邮件/Webhook、OCR、附件预览和 Refresh Token 暂不做，是为了避免引入外部服务、密钥、回调和更复杂安全策略后拖慢 MVP 收口。

面试口径：

- 可以说明“P9.7 不是把所有能想到的增强都做完，而是选择最能证明工程质量的 RustFS 和 CI；其他增强先留到后续阶段，体现范围控制能力”。
- 可以说明“CI 让后端测试、前端构建和 Docker 配置校验每次提交自动执行，避免项目只在本机可用”。

### P9.9 P9 全量验收收尾

目标：

- 汇总 P9 的文档、代码、测试、安全、演示和体验结果，正式关闭系统性完善阶段。

任务拆分：

- [x] Task: 全量验证
  - Acceptance: 后端测试、前端构建、Docker Compose 配置校验全部通过。
  - Verify: `mvn test`、`npm run build`、`docker compose config --quiet`。
  - Files: `docs/tasks.md`。
- [x] Task: P9 完成状态归档
  - Acceptance: `docs/tasks.md` 当前状态、验证记录和下一阶段建议更新为 P10。
  - Verify: 人工检查 P9 小版本状态和最终验证记录一致。
  - Files: `docs/tasks.md`。

验收标准：

- P9 各小版本均有明确完成或延期说明。
- 项目进入 P10 时边界清晰，不把可选增强误认为 MVP 缺口。

验证记录：

- 已执行后端全量测试：`mvn test -q`，Surefire 汇总 80 个测试，失败 0、错误 0、跳过 0。
- 已执行前端构建：`npm run build`，Vue 类型检查和 Vite 生产构建通过；当前只存在 Vite chunk size 与第三方注释告警，不影响构建结果。
- 已执行 Docker Compose 配置校验：`docker compose config --quiet`，通过。
- 已执行提交前空白检查：`git diff --check`，通过。

收口结论：

- P9 已完成文档对齐、代码质量治理、安全与测试审查、RustFS 文件存储、Skills 文档规范、面试材料、设备护照体验、CI 质量门禁和全量验收。
- 后续进入 P10，不再把真实 AI、OCR、邮件/Webhook、附件在线预览、Refresh Token 当作 P9 阻塞项，而是作为增强阶段继续评估。

面试口径：

- 可以说明“P9 不是继续堆功能，而是把项目从可运行提升到可演示、可解释、可验证：有测试、有安全审查、有 CI、有 Docker、有文档留痕”。

### P9 总结

完成范围：

- P9.1 文档与实现对齐：修正 README、架构、接口、数据库和 UI 与当前实现的偏差。
- P9.2 代码质量治理：处理事务边界、日志、异常脱敏、JWT 退出黑名单和文件名安全。
- P9.3 / P9.4 测试与安全：补充认证、附件、安全和家庭空间隔离审查证据。
- P9.5 面试材料：新增面试讲解指南和演示路径。
- P9.6 产品体验：将设备护照入口升级为按房间分组的卡片墙，登录页改为主动填入演示账号。
- P9.7 可选增强：接入 RustFS，新增 GitHub Actions CI，明确真实 AI、OCR、通知和预览延期。
- P9.8 Skills 文档规范：补充规格文档和 ADR，固化先文档、再实现、再验证的流程。
- P9.9 全量验收：完成后端测试、前端构建、Docker 配置和空白检查。

下一阶段：

- P10 文档深度对齐：逐项复核 `requirements.md`、`architecture.md`、`api.md`、`database.md`、`ui.md`、README 与最终代码和演示体验。
- P10 后再进入更细的 P11 代码治理、P12 测试增强、P13 安全审计、P14 演示增强或 P15 产品体验升级。


## 13. P10 文档对齐

P10 的目标不是继续堆功能，而是把已经完成的系统能力、暂未实现的增强项和面试讲解口径统一到文档中。这样做的原因是：面试官通常会追问“文档写的和代码是否一致”，P10 可以证明项目不仅能跑，还能被准确解释、复盘和维护。

### P10.1 需求文档复核

目标：

- 复核 `docs/requirements.md` 和 `docs/spec.md`，把当前实现、预留能力和后续增强拆清楚。

范围：

- 修正设备列表筛选口径：当前支持关键字、分类、状态、品牌；保修状态筛选为后续增强。
- 修正提醒口径：当前定时生成保修和耗材提醒；维修待跟进保留类型但未接入扫描。
- 修正 Redis 口径：当前用于提醒去重、JWT 黑名单和首页刷新标记/缓存钩子，不夸大为完整首页结果缓存。
- 修正 CI 口径：GitHub Actions 已存在，后续问题是是否增加部署流水线、E2E 和分支保护。

验收标准：

- `requirements.md` 中不再把未实现能力写成已完成能力。
- `spec.md` 中的技术栈、边界和 Open Questions 与当前 P9 完成状态一致。
- `tasks.md` 记录 P10.1 的执行范围、验证方式和完成结论。

验证记录：

- `rg -n "P9.7.1|保修状态筛选|是否新增 CI/CD|首页统计缓存" docs/requirements.md docs/spec.md`：已用于检查旧口径。
- `git diff --check`：P10.1 提交前执行。

完成结论：

- P10.1 已完成。需求文档现在能清楚回答“这个版本做了什么、哪些只是预留、为什么后续再做”。

### P10.2 架构文档复核

目标：

- 复核 `docs/architecture.md`，让架构分层、Redis、定时任务、通知和文件存储描述与当前工程一致。

范围：

- 区分当前已有基础设施和后续预留基础设施。
- 修正单一提醒扫描 cron、站内通知当前落点和 Refresh Token 后续增强口径。
- 修正 Redis 架构描述，明确首页当前是刷新标记/缓存钩子，不是完整统计结果缓存。
- 修正通知架构描述，明确当前没有独立 `NotificationService`，站内通知由提醒模块写入。

验收标准：

- `architecture.md` 不再把 Refresh Token、独立 NotificationService、维修待跟进扫描或完整首页缓存描述为当前已实现。
- 定时任务章节能对应 `ReminderScheduler`、`ReminderService` 和 `ReminderCreationService` 的实际职责。
- 面试时可以用“当前实现 + 后续扩展点”解释架构取舍。

验证记录：

- `rg -n "NotificationService|SchedulerSupport|08:10|P9.7.1|首页统计缓存" docs/architecture.md`：用于检查旧架构口径。
- `git diff --check`：P10.2 提交前执行。

完成结论：

- P10.2 已完成。架构文档现在能清楚说明当前系统的真实分层、定时任务、Redis 和通知边界。

### P10.3 接口与数据库文档复核

目标：

- 复核 `docs/api.md`、`docs/database.md`，让接口示例和实际 Controller、表结构一致。

范围：

- 修正设备分页字段、保修状态预留字段、当前接口清单和实际表结构说明。
- 明确设备分页支持 `keyword`、`categoryId`、`status`、`brand`，不支持 `warrantyStatus` 查询参数。
- 明确 `schema.sql` 是当前表结构准确信息来源，RBAC 和操作日志表仍是二期规划。
- 明确 `MAINTENANCE_FOLLOW_UP` 是提醒类型预留，当前扫描只生成保修和耗材提醒。

验收标准：

- `api.md` 的接口示例不会把当前返回 `null` 的字段写成已聚合完成。
- `database.md` 明确当前已建表和规划表边界。
- 面试时可以说清楚“接口为什么保留扩展字段、数据库为什么先不做 RBAC 表”。

验证记录：

- `rg -n "warrantyStatus|VALID|P9.7.1|MAINTENANCE_FOLLOW_UP" docs/api.md docs/database.md`：用于检查旧接口和表结构口径。
- `rg -n "@(RequestMapping|GetMapping|PostMapping|PutMapping|DeleteMapping|PatchMapping)" backend/src/main/java/com/fixledger/modules -g "*Controller.java"`：用于核对当前 Controller 接口范围。
- `git diff --check`：P10.3 提交前执行。

完成结论：

- P10.3 已完成。接口文档和数据库文档现在能对应当前 Controller、分页规则、RustFS 下载方式和 `schema.sql` 实际建表范围。

### P10.4 UI 与 README 复核

目标：

- 复核 `docs/ui.md`、`README.md`，让页面说明、启动说明和演示口径与当前前端体验一致。

范围：

- 修正设备护照卡片墙已实现、凭证盒/独立日历后续增强、README 当前目录和 P10 状态。
- 修正 README 中 Redis、家庭成员、通知、前端组件库和项目目录的当前实现口径。
- 修正演示路线，强调设备护照已按房间卡片墙展示，高级清单只作为工程能力保留。
- 同步面试指南和安全审查文档中的 P10 完成状态，避免仍把 P10 写成未来计划。

验收标准：

- `ui.md` 不再把设备护照卡片墙写成后续才做。
- README 第一入口能解释当前完成状态、Docker 启动、CI 门禁和后续增强边界。
- 面试指南不再把 P10 写成下一阶段，而是把下一步指向 P11 代码质量治理。
- P10 四个小版本全部有完成记录和验证证据。

验证记录：

- `rg -n "后续升级为按房间|P9.8 文档对齐状态|计划目录结构|首页统计缓存" docs/ui.md README.md docs/interview-guide.md docs/security-test-review.md`：用于检查旧 UI/README/面试口径。
- `docker compose config --quiet`：验证 P10 文档收口没有破坏一键启动配置。
- `git diff --check`：P10.4 提交前执行。

完成结论：

- P10.4 已完成。UI 文档、README、面试指南和安全审查文档现在能准确表达当前演示体验：我的家、家庭日历、设备护照卡片墙、凭证盒、智能助手和 Docker 一键启动。

### P10 总结

完成范围：

- P10.1 需求文档复核：把当前实现、预留字段和后续增强拆清楚。
- P10.2 架构文档复核：修正 Redis、定时任务、通知、Refresh Token 和文件存储架构口径。
- P10.3 接口与数据库文档复核：对齐 Controller、分页规则、RustFS 下载方式和 `schema.sql` 实际建表。
- P10.4 UI 与 README 复核：对齐设备护照卡片墙、凭证盒、家庭日历、Docker、CI 和项目入口说明。

验证记录：

- P10 每个小版本提交前均执行 `git diff --check`。
- P10.4 收口执行 `docker compose config --quiet`，确认 Docker 编排配置仍有效。

面试口径：

- 可以说明“P10 不是做新功能，而是把项目从能演示提升到能被追问：需求、架构、接口、数据库、UI 和 README 都能对应实际代码，不把未实现能力包装成已完成能力”。

下一步建议：

- 进入 P11 代码质量治理：继续按任务文档做后端规范扫描、前端规范扫描、注释与命名复核、构建与静态检查。


## 14. P11 代码质量治理

P11 的目标是把 P10 文档对齐后的结论落到代码质量检查上。P10 解决“文档能否说清楚”，P11 解决“代码是否经得起阅读、审查和面试追问”。本阶段不新增业务功能，优先做规范扫描、小范围修复和验证闭环。

### P11.1 后端规范扫描

目标：

- 扫描后端异常、日志、事务、配置、返回值和 Entity 暴露风险，优先修复低风险且影响面试评价的问题。

范围：

- 检查是否存在 `throw new RuntimeException(...)`、`System.out`、`printStackTrace`、空 `catch`、Controller 直接返回 Entity、业务代码绕过 `Result<T>`。
- 检查 `@Transactional` 是否主要放在 Service 层，外部能力调用是否与核心事务解耦。
- 检查配置是否通过 `@ConfigurationProperties` 管理，避免 `@Value` 散落在 Service。
- 根据扫描结果做小范围修复或记录延期原因。

扫描结论：

- 未发现业务代码直接 `throw new RuntimeException(...)`、`throw new Exception(...)`、`System.out`、`System.err`、`printStackTrace` 或 `@Value` 散落。
- Controller 返回值均使用 `Result<T>`；文件下载和全局业务异常 HTTP 状态映射使用 `ResponseEntity` 属于合理例外。
- `@Transactional` 主要集中在 Service 层；提醒去重的 Redis 操作在事务外，提醒任务和站内通知由 `ReminderCreationService` 同事务写库。
- `StringRedisTemplate`、`S3Client`、`AiClient` 等外部能力集中在基础设施层或统一 Client 中，业务层没有散落第三方 SDK 直接调用。
- 发现 Redis 连接失败时此前是静默降级，已修复为首次 `warn` 简要提示、后续 `debug` 堆栈，既保留排障信息又避免测试和演示刷屏。

验收标准：

- 后端扫描结果记录到本节。
- 明确哪些问题已修复、哪些是合理例外或后续增强。
- 后端测试通过。

验证记录：

- `rg -n "throw new RuntimeException|throw new Exception|printStackTrace|System\.out|System\.err|@Value" backend/src/main/java`：未发现命中。
- `rg -n "ResponseEntity<|Map<|return .*Entity|@RestController" backend/src/main/java/com/fixledger/modules backend/src/main/java/com/fixledger/common`：确认 `ResponseEntity` 仅用于文件下载和异常 HTTP 状态映射，未发现 Controller 直接返回 Entity。
- `rg -n "catch \(" backend/src/main/java`：确认第三方异常均转换为业务异常、记录日志或按基础设施降级处理。
- `rg -n "@Transactional" backend/src/main/java/com/fixledger`：确认事务边界集中在 Service 层。
- `cd backend; mvn test -q`：通过；测试环境 Redis 未启动时仅出现一次简短降级 WARN，未影响测试。

完成结论：

- P11.1 已完成。后端规范整体符合 `AGENTS.md` 分层、异常、事务、配置和日志要求，本轮只做 Redis 降级日志治理这一处小修复。

### P11.2 前端规范扫描

目标：

- 扫描前端 API 封装、类型、路由守卫、页面职责和分页边界，修正影响维护性的明显问题。

范围：

- 检查页面是否直接散落 Axios URL。
- 检查是否存在无约束 `any`、调试 `console.log`、重复分页参数处理。
- 检查路由守卫、Store 和 API 模块职责是否清晰。
- 本轮计划把设备详情聚合字段从 `unknown[]` 收敛为明确业务类型，并补充附件 Blob 下载为什么绕过通用 JSON 响应封装的说明。

验收标准：

- 前端扫描结果记录到本节。
- 明确是否存在需要修复的类型或职责问题。
- 前端构建通过。

扫描结论：

- 未发现页面直接调用 `axios` 或 `fetch`；业务页面通过 `src/api/` 中的函数访问后端接口。
- 未发现 `console.log`、`console.error`、`debugger`、`TODO`、`FIXME` 或 `as any` 调试遗留。
- `request.ts` 统一处理 Token、401 退出和分页参数边界，`pageSize` 会在前端请求层收敛到 `1..100`，避免再次触发后端分页上限错误。
- `file.ts` 使用 `axiosInstance` 下载 Blob 属于合理例外，因为附件下载返回二进制内容，不符合通用 `Result<T>` JSON 解包流程。
- `DeviceDetail` 中的保修、耗材、维修和附件聚合字段已从 `unknown[]` 收敛为明确业务类型，方便页面后续安全使用字段。
- 多个页面监听 `family-changed` 当前能保持家庭空间切换后的数据刷新；后续如继续扩展页面，可抽成 composable 降低重复。
- `downloadUrl()` 暂无调用，本轮先保留为后续预览/新窗口下载扩展点，不在代码质量扫描中静默删除。

验证记录：

- `rg -n "axios\.|fetch\(|console\.log|console\.error|debugger|\bany\b|as any|TODO|FIXME|pageSize:\s*(10[1-9]|[2-9][0-9]{2,})|http://|/api/" frontend/src -g "*.ts" -g "*.vue"`：确认 API URL 集中在 `src/api/`，无调试语句和无约束 `any`。
- `rg -n "getDevicePage\(|pageSize:\s*100|family-changed|axiosInstance|request<" frontend/src -g "*.ts" -g "*.vue"`：确认设备选择使用 `pageSize: 100` 未超过后端上限，家庭切换事件和 Blob 下载例外可解释。
- `rg -n "downloadUrl\(" frontend/src`：确认当前仅定义未调用，本轮不删除。
- `cd frontend; npm run build`：通过；仅存在 Vite 分包体积和第三方库 PURE 注释警告，不影响构建。

完成结论：

- P11.2 已完成。前端 API 边界、路由守卫、Store 职责和分页保护整体符合规范，本轮只做设备详情类型收敛和附件下载说明补强。

### P11.3 注释与命名复核

目标：

- 复核核心类和关键方法注释，减少机械化、重复或误导性注释，让注释解释“为什么”和“业务作用”。

范围：

- 优先处理核心基础设施、提醒、文件、AI、认证、设备等模块中的关键公开方法注释。
- 不做大规模格式化，不改无关业务逻辑。
- 对已经清晰的代码不强行添加无意义注释。
- 本轮计划重点优化 Redis 降级、附件家庭隔离、提醒去重扫描、AI Provider 适配这些面试高频追问点的注释。

验收标准：

- 关键注释能说明方法作用、参数含义和返回值语义。
- 不引入与实现不一致的注释。
- 后端测试或前端构建至少执行对应受影响侧验证。

复核结论：

- 本轮没有按模板给所有类和方法机械补注释，而是只改高价值说明，避免代码被低信息量注释污染。
- Redis 注释已说明缓存/去重/黑名单等临时状态职责，以及 Redis 不可用时为什么可以降级到数据库兜底。
- 附件服务注释已说明家庭空间鉴权、业务对象归属校验、逻辑删除和本地/对象存储抽象，便于回答“文件为什么不能随便下载”。
- 提醒服务注释已区分手动扫描和定时任务扫描，并说明保修、耗材提醒的去重和状态处理边界。
- AI Client 注释已说明真实 Provider、Mock Provider、Prompt 渲染和分析记录审计字段，强调 AI 只生成建议不覆盖核心业务数据。

验证记录：

- `rg -n "功能说明：.*(数据|基础设施|业务逻辑|请求结果|格式化后的展示数据|维护)|TODO|FIXME|实现.*业务|完成.*操作" backend/src/main/java frontend/src -g "*.java" -g "*.ts" -g "*.vue"`：用于定位泛化注释和调试遗留，本轮优先处理核心基础设施与业务服务。
- 人工复核 `RedisServiceImpl`、`FileResourceServiceImpl`、`ReminderServiceImpl`、`LocalFileStorageService`、`OpenAiCompatibleClient`、`MockAiClient` 的类注释和公开方法注释。
- `cd backend; mvn test -q`：当前 shell 未配置 `mvn` 命令，已改用本地 Maven 绝对路径执行。
- `cd backend; $env:JAVA_HOME="D:\Software\Tools\Java tools\jdk\jdk-21.0.11"; & "D:\Software\Tools\Java tools\Maven\apache-maven-3.9.15\bin\mvn.cmd" test -q`：通过；测试环境 Redis 未启动时仅出现一次简短降级 WARN。

完成结论：

- P11.3 已完成。注释从“说明我在做操作”调整为“说明业务作用和设计取舍”，更适合代码审查和面试追问。

### P11.4 构建与静态检查收口

目标：

- 固化 P11 的最终验证证据，确认代码质量治理没有破坏运行和一键启动配置。

范围：

- 执行后端测试、前端构建、Docker Compose 配置校验和 Git 空白检查。
- 汇总 P11 完成项、遗留项和下一阶段建议。

验收标准：

- P11 四个小版本状态更新为已完成或明确延期。
- `docs/tasks.md` 记录验证命令和结果。
- 工作区最终保持干净并提交推送。

验证记录：

- `cd backend; $env:JAVA_HOME="D:\Software\Tools\Java tools\jdk\jdk-21.0.11"; & "D:\Software\Tools\Java tools\Maven\apache-maven-3.9.15\bin\mvn.cmd" test -q`：通过；本机 `mvn` 不在 PATH，因此使用显式 Maven 路径验证。
- `cd frontend; npm run build`：通过；仍有 Vite 分包体积和第三方库 PURE 注释警告，属于构建告警，不阻塞 P11 收口。
- `docker compose config --quiet`：通过，说明一键启动编排文件语法仍有效。
- `git diff --check`：通过，无尾随空白或空白错误。

P11 总结：

- P11.1 后端规范扫描：确认异常、返回值、事务、配置和第三方调用边界整体合规，并补强 Redis 降级日志。
- P11.2 前端规范扫描：确认 API 封装、Token、路由守卫、分页保护和 Blob 下载例外可解释，并收敛设备详情类型。
- P11.3 注释与命名复核：把核心注释从机械模板改成业务作用和设计取舍说明。
- P11.4 构建与静态检查：后端测试、前端构建、Docker Compose 配置和空白检查均通过。

面试口径：

- 可以说明“P11 不是继续加功能，而是像一次小型代码审查：先扫后端规范，再扫前端边界，再修注释可读性，最后用测试和构建证明质量治理没有破坏系统”。
- 如果被问到为什么不把所有注释都补满，可以回答“注释不是越多越好，P11 重点让注释解释业务意义和架构取舍，比如 Redis 降级、附件鉴权、提醒去重、AI 只辅助”。

完成结论：

- P11 已完成。项目当前从功能闭环、文档对齐进一步进入代码可审查、可解释、可验证的状态。

下一步建议：

- 进入 P12 测试体系补强：优先补 Service 核心业务测试、Controller 权限与参数测试、文件/RustFS 测试边界和前端关键页面构建验证。

## 15. P12 测试体系补强

P12 的目标不是继续新增业务功能，而是把核心业务闭环变成“可证明”的工程质量：通过 Service 测试证明业务规则，通过 Controller 测试证明认证、参数校验和越权拦截，通过前端冒烟检查证明关键页面和 API 封装没有断，通过 Docker 验证脚本证明一键启动后知道如何检查健康状态。

### P12.1 Service 核心业务测试

目标：

- 补强保修、耗材、维修、提醒和 AI 相关 Service 测试，覆盖 P9/P11 后仍容易被追问的边界规则。

范围：

- 保修：默认保修类型、默认提前提醒天数、负数提醒天数拒绝。
- 耗材：停用耗材不能记录更换、未设置上次更换日期时不生成下次提醒日期。
- 维修：终态维修记录不能修改、报废设备不能被维修流程恢复。
- 提醒：逾期保修和逾期耗材生成对应提醒类型。
- AI：保留 Mock Provider 测试，不依赖真实外部 API。

验收标准：

- 新增测试使用 `@DisplayName` 中文说明测试意图。
- 测试验证业务结果和错误码，不只验证方法调用。
- 目标 Service 测试和后端全量测试通过。

验证记录：

- `cd backend; $env:JAVA_HOME="D:\Software\Tools\Java tools\jdk\jdk-21.0.11"; & "D:\Software\Tools\Java tools\Maven\apache-maven-3.9.15\bin\mvn.cmd" "-Dtest=WarrantyServiceTest,ConsumableServiceTest,MaintenanceServiceTest,ReminderServiceTest" test`：通过；目标 Service 测试 28 个用例，失败 0、错误 0、跳过 0。

完成结论：

- P12.1 已完成。保修默认值与负数提醒天数、耗材停用与空更换日期、维修终态和报废设备保护、逾期提醒类型均有测试覆盖。

### P12.2 Controller 与权限测试

目标：

- 补强接口层参数校验、认证拦截和越权访问测试，证明权限不是只靠前端控制。

范围：

- 设备、保修、耗材、维修等关键 Controller 增加无效请求体测试。
- 增加跨家庭空间访问测试，确认非家庭成员访问返回 403 和统一错误码。
- 保持文件下载 `ResponseEntity<Resource>` 作为合理例外，不强行 JSON 包装。

验收标准：

- Controller 测试覆盖 401、400、403 和正常成功路径。
- 越权测试通过真实登录 Token 和真实业务数据构造，不只 Mock Service。
- 目标 Controller 测试通过。

验证记录：

- `cd backend; $env:JAVA_HOME="D:\Software\Tools\Java tools\jdk\jdk-21.0.11"; & "D:\Software\Tools\Java tools\Maven\apache-maven-3.9.15\bin\mvn.cmd" "-Dtest=DeviceAssetControllerTest,WarrantyControllerTest,ConsumableControllerTest,MaintenanceControllerTest" test`：通过；目标 Controller 测试 16 个用例，失败 0、错误 0、跳过 0。

完成结论：

- P12.2 已完成。设备、保修、耗材和维修 Controller 均覆盖未登录 401、参数错误 400、跨家庭访问 403 和正常成功路径。

### P12.3 前端构建与冒烟测试

目标：

- 固化前端关键页面、路由入口和 API 模块的静态冒烟检查，避免页面重构时漏掉核心入口。

范围：

- 增加前端 smoke 脚本，检查关键路由和核心 API 模块入口仍存在。
- 保留 `npm run build` 作为 Vue 类型检查和生产构建门禁。
- 不引入大型 E2E 框架，避免 P12 偏离“轻量质量门禁”的目标。

验收标准：

- `npm run build` 通过。
- `npm run smoke` 通过。
- smoke 脚本只检查项目内文件，不依赖真实后端或浏览器。

验证记录：

- `cd frontend; npm run build`：通过；仍有 Vite 大 chunk 和第三方 PURE 注释警告，不阻塞构建。
- `cd frontend; npm run smoke`：通过；静态检查关键路由、页面文件、API 模块入口、Token 请求头和分页参数保护。

完成结论：

- P12.3 已完成。新增 `frontend/scripts/smoke.mjs` 与 `npm run smoke`，并已加入 CI 前端质量门禁。

### P12.4 Docker 启动验证

目标：

- 固化 Docker 一键启动后的健康检查方式，让面试和自测时能快速判断 MySQL、Redis、RustFS、后端和前端是否正常。

范围：

- 增加项目内 PowerShell 健康检查脚本，检查 Docker Compose 服务状态、前端首页和后端 Actuator 健康端点。
- 校验 `docker-compose.yml` 语法和服务依赖关系。
- 文档说明如果本机镜像或网络不可用，先执行配置校验和脚本 dry-run，不把外部镜像下载失败误判成代码失败。

验收标准：

- `docker compose config --quiet` 通过。
- Docker 健康检查脚本可在项目目录内执行。
- P12 完成记录写入 `docs/tasks.md`，并提交推送。

验证记录：

- `docker compose config --quiet`：通过。
- `powershell -NoProfile -ExecutionPolicy Bypass -File scripts\check-docker-health.ps1 -DryRun`：通过；dry-run 校验 Compose 配置并列出会检查的服务、前端地址和后端健康地址。

完成结论：

- P12.4 已完成。新增 `scripts/check-docker-health.ps1`，实际容器启动后可检查 mysql、redis、rustfs、backend、frontend 运行状态，以及前端首页和后端 Actuator 健康端点。

### P12 总结

完成范围：

- P12.1 Service 核心业务测试：新增 7 个 Service 边界测试，后端总测试数从 80 提升到 95。
- P12.2 Controller 与权限测试：新增 8 个 Controller 参数和越权测试，证明关键业务接口不依赖前端做权限控制。
- P12.3 前端构建与冒烟测试：新增 `npm run smoke`，检查关键页面、路由和 API 入口。
- P12.4 Docker 启动验证：新增 Docker 健康检查脚本，支持 dry-run 和实际容器健康检查。

验证记录：

- `cd backend; $env:JAVA_HOME="D:\Software\Tools\Java tools\jdk\jdk-21.0.11"; & "D:\Software\Tools\Java tools\Maven\apache-maven-3.9.15\bin\mvn.cmd" test`：通过；95 个测试，失败 0、错误 0、跳过 0。
- `cd frontend; npm run build`：通过；仅有已知构建告警。
- `cd frontend; npm run smoke`：通过。
- `docker compose config --quiet`：通过。
- `powershell -NoProfile -ExecutionPolicy Bypass -File scripts\check-docker-health.ps1 -DryRun`：通过。

面试口径：

- 可以说明“P12 的重点不是继续堆功能，而是把已有核心闭环变成可证明：业务规则有 Service 测试、接口边界有 Controller 测试、前端入口有 smoke 检查、Docker 演示有健康检查脚本”。

下一步建议：

- 进入 P13 安全与数据隔离：继续围绕家庭空间隔离、附件鉴权、JWT 敏感信息和边界参数做专项审计。


## 16. P13 安全与数据隔离

P13 的目标是在已有认证、家庭空间权限和文件存储能力基础上做专项安全收口：重点证明用户只能访问所属家庭数据，附件上传不能通过伪装扩展名或 MIME 类型绕过校验，JWT 和日志不泄露敏感信息，分页、ID、日期和金额等边界参数有明确约束。

### P13.1 家庭空间隔离审计

目标：

- 审查设备、保修、耗材、维修、提醒、看板、AI 和附件的家庭空间隔离入口，确认所有 family_id 数据访问前都经过成员校验。

范围：

- 扫描 Service 层 `familyService.checkFamilyMember`、`getDevice(familyId, ...)`、`getWarranty(familyId, ...)`、`getConsumable(familyId, ...)`、`getMaintenance(familyId, ...)` 等隔离模式。
- 对已存在的 Controller 越权测试和 Service 非成员测试做证据归档。
- 不在本小节改业务功能；发现可复现漏洞时转入对应修复小节。

验收标准：

- 明确已覆盖模块和剩余风险。
- 若发现隔离漏洞，必须补测试并修复。

当前记录：

- 已完成扫描，核心业务 Service 均先校验家庭成员，再以 `familyId` 约束业务对象查询。
- AI 故障建议和维修总结通过 `getDevice(familyId, deviceId)` 与 `validateMaintenanceIfPresent(familyId, deviceId, maintenanceId)` 限制跨家庭、跨设备访问。
- 附件上传、列表、下载和删除均先校验家庭成员，再校验附件元数据或业务对象归属。
- 本轮发现名称补全类查询存在隐性隔离风险：设备分类名、维修/保修/耗材/提醒中的设备名补全不能只按 ID 回查，否则异常外键数据可能暴露其他家庭名称。
- 已将设备、AI、保修、耗材、维修和提醒中的分类/设备名称补全统一改为带 `familyId` 的 `selectOne/selectList`，保留批量加载避免 N+1。

验证记录：

- 先新增 `DeviceAssetServiceFamilyIsolationTest` 与 `MaintenanceServiceFamilyIsolationTest` 复现异常外键泄露名称风险，旧实现下失败。
- 修复后执行 `cd backend; $env:JAVA_HOME="D:\Software\Tools\Java tools\jdk\jdk-21.0.11"; $env:Path="$env:JAVA_HOME\bin;$env:Path"; & "D:\Software\Tools\Java tools\Maven\apache-maven-3.9.15\bin\mvn.cmd" "-Dmaven.repo.local=repository" "-Dtest=AiServiceTest,DeviceAssetServiceFamilyIsolationTest,DeviceAssetServiceNPlusOneTest,MaintenanceServiceFamilyIsolationTest,MaintenanceServiceNPlusOneTest,WarrantyServiceNPlusOneTest,ConsumableServiceNPlusOneTest,ReminderServiceTransactionBoundaryTest" test`：通过；15 个测试失败 0、错误 0、跳过 0。

完成结论：

- P13.1 家庭空间隔离审计已完成。所有核心数据入口保留家庭成员校验，业务对象读取和名称补全均带 `familyId` 约束；已补回归测试防止后续重新引入只按 ID 补全名称的泄露风险。

### P13.2 附件安全审计

目标：

- 强化附件上传安全，除扩展名和 MIME 类型外，增加文件头魔数校验，阻断把文本或可执行内容伪装成图片/PDF 上传。

范围：

- 后端附件上传校验。
- 附件相关 Service / Controller 测试数据调整为真实 JPEG、PNG、PDF 文件头。
- 补充伪装文件回归测试。

验收标准：

- `.jpg/.jpeg` 必须匹配 JPEG 文件头和 `image/jpeg`。
- `.png` 必须匹配 PNG 文件头和 `image/png`。
- `.pdf` 必须匹配 `%PDF-` 文件头和 `application/pdf`。
- 内容与扩展名或 MIME 类型不一致时返回 `FILE_TYPE_NOT_ALLOWED`。

验证记录：

- 先新增 `FileResourceServiceTest#rejectSpoofedFileContent`，当前实现下失败，证明只校验扩展名和 MIME 类型不足。
- 已在 `FileResourceServiceImpl` 增加 JPEG、PNG、PDF 魔数校验。
- `cd backend; $env:JAVA_HOME="D:\Software\Tools\Java tools\jdk\jdk-21.0.11"; $env:Path="$env:JAVA_HOME\bin;$env:Path"; & "D:\Software\Tools\Java tools\Maven\apache-maven-3.9.15\bin\mvn.cmd" "-Dmaven.repo.local=repository" "-Dtest=FileResourceServiceTest#rejectSpoofedFileContent" test`：通过。
- `cd backend; $env:JAVA_HOME="D:\Software\Tools\Java tools\jdk\jdk-21.0.11"; $env:Path="$env:JAVA_HOME\bin;$env:Path"; & "D:\Software\Tools\Java tools\Maven\apache-maven-3.9.15\bin\mvn.cmd" "-Dmaven.repo.local=repository" "-Dtest=FileResourceServiceTest,FileResourceControllerTest,FileResourceServiceCompensationTest" test`：通过；附件相关 11 个测试失败 0、错误 0、跳过 0。

完成结论：

- P13.2 附件安全审计已完成主要修复。当前允许的图片和 PDF 类型都具备扩展名、MIME、魔数三层校验；附件鉴权和归属校验沿用已有家庭空间隔离逻辑。

### P13.3 认证与敏感信息审计

目标：

- 审查 JWT、密码、Token、日志和响应脱敏，确认不会在接口响应或日志中暴露密码哈希、Token、API Key 等敏感信息。

范围：

- 扫描认证响应、用户响应、日志语句、配置文件和测试数据。
- 复核 JWT 黑名单 Redis 失败时的 fail-safe 行为。

验收标准：

- 不直接返回 `UserEntity` 或 `passwordHash`。
- 日志不输出密码、Token、API Key。
- JWT 退出黑名单写入失败不能静默成功，读取失败按 Token 失效处理。

验证记录：

- 已执行敏感关键词扫描：`password`、`passwordHash`、`token`、`accessToken`、`refreshToken`、`Authorization`、`api-key`、`secret` 和 `log.`。
- 认证响应只返回 `RegisterResponse`、`LoginResponse` 和 `UserProfileResponse`，不直接返回 `UserEntity`，不包含 `passwordHash`。
- 日志未发现直接输出密码、完整 Token、API Key 或 Secret；JWT 黑名单读取失败时记录堆栈但不记录 Token 内容。
- `JwtBlacklistServiceTest` 已覆盖 Redis 黑名单读失败按 Token 失效处理、写失败转业务异常，避免退出登录静默成功。

完成结论：

- P13.3 认证与敏感信息审计已完成。密码只以哈希入库，接口响应不暴露密码哈希；JWT 退出依赖 Redis 黑名单 fail-safe；日志敏感信息风险可控，README/docs 中出现的密码和 Token 均为演示或占位示例。

### P13.4 边界参数审计

目标：

- 审查分页上限、ID 越权、日期范围、金额和枚举状态边界，避免非法参数进入业务状态流转或造成资源滥用。

范围：

- PageQuery、日期查询、金额字段、状态更新请求、AI 输入长度和文件大小限制。
- 必要时补充边界测试。

验收标准：

- 分页上限保持 100。
- 日期、金额、状态枚举和文件大小等边界都有后端约束。
- 新增或确认对应测试证据。

验证记录：

- 已复核 `PageQuery` 分页上限：`pageSize` 通过 `@Max(100)` 限制。
- 已复核查询窗口：保修/耗材 due soon 天数限制在 `0..3650`，首页趋势月份限制在 `1..24`，提醒日历和维修费用统计校验结束日期不得早于开始日期。
- 已为金额字段按数据库 `DECIMAL(12,2)` 增加 `@Digits(integer = 10, fraction = 2)`：设备购买价、耗材更换费用、维修费用。
- 已新增 Controller 边界测试：分页超过 100、设备金额整数位超限、耗材金额小数位超限、首页趋势月份超限、提醒日历日期倒挂。
- `cd backend; $env:JAVA_HOME="D:\Software\Tools\Java tools\jdk\jdk-21.0.11"; $env:Path="$env:JAVA_HOME\bin;$env:Path"; & "D:\Software\Tools\Java tools\Maven\apache-maven-3.9.15\bin\mvn.cmd" "-Dmaven.repo.local=repository" "-Dtest=DeviceAssetControllerTest,ConsumableControllerTest,DashboardControllerTest" test`：通过；15 个测试失败 0、错误 0、跳过 0。

完成结论：

- P13.4 边界参数审计已完成。分页、日期窗口、月份窗口、状态枚举、文件大小、AI 输入长度和金额精度均有后端约束，并已补充对应回归测试证据。

## 17. P14 演示体验完善

P14 的目标是把已有 MVP 和 P10-P13 的质量、安全成果包装成可重复演示的体验：演示数据可解释、启动路径清晰、5-10 分钟路线稳定、README 能快速导航，常见问答能覆盖架构、数据库、Redis、AI、文件存储和安全追问。

### P14.1 演示数据整理

目标：

- 固化 `demo / fixledger123`、`演示家庭`、三台示例设备和保修/耗材/维修/提醒/附件/AI 数据的讲解地图。

范围：

- 复核 `backend/src/main/resources/db/demo-data.sql` 的演示用户、家庭空间、设备、提醒和 AI 留痕。
- 在 `docs/demo-guide.md` 和 `docs/database.md` 说明演示数据含义、固定日期基准和附件元数据限制。
- 不改生产表结构，不新增与家庭设备生命周期无关的演示业务。

完成记录：

- 已新增 `docs/demo-guide.md`，整理演示账号、数据地图、启动命令、重置方式和排障预案。
- 已在 `docs/database.md` 增加 P14 演示数据地图，明确三个设备分别承担净水器生命周期、吸尘器耗材提醒和路由器维修/AI 场景。
- 已说明初始化 SQL 中的附件记录主要是元数据样例，真实下载需要通过页面上传文件写入 RustFS。

### P14.2 面试演示路径

目标：

- 整理可在 5 分钟内讲清核心闭环、10 分钟内展开技术追问的演示路线。

范围：

- 以“我的家 -> 设备护照 -> 设备详情 -> 耗材 -> 维修 -> 凭证盒 -> 智能助手”为主线。
- 把 OpenAPI、Docker Compose、Redis、P13 安全审计作为追问支线，而不是抢主线。

完成记录：

- 已在 `docs/demo-guide.md` 补充 5 分钟路线和 10 分钟技术路线。
- 已更新 `docs/interview-guide.md`，把 P11/P12/P13/P14 纳入已完成阶段，并补充现场兜底口径。

### P14.3 README 展示增强

目标：

- 让 README 能承担项目入口职责：快速启动、演示账号、功能亮点、文档导航、演示路线和后续状态都能直接找到。

范围：

- 增加 `docs/demo-guide.md` 文档导航。
- 更新效果展示，从“后续补截图”调整为“按真实环境可重复演示的页面、讲解重点和截图策略”。
- 更新当前完善状态到 P14。

完成记录：

- README 已新增 P14 演示指南入口。
- README 效果展示已按登录页、我的家、家庭日历、设备护照、设备详情、耗材、维修、凭证盒和智能助手整理，并说明 P14 不把静态截图作为唯一展示依据。
- README 底部状态已从 P10 更新到 P14，并把 P15 作为下一阶段。

### P14.4 常见问答材料

目标：

- 面试追问时能快速解释架构、数据库、Redis、AI、文件存储、安全边界和延期项。

范围：

- 在 `docs/demo-guide.md` 汇总高频问答速答。
- 在 `docs/interview-guide.md` 补充附件下载、截图策略和 P14 后续计划口径。
- 保持“AI 是辅助、文件访问走后端鉴权、家庭空间隔离是核心权限模型”的一致表达。

完成记录：

- 已补充 P14 高频问答速答，覆盖普通后台差异、家庭空间隔离、Redis、RustFS、AI 和延期项。
- 已在面试指南补充附件下载失败和不提交静态截图的说明，避免演示现场把元数据样例误当真实对象文件。

### P14 验证记录

- `rg -n "P14|demo-guide|演示路线|后续建议" docs README.md`：通过，已确认 README、演示指南、面试指南、数据库文档和任务记录均出现 P14 演示入口与下一步 P15 口径。
- `git diff --check`：通过；仅出现既有 LF/CRLF 换行提示，无空白错误。
- `docker compose config --quiet`：通过，P14 文档中的一键启动配置仍可解析。

完成结论：

- P14 演示体验完善已完成文档与演示材料收口。项目现在可以通过 README 找到演示指南，通过 `docs/demo-guide.md` 走稳定路线，通过 `docs/interview-guide.md` 回答常见追问，通过 `docs/database.md` 解释演示数据来源。


## 18. MVP 验收清单

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

## 19. 推荐开发顺序

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

## 20. 面试展示优先级

优先打磨这些能力：

1. 设备详情聚合页。
2. 保修和耗材提醒。
3. 维修状态流转。
4. Redis 提醒去重。
5. AI 故障排查建议。
6. 我的家场景首页。
7. 设备护照和凭证盒。

## 21. 风险与控制

| 风险 | 控制方式 |
| --- | --- |
| 功能范围变大 | 先完成核心闭环，其他放二期 |
| AI 接口不可用 | 默认 Mock Provider |
| 文件存储复杂 | Docker 默认 RustFS，测试保留本地文件，MinIO/S3 通过同一抽象可替换 |
| 前端页面过多 | 优先设备详情、首页、列表页 |
| 定时任务难测试 | 提供手动扫描接口 |
| 权限遗漏 | 所有业务接口统一校验 familyId |

## 22. 当前状态

截至当前文档版本：

- README 已完成。
- AGENTS 规范已完成。
- docs 开发资料已完成第一版。
- 后端 P0-P7 已完成：脚手架、认证与家庭空间、设备分类与设备档案、保修记录与附件、耗材与维修、提醒与看板、AI 辅助、后端工程化。
- 前端 MVP 页面已完成：登录注册、主布局、首页看板、设备档案、设备详情、保修管理、耗材管理、维修记录、维修详情、提醒中心、附件库、AI 助手和家庭设置。
- 后端完整 Maven 测试已通过，当前共 113 个测试用例，失败 0、错误 0、跳过 0。
- 前端 `npm run build` 已通过，Vue 类型检查和 Vite 构建均成功。
- Docker Compose 已支持一键启动前端、后端、MySQL、Redis 和 RustFS，前端通过 Nginx 代理后端 API。
- P8 产品化体验重构已启动：首页升级为“我的家”，导航从后台模块转向家庭场景入口。
- 已完成一次项目注释审查：补充后端核心类/公开方法 Javadoc，以及前端 API、Store、路由和字典工具注释。
- P9 系统性完善阶段已完成：文档对齐、代码质量、安全测试、RustFS、Skills 文档规范、面试材料、产品体验、CI 门禁和全量验收均已收口。
- P10 文档深度对齐已完成：需求、架构、接口、数据库、UI、README 与当前实现和演示体验保持一致。
- P11 代码质量治理已完成：异常、日志、事务、配置、前端 API 封装和构建静态检查已收口。
- P12 测试体系补强已完成：Service 边界、Controller 参数与越权、前端 smoke 和 Docker 健康检查脚本均已收口。
- P13 安全与数据隔离已完成：家庭空间隔离、附件安全、JWT 敏感信息和边界参数均已补强并通过全量测试。
- P14 演示体验完善已完成：演示指南、演示数据地图、README 展示增强和常见问答材料已收口。

下一步建议：

```text
P14 已完成，下一步建议进入 P15 产品体验升级与可选增强，继续深化设备护照、凭证盒、附件预览、真实 AI Provider、通知渠道和自动化流水线。
```


## 23. P15 产品体验升级与可选增强

P15 的目标是在不扩大业务边界的前提下继续弱化后台感，把页面视觉调整为更接近米家 App 的家庭设备体验：浅色空间、柔和卡片、设备状态可感知、房间与设备卡片优先，表格作为辅助能力保留。

### P15.1 米家风格视觉基线与设备护照深化

目标：

- 建立接近米家 App 的前端视觉基线：暖白背景、柔和阴影、圆角设备卡、状态色克制、移动端单列优先。
- 优先改造登录页、主布局、我的家首页、设备护照和设备详情，让演示第一眼更像家庭设备应用。

范围：

- `frontend/src/styles/main.css`：统一颜色、字体、卡片、按钮、表单、表格和动效基础。
- `frontend/src/layouts/MainLayout.vue`：侧边导航和顶部栏改为更轻的家庭设备控制台风格。
- `frontend/src/views/auth/LoginView.vue`：登录页改为米家式浅色欢迎页和设备能力卡片。
- `frontend/src/views/dashboard/DashboardView.vue`：首页 Hero、健康分、指标卡、日历和任务卡视觉升级。
- `frontend/src/views/devices/DeviceListView.vue`、`DeviceDetailView.vue`：设备卡片和设备详情护照深化。

验收标准：

- 页面仍能正常登录、切换家庭、查看首页和设备页面。
- `npm run build` 通过。
- Docker 前端镜像可重新构建并通过 `http://localhost:5173` 访问。
- 不引入新业务方向，不把项目改成泛智能家居控制平台。

当前记录：

- 已启动 P15.1，先补充任务记录和 UI 方向，再进行前端视觉改造。
- 已完成首批米家风格视觉改造：全局视觉基线、主布局、登录页、我的家首页、设备护照和设备详情。
- 已保留家庭设备生命周期定位，没有新增智能家居控制、商城或企业资产盘点方向。

验证记录：

- `cd frontend; npm run build`：通过；仅保留 Rollup PURE 注释和大 chunk 的既有构建告警。
- `docker compose build frontend`：通过，前端镜像已重新构建。
- `docker compose up -d frontend`：通过，`fixledger-frontend` 已重新启动。
- `Invoke-WebRequest http://localhost:5173`：返回 200，页面标题为 `FixLedger 家庭设备档案本`。
- `git diff --check -- frontend/src/styles/main.css frontend/src/layouts/MainLayout.vue frontend/src/views/auth/LoginView.vue frontend/src/views/dashboard/DashboardView.vue frontend/src/views/devices/DeviceListView.vue frontend/src/views/devices/DeviceDetailView.vue docs/tasks.md docs/ui.md`：通过；仅有 LF/CRLF 换行提示，无空白错误。


### P15.1.1 公开首页

目标：

- 增加一个公开访问的产品首页，参考 `https://home.mi.com/` 的浅色官网气质，并根据体验反馈收敛为简洁、优雅的登录引导入口。
- 首页用于项目展示和面试开场，强调家庭设备生命周期管理，不做商城、不做 IoT 控制台。

范围：

- 新增公开首页页面，路由为 `/`。
- 登录、注册和应用内页面保持原有功能；登录后进入 `/dashboard`。
- 首页只使用项目自有文案和 CSS 形状，不复制米家品牌素材、Logo 或图片。

验收标准：

- 未登录访问 `http://localhost:5173/` 能看到公开首页。
- 点击“登录 / 体验演示”能进入登录页。
- 已登录用户仍可访问 `/dashboard`、`/devices` 等应用内页面。
- `npm run build` 通过，Docker 前端镜像可重新构建。

当前记录：

- 已启动公开首页开发，先补充任务记录和 UI 文档，再新增页面与路由。
- 已新增 `frontend/src/views/home/HomeLandingView.vue`，以浅色官网、顶部导航、大 Hero、能力卡片、场景区和工程可信度作为首页结构。
- 已调整前端路由：`/` 为公开首页，`/dashboard` 仍是登录后的我的家工作台，`/login` 和 `/register` 继续公开访问。
- 首页 CTA 指向登录页，保持演示账号由登录页主动填入。
- 根据体验反馈，公开首页和登录页去掉大 Hero、大字和宣传型功能矩阵，调整为轻量首页入口与单列居中登录卡片。

验证记录：

- `cd frontend; npm run build`：通过；仅保留 Rollup PURE 注释和大 chunk 的既有构建告警。
- `git diff --check -- frontend/src/router/index.ts frontend/src/views/home/HomeLandingView.vue docs/tasks.md docs/ui.md`：通过；仅有 LF/CRLF 换行提示，无空白错误。
- `docker compose build frontend; docker compose up -d frontend`：通过，前端镜像已重新构建并重启。
- `Invoke-WebRequest http://localhost:5173/` 与 `Invoke-WebRequest http://localhost:5173/login`：均返回 200。
- 体验回调调整后，`cd frontend; npm run build` 通过；普通权限首次遇到旧 `dist` 文件 EPERM，提升权限重跑通过，仍只保留既有 Rollup PURE 注释和大 chunk 告警。
- 体验回调调整后，`cd frontend; npm run smoke` 通过。
- 体验回调调整后，`git diff --check` 通过；仅有 LF/CRLF 换行提示，无空白错误。


### P15.2 凭证盒深化

目标：

- 将 `/files` 从普通附件列表升级为“凭证盒”体验，突出家庭设备凭证归档、完整度和可预览能力。
- 保持现有后端附件接口不变，先通过前端信息架构完成凭证分类、设备关联和演示体验升级。

范围：

- `frontend/src/views/files/FileLibraryView.vue`：改为设备优先的凭证盒页面，支持凭证类型切换、完整度统计、上传、下载、删除和预览。
- `frontend/src/api/file.ts`：补充附件预览 Blob URL 能力，下载仍保持浏览器保存。
- `docs/ui.md`、`docs/tasks.md`：同步凭证盒体验边界和完成记录。

验收标准：

- 能按设备选择凭证归档对象，并按发票/说明书/保修/维修/耗材类型查看附件。
- 图片和 PDF 可以在页面中预览；不支持预览的类型仍提供下载。
- 页面保留后端鉴权下载路径，不暴露对象存储地址。
- `npm run build` 和 `npm run smoke` 通过。

当前记录：

- 已启动 P15.2，先按现有接口完成前端体验升级，不新增表结构和后端路由。
- 完整度采用前端按当前设备已查询凭证类型计算，后续如需要跨业务对象汇总，可再扩展后端聚合接口。
- 已将 `/files` 改为设备优先的凭证盒：先选择设备，再按发票、说明书、保修、维修和耗材类型查看凭证。
- 已补充图片/PDF 预览，预览数据仍来自后端鉴权下载流，不暴露 RustFS 对象地址。
- 保修、维修和耗材凭证按真实业务记录 ID 查询和上传，不把设备 ID 误用为业务记录 ID。

验证记录：

- `cd frontend; npm run build`：首次在清理旧 `dist/assets` 时遇到 Windows `EPERM unlink`，提升权限重跑通过；仅保留 Rollup PURE 注释和大 chunk 的既有构建告警。
- `cd frontend; npm run smoke`：通过，前端关键页面、路由和 API 契约检查成功。
- `git diff --check`：通过；仅有 LF/CRLF 换行提示，无空白错误。

完成结论：

- P15.2 已完成。凭证盒已从后台式附件查询页升级为设备优先、按凭证类型组织、支持图片/PDF 预览的家庭凭证归档体验。

### P15.3 存储与 AI 增强

目标：

- 明确当前 RustFS/S3、本地存储、OpenAI-compatible Client 和 Mock AI 的边界，让增强能力可配置、可演示、可降级。
- 不把真实 AI Provider 或对象存储临时 URL 变成核心业务强依赖。

范围：

- 凭证预览继续走后端鉴权转发下载，不直接暴露 RustFS/MinIO 临时 URL。
- AI 继续保留 Mock Provider 和 OpenAI-compatible Provider 两条路径，真实 Provider 需要通过环境变量启用。
- 文档说明 P15 阶段的取舍：文件预览先做前端 Blob 预览，全文搜索/OCR/临时 URL 仍是后续增强。

验收标准：

- README、架构和 UI 口径不夸大当前 AI 和对象存储能力。
- 前端预览失败时不影响下载和核心附件管理。
- 不新增真实密钥或硬编码 Provider 配置。

当前记录：

- P15.3 选择先完成文件预览体验与文档口径收口，不新增真实 Provider 密钥和外部服务依赖。
- 当前 AI 基础设施已经支持 `MOCK` 和 `OPENAI_COMPATIBLE` 两种 Provider，真实调用需要显式启用并配置 `apiKey`、`baseUrl` 和 `model`。
- P15.3 当时凭证预览采用后端下载接口返回 Blob，浏览器生成对象 URL；对象存储临时访问 URL、OCR 和 PDF 全文搜索继续作为后续增强，后续 P16.2 已完成文本型 PDF/文件名搜索第一版。

完成结论：

- P15.3 已完成本轮收口。当前增强重点落在“可安全预览、可解释配置、可离线演示”，没有把真实 AI Provider 或对象存储临时 URL 变成核心依赖。

### P15.4 自动化与通知增强

目标：

- 复核当前 CI、Docker 健康检查、站内通知和延期通知渠道，形成 P15 可交付边界。
- 邮件/Webhook 和操作日志如果不进入本轮实现，必须写清原因和后续落点。

范围：

- 继续使用现有 GitHub Actions：后端测试、前端构建、前端 smoke、Docker Compose 配置校验和 Docker health dry-run。
- 通知能力当前保持站内通知；邮件/Webhook 作为后续通知基础设施扩展，不阻塞家庭设备核心闭环。
- 在任务记录中补充 P15 收口验证。

验收标准：

- `npm run build`、`npm run smoke`、`git diff --check` 通过。
- 文档能解释为什么 P15 优先做凭证盒和预览，而不是先接邮件/Webhook。

当前记录：

- P15.4 本轮不新增邮件/Webhook 实现，避免引入投递配置、失败重试、敏感配置和通知审计表的半成品能力。
- 当前自动化仍以 GitHub Actions 和本地脚本为主：后端测试、前端构建、前端 smoke、Docker Compose 配置校验和 Docker health dry-run。
- 操作日志和独立 NotificationService 保持为后续增强，当前站内通知仍由提醒模块同事务写入。

完成结论：

- P15.4 已完成本轮收口。自动化与通知边界已明确：当前保留稳定 CI 与站内通知，邮件/Webhook、操作日志和更完整部署流水线作为后续专项。

### P15 总结

完成范围：

- P15.1 米家风格视觉基线：全局样式、主布局、登录页、我的家、设备护照和设备详情完成首批产品化改造。
- P15.1.1 公开首页：新增 `/` 产品首页，登录后的工作台仍保留 `/dashboard`。
- P15.2 凭证盒深化：设备优先的凭证盒、凭证完整度、按类型归档、图片/PDF 预览和安全下载已完成。
- P15.3 存储与 AI 增强：文件预览走后端鉴权 Blob，AI Provider 配置边界和 Mock 兜底口径已收口。
- P15.4 自动化与通知增强：CI、Docker health dry-run、站内通知和后续邮件/Webhook/操作日志边界已收口。

验证记录：

- `cd frontend; npm run build`：通过；首次因旧 `dist` 文件删除权限失败，提升权限重跑成功。
- `cd frontend; npm run smoke`：通过。
- `git diff --check`：通过；仅有 LF/CRLF 换行提示。

下一步建议：

- 如继续增强，优先进入 P16：凭证聚合后端接口、说明书 PDF 全文搜索、OCR 辅助录入、邮件/Webhook 通知基础设施、操作日志和更完整 E2E。


## 24. P16 凭证聚合与智能归档增强

P16 的目标是在 P15 凭证盒前端体验基础上，把“设备、保修、维修、耗材、附件”的拼装逻辑逐步收回后端，减少前端重复请求和业务知识泄漏，并逐步补齐说明书搜索、OCR 和凭证自动分类基础。

### P16.1 凭证盒后端聚合接口

目标：

- 新增设备维度凭证盒聚合接口，让前端一次获取设备凭证、说明书、保修凭证、维修凭证和耗材凭证。
- 后端统一处理家庭空间校验、业务对象归属、附件分组和完整度统计，避免前端把设备 ID 误当作保修/维修/耗材记录 ID。

接口契约：

```text
GET /api/families/{familyId}/devices/{deviceId}/credential-box
```

响应内容：

- `deviceId`、`deviceName`、`location`。
- `completionPercent`、`archivedTypeCount`、`totalTypeCount`、`totalFileCount`、`totalFileSize`。
- `groups[]`：每个凭证类型包含 `bizType`、`title`、`shortTitle`、`description`、`targets[]`、`files[]`。
- `targets[]`：可挂载对象，设备/说明书指向设备 ID，保修/维修/耗材指向对应业务记录 ID。
- `files[]`：附件元数据，额外带 `targetLabel`，前端无需再维护业务对象名称映射。

范围：

- 后端新增凭证盒响应 DTO、Service 方法和 Controller 路由。
- 后端补充聚合接口测试，覆盖设备、说明书、保修、维修、耗材附件分组和文件数量统计。
- 前端 `/files` 改为优先调用聚合接口，上传/下载/预览仍复用现有附件接口。
- 同步 `docs/api.md`、`docs/ui.md` 和任务记录。

验收标准：

- 聚合接口只允许家庭成员访问，并且所有业务对象查询都带 `familyId` 和 `deviceId`。
- 前端凭证盒加载时不再分别调用设备保修、维修、耗材和多次附件查询接口。
- `mvn -Dtest=FileResourceControllerTest test`、`npm run build`、`npm run smoke` 和 `git diff --check` 通过。

本轮完成：

- 后端新增 `GET /api/families/{familyId}/devices/{deviceId}/credential-box` 聚合接口，固定返回设备凭证、说明书、保修、维修和耗材五类分组。
- `FileResourceService` 统一校验家庭成员、设备归属，并按 `familyId + deviceId` 批量读取保修、维修、耗材和附件，避免前端多次拼装造成的 N+1 查询风险。
- 前端 `/files` 已切换为调用凭证盒聚合接口；上传、删除、预览和下载继续复用原附件接口。
- `docs/api.md` 和 `docs/ui.md` 已补充 P16.1 聚合接口契约与页面数据流说明。

验证：

- 后端目标测试：`mvn -Dmaven.repo.local=repository -Dtest=FileResourceControllerTest#getCredentialBoxAggregatesDeviceRelatedFiles test` 通过。
- 后端附件测试：`mvn -Dmaven.repo.local=repository -Dtest=FileResourceControllerTest,FileResourceServiceTest test` 通过，11 个测试全部通过。
- 前端构建：`npm run build` 通过；Windows 普通权限下旧 `dist` 文件偶发 EPERM，提升权限重跑成功。
- 前端冒烟：`npm run smoke` 通过。
- 静态空白检查：`git diff --check` 通过，仅保留 Git 的 LF/CRLF 换行提示。

### P16.2 说明书全文搜索

目标：

- 在 P16.1 设备凭证盒聚合基础上，让已归档的说明书可以按关键词搜索。
- 第一版只做离线可演示能力：索引文本型 PDF 中可提取的文字和说明书文件名，不接真实 OCR、不依赖真实 AI Provider。
- 搜索结果返回匹配说明书附件、文件名和短片段，避免向前端返回完整说明书文本。

接口契约：

```text
GET /api/families/{familyId}/devices/{deviceId}/manuals/search?keyword=reset
```

范围：

- 新增说明书文本索引表、Entity、Mapper 和响应 DTO。
- 上传 `MANUAL` 附件后，后端尝试提取文本并写入索引；提取失败不影响附件上传。
- 删除说明书附件时同步逻辑删除文本索引。
- 前端凭证盒在说明书分组内提供关键词搜索，结果可继续复用预览/下载能力。
- 同步 `docs/api.md`、`docs/database.md`、`docs/ui.md` 和任务记录。

验收标准：

- 搜索接口只允许家庭成员访问，并且查询必须同时带 `familyId` 和 `deviceId`。
- 搜索空关键词或过长关键词返回参数错误。
- 上传文本型说明书 PDF 后可通过关键词命中，扫描件/OCR 不作为本轮阻塞项。
- 后端文件测试、前端构建、前端冒烟和 `git diff --check` 通过。

完成记录：

- 已新增 `fl_manual_text_index` 表、Entity、Mapper 和 `ManualSearchResponse`。
- 已在 `FileResourceService` 中实现说明书上传索引、删除索引清理和设备维度搜索，搜索结果只返回附件元数据与短片段。
- 已新增 `GET /api/families/{familyId}/devices/{deviceId}/manuals/search`，并补充说明书搜索成功与空关键词拒绝测试。
- 已在 `/files` 凭证盒说明书分组接入关键词搜索，搜索结果可继续预览或下载原说明书。
- 已同步 `docs/api.md`、`docs/database.md` 和 `docs/ui.md`，明确 P16.2 第一版不包含 OCR 和复杂 PDF 解析。

验证记录：

- `mvn -Dmaven.repo.local=repository -Dtest=FileResourceControllerTest#searchManualsByKeyword+searchManualsRejectsBlankKeyword test` 通过。
- `mvn -Dmaven.repo.local=repository -Dtest=FileResourceControllerTest,FileResourceServiceTest,FileResourceServiceCompensationTest test` 通过。
- `npm run build` 通过；普通权限首次因 Windows 旧 `dist` 文件 `EPERM` 失败，提升权限清理后构建成功。
- `npm run smoke` 通过。
- `git diff --check` 通过，仅输出 Git 的 LF/CRLF 换行提示。


## 25. P17 前端体验重做

P17 的目标是根据用户反馈推倒重做上一版前端视觉。上一版参考米家 App/官网时出现了暖橙大卡片、大 Hero 和宣传页式大字问题，已被明确否定；本阶段改为简洁、克制、实用的家庭设备工作台。随后用户继续明确“不用旧绿色”，P17.7 又把蓝紫 Liquid Glass 收敛为浅色产品工具风：银白玻璃、克制橙色主操作、浅灰白面板和低饱和辅助色。P17.5 对公开首页做单独返工，允许参考 `home.mi.com` 的浅色留白、顶部导航和产品预览节奏，但不复制米家品牌、不做商城导购，也不回到旧的大字海报。

### P17.1 简洁实用视觉重做

目标：

- 使用 `ui-ux-pro-max` 和 `frontend-ui-engineering` skills 重新建立前端视觉基线。
- 全局废弃暖橙渐变、大 Hero、装饰球、拟物图钉、大圆角重阴影和宣传页式大字。
- 保留家庭设备生命周期业务定位，页面第一眼能看到待办、设备、凭证、筛选和操作。

范围：

- `frontend/src/styles/main.css`：收敛为银蓝浅底、系统蓝主色、淡紫辅助 tint、半透明玻璃层、细高光边框和低阴影。
- `frontend/src/layouts/MainLayout.vue`：保持简洁侧边导航，菜单文案改为总览、日历、设备、凭证、辅助、家庭设置。
- `frontend/src/views/home/HomeLandingView.vue`、`LoginView.vue`、`RegisterView.vue`：去掉大字和宣传感，改为轻量入口与清晰表单。
- `frontend/src/views/dashboard/DashboardView.vue`：首页工作台化，提醒日历用状态点替代拟物图钉。
- `frontend/src/views/devices/DeviceListView.vue`、`DeviceDetailView.vue`：文案从设备护照收敛为设备档案，去掉装饰封面和装饰球。
- `frontend/src/views/files/FileLibraryView.vue`：凭证盒改为完整度面板、类型切换、说明书搜索和附件清单，不再使用暖色大卡片。
- `frontend/src/views/ai/AiToolsView.vue`、`MaintenanceDetailView.vue`、`FamilySettingsView.vue`：弱化 AI 主角感和旧色变量。
- `docs/ui.md`、`docs/tasks.md`：同步当前视觉方向和历史调整说明。

验收标准：

- 搜索前端源码不再命中旧米家变量、暖色渐变、大 Hero、装饰球和拟物图钉关键词。
- `npm run build`、`npm run smoke`、`git diff --check` 通过。
- 浏览器打开 `/`、`/login`、`/dashboard`、`/devices`、`/files` 时视觉应统一、简洁、实用，不出现明显重叠或大标题遮挡。

当前记录：

- 已完成首轮视觉重做：全局样式、主布局、首页、登录注册、总览、设备档案、设备详情、凭证盒、辅助工具和部分业务页样式已收敛。
- 已通过 `rg` 扫描确认旧米家变量、暖色渐变、`hero/orb/pushpin` 等旧视觉关键词已清除。
- 已使用临时生产预览检查 `/` 和 `/login` 桌面/移动端结构，后端未启动时应用内数据页无法做登录后浏览器联调。

验证记录：

- 旧风格扫描：`rg` 检查旧米家变量、暖色渐变、大 Hero、装饰球、拟物图钉和过大字号关键词，无命中。
- 类型检查：`npx vue-tsc --noEmit -p tsconfig.json` 通过。
- 临时生产构建：`npx vite build --outDir dist-codex --emptyOutDir` 通过；仅保留 Rollup PURE 注释和大 chunk 的既有构建告警。
- 前端冒烟：`npm run smoke` 通过。
- 静态空白检查：`git diff --check` 通过；仅有 Git 的 LF/CRLF 换行提示。
- 标准 `npm run build`：类型检查和模块转换通过，但 Windows 当前旧 `frontend/dist` 文件被系统拒绝删除，报 `EPERM unlink frontend/dist/assets/...`；已用临时输出目录完成等价构建验证，未修改生产构建配置。

### P17.2 Liquid Glass 视觉修正

用户反馈：不使用旧绿色，改成类似 iOS 液态玻璃的浅色、新潮、通透风格，同时保持实用性。

调整范围：

- `frontend/src/styles/main.css`：主色改为 `#0A84FF` 系统蓝，加入淡紫、浅青、银白玻璃层、`backdrop-filter` 和高光边框 token。
- `frontend/src/layouts/MainLayout.vue`：侧边栏、顶部栏、家庭切换和菜单选中态统一为浅色玻璃层。
- `frontend/src/views/home/HomeLandingView.vue`、`LoginView.vue`、`RegisterView.vue`：保持小字号、低宣传感，但改为更轻的新潮玻璃面板。
- `frontend/src/views/dashboard/DashboardView.vue`：首页总览、健康分、日历、提醒卡和图表色全部去掉旧绿色，改为蓝紫玻璃和状态语义色。
- `frontend/src/views/devices/DeviceListView.vue`、`DeviceDetailView.vue`、`FileLibraryView.vue`、`FamilySettingsView.vue`：去掉硬白块和旧绿色 hover/focus，统一为银白玻璃卡片。
- `docs/ui.md`、`docs/tasks.md`：同步当前视觉方向，不再把旧绿色作为当前规范。

验收补充：

- 搜索前端源码和当前规范不应再命中旧绿色主色、旧深绿色强调色或旧绿色状态 rgba。
- 页面可保留少量白色高光和白色文字，但不能回到硬白后台卡片或绿色品牌风。

### P17.3 系统级玻璃质感细化

目标：在 P17.2 的浅色 Liquid Glass 基础上继续优化，让视觉更接近系统级透明磨砂 UI，而不是普通玻璃拟态卡片。

调整范围：

- `frontend/src/styles/main.css`：新增更细的玻璃材质 token，统一按钮、输入框、弹窗、抽屉和下拉层的磨砂、高光、hover、focus、pressed 状态。
- `frontend/src/layouts/MainLayout.vue`：侧边栏、顶部栏、家庭面板、菜单 hover/active 改为更轻的悬浮系统栏质感。
- `frontend/src/views/home/HomeLandingView.vue`、`LoginView.vue`、`RegisterView.vue`：公开入口和认证页降低厚重感，增加边缘折射、高光层和更稳定的触控高度。
- `frontend/src/views/dashboard/DashboardView.vue`、`DeviceListView.vue`、`DeviceDetailView.vue`、`FileLibraryView.vue`：首屏摘要面板统一为更通透的 elevated surface，内部小卡统一为 chip 材质。
- `docs/ui.md`：补充 Liquid Glass 的使用边界、触控高度、低运动和对比度要求。

验收补充：

- 玻璃层必须服务信息层级，不能牺牲正文和表格可读性。
- 按钮、输入框和主要可点击元素保持约 44px 可触达高度。
- 继续避免大面积旧绿色、暖橙、米色渐变、宣传型大 Hero 和强动态装饰。

### P17.4 纯 CSS 液体玻璃细节补强

用户补充参考了“纯 CSS 实现 iOS 26 Liquid Glass 液体玻璃效果”的文档和截图，本轮在不牺牲可读性的前提下吸收其中的实现方式。

目标：

- 将参考文档中的半透明底、`backdrop-filter`、细高光边框、内阴影和 `::after` 液体反光转化为 FixLedger 可维护的全局材质。
- 首页、登录页、注册页保持小字号和实用入口，不回到大 Hero 或宣传页。
- 登录后的侧边栏和顶部栏同步同一套玻璃边缘，保证登录前后视觉一致。

调整范围：

- `frontend/src/styles/main.css`：降低硬白卡片感，补充 `--fl-liquid-edge`、`--fl-liquid-shine`、`--fl-liquid-filter` 等材质 token，并让通用卡片 `::after` 具备轻微 blur、brightness 和内反射。
- `frontend/src/views/home/HomeLandingView.vue`：主入口面板和今日关注面板增加液体反光，小状态条和能力条改为嵌套浅玻璃层。
- `frontend/src/views/auth/LoginView.vue`、`RegisterView.vue`：认证面板补齐边缘折射，演示账号区改为内嵌玻璃层。
- `frontend/src/layouts/MainLayout.vue`：侧边栏、顶部栏和当前家庭卡片补充同源玻璃高光。
- `docs/ui.md`、`docs/tasks.md`：记录参考文档取舍和当前边界。

验收标准：

- `/`、`/login`、`/register` 视觉应更接近参考图的透明磨砂层级，但正文、表单和按钮仍清晰可读。
- 不照搬参考文档中的黑色 `drop-shadow`、低对比白字、emoji 图标或背景图依赖。
- 继续通过类型检查、临时生产构建、冒烟测试和浏览器截图复核。

验证记录：

- 旧视觉和参考文档反模式扫描：未命中旧绿色主色、黑色 `drop-shadow(10px...)`、示例 emoji 图标等新引入问题。
- 类型检查：`npx vue-tsc --noEmit -p tsconfig.json` 通过。
- 临时生产构建：`npx vite build --outDir dist-codex --emptyOutDir` 通过；仅保留 Rollup PURE 注释和大 chunk 的既有构建提示。
- 前端冒烟：`npm run smoke` 通过。
- 浏览器复核：生产预览检查 `/`、`/login`、`/register`，桌面与窄屏下无横向滚动，控制台无错误；注册页顺手将昵称输入 `autocomplete` 调整为标准 `name`，消除 DevTools issue。
- 临时产物：`frontend/dist-codex` 已在验证后清理。

### P17.5 米家官网式公开首页返工

用户反馈：

- 当前公开首页仍显得不够像 `home.mi.com`，视觉节奏和第一屏质感需要继续调整。
- 用户不希望看到夸张大字、土味 UI 或堆叠装饰，但希望参考米家官网那种浅色、干净、产品展示明确的页面。

目标：

- 将 `/` 从 Liquid Glass 入口卡进一步调整为浅色产品官网式首屏：顶部导航、简短文案、清晰 CTA、右侧产品界面预览和少量能力露出。
- 收小 H1 和宣传文案，避免大标题压迫感；橙色只作为品牌点缀和主 CTA，不形成大面积暖橙主题。
- 产品预览只展示 FixLedger 的设备档案、待处理事项、凭证归档等真实能力，不展示商城、硬件售卖或 IoT 控制。

范围：

- `frontend/src/views/home/HomeLandingView.vue`：重做公开首页首屏布局、产品界面 mockup、能力卡片、响应式和按钮状态。
- `docs/ui.md`：补充 P17.5 公开首页设计边界，明确允许参考米家官网节奏但不复制品牌资产。
- `docs/tasks.md`：补充 P17.5 计划、范围、验收标准和验证记录。

验收标准：

- 桌面端第一屏应像克制的浅色产品页，而不是后台登录入口、营销海报或深色玻璃卡片页。
- 移动端 390px 左右宽度无横向滚动，顶部操作、CTA、产品预览和能力卡片不挤压、不重叠。
- `npx vue-tsc --noEmit -p tsconfig.json`、临时生产构建、`npm run smoke` 和 `git diff --check` 通过。
- 生产预览浏览器复核 `/` 控制台无错误。

完成记录：

- `/` 已从较重的 Liquid Glass 入口页调整为浅色产品官网式首屏：保留顶部导航、简短标题、登录/演示 CTA、右侧 FixLedger 工作界面预览和三项核心能力。
- 标题文案收敛为“设备记录，一目了然。”，桌面端计算字号约 48px，移动端 390px 宽度下不再出现不自然断行。
- 右侧 mockup 改为浅色产品面板，橙色仅用于品牌点、主 CTA 和少量状态强调。
- `docs/ui.md` 已补充 P17.5 公开首页规范，明确参考 `home.mi.com` 的结构节奏但不复制米家品牌、不做商城和 IoT 控制。

验证记录：

- 类型检查：`npx.cmd vue-tsc --noEmit -p tsconfig.json` 通过。
- 临时生产构建：`npx.cmd vite build --outDir dist-codex --emptyOutDir` 通过；仅保留 Rollup PURE 注释和大 chunk 的既有提示。
- 前端冒烟：`npm.cmd run smoke` 通过。
- 浏览器复核：生产预览 `http://127.0.0.1:4176/` 桌面 1365x768 与移动端 390x844 均无横向滚动，控制台无错误。

### P17.6 登录注册入口统一

目标：

- 将 `/login`、`/register` 从 P17.4 的蓝紫 Liquid Glass 入口同步为 P17.5 公开首页同源的浅色产品入口风格。
- 保留清晰表单、演示账号主动填入、注册后自动创建家庭空间等已有业务交互。
- 降低过度磨砂、伪元素高光和蓝紫主色割裂感，使用浅灰白底、橙色主操作、细边框和低阴影。

范围：

- `frontend/src/views/auth/LoginView.vue`：重做登录页容器、品牌、表单、演示账号区和移动端布局。
- `frontend/src/views/auth/RegisterView.vue`：重做注册页容器、表单密度、品牌入口和移动端布局。
- `docs/ui.md`、`docs/tasks.md`：记录登录/注册入口与公开首页统一的设计边界。

验收标准：

- `/login` 和 `/register` 与 `/` 在背景、品牌点、按钮、卡片阴影和字号上保持一致，不再像两个不同产品。
- 表单字段、错误提示、演示账号填入和注册提交不受影响。
- 桌面和 390px 移动端无横向滚动，控制台无错误。
- 类型检查、临时生产构建、前端冒烟和 `git diff --check` 通过。

完成记录：

- `/login` 已从蓝紫 Liquid Glass 双栏调整为浅色产品入口：左侧登录表单、右侧演示账号说明，主按钮和品牌点统一使用首页橙色。
- `/register` 已同步为同源浅色单列表单，保留用户名、邮箱、昵称、密码和确认密码字段。
- 登录/注册页的品牌入口、表单提交、演示账号填入、注册后进入总览等业务逻辑保持不变。
- 输入框高度、按钮高度和移动端表单宽度已按触控友好要求收敛。

验证记录：

- 类型检查：`npx.cmd vue-tsc --noEmit -p tsconfig.json` 通过。
- 临时生产构建：`npx.cmd vite build --outDir dist-codex --emptyOutDir` 通过；仅保留 Rollup PURE 注释和大 chunk 的既有提示。
- 前端冒烟：`npm.cmd run smoke` 通过。
- 浏览器复核：生产预览检查 `/login`、`/register`，桌面 1365x768 与移动端 390x844 均无横向滚动，控制台无错误。

### P17.7 全前端体验收口

目标：

- 将公开首页、认证页和登录后的主应用统一为同一套浅色产品工具风。
- 全局主色从系统蓝/淡紫 Liquid Glass 收敛到克制橙色主操作 + 浅灰白玻璃面板 + 语义状态色。
- 清理高频页面中的旧“设备护照”展示文案，统一称为“设备档案”。
- 保留表格、筛选、上传、看板、图表等实用工作台能力，不新增后端接口和业务模块。

范围：

- `frontend/src/styles/main.css`：统一全局颜色 token、Element Plus 主色、按钮、输入框、表格 hover 和焦点状态。
- `frontend/src/layouts/MainLayout.vue`：侧边栏、顶部栏、家庭卡片和菜单选中态同步浅色产品工具风。
- `frontend/src/views/dashboard/DashboardView.vue`：总览页图表色、健康卡、日历点、房间入口文案与全局主色对齐。
- `frontend/src/views/devices/DeviceListView.vue`、`DeviceDetailView.vue`、`FileLibraryView.vue`：清理旧蓝紫强调和旧“设备护照”文案。
- `docs/ui.md`、`docs/tasks.md`：记录 P17.7 收口边界和验证记录。

验收标准：

- `/`、`/login`、`/register`、`/dashboard`、`/devices`、`/files` 视觉语言一致，不再像多个主题拼接。
- 前端展示文案以“设备档案”为主，不把旧“设备护照”作为当前页面标题或入口名称。
- 桌面和 390px 移动端无横向滚动，控制台无错误。
- 类型检查、临时生产构建、前端冒烟和 `git diff --check` 通过。

完成记录：

- 全局主操作、选中态、表格 hover、按钮和焦点色统一为克制橙色，保留浅灰白玻璃面板和低饱和信息辅助色。
- `MainLayout.vue` 已统一侧边栏、顶部栏、家庭卡片、菜单选中态和用户入口的浅色产品工具风。
- `DashboardView.vue` 已将图表、健康卡、日历选中态和房间入口文案收敛到“设备档案”与橙色主操作。
- `DeviceListView.vue`、`DeviceDetailView.vue`、`FileLibraryView.vue` 已清理当前 UI 中的蓝紫强调和旧“设备护照”展示文案。
- `/` 首页在桌面首屏底部露出能力卡片，移动端产品预览压缩为统计与单条设备预览，避免回到满屏大海报。

验证记录：

- 类型检查：`npx.cmd vue-tsc --noEmit -p tsconfig.json` 通过。
- 临时生产构建：`npx.cmd vite build --outDir dist-codex --emptyOutDir` 通过；仅保留 Rollup PURE 注释和大 chunk 的既有提示。
- 前端冒烟：`npm.cmd run smoke` 通过。
- 浏览器复核：生产预览检查 `/`、`/login`、`/register`，桌面 1365x768 与移动端 390x844 均无横向滚动，控制台无错误；未登录访问 `/dashboard` 按预期重定向到 `/login?redirect=/dashboard`。

### P18.1 默认设备分类初始化

目标：

- 注册默认家庭空间后自动初始化常用设备分类，减少首次录入设备前的空白配置。
- 手动创建新的家庭空间时同步初始化同一套分类。
- 默认分类标记为系统默认分类，继续禁止删除，避免演示和真实使用中误删基础分类。

范围：

- `FamilyServiceImpl`：家庭空间创建事务内插入默认设备分类。
- `DeviceCategoryServiceTest`：补充注册默认家庭、手动创建家庭和系统默认分类删除保护测试。
- `docs/requirements.md`、`docs/database.md`、`docs/tasks.md`：同步默认分类初始化已进入当前实现。

验收标准：

- 新家庭空间包含：数码设备、大家电、小家电、网络设备、厨房设备、清洁设备、家居设备、其他。
- 默认分类按固定排序返回，`systemDefault=true`。
- 手动新增分类仍可继续追加，重名校验和删除保护不被破坏。
- 相关后端测试通过。

完成记录：

- 已在 `FamilyServiceImpl` 的家庭空间创建事务中批量写入 8 个默认设备分类，覆盖注册默认家庭和手动创建家庭两条路径。
- 已新增 `DeviceCategoryMapper.insertBatch` 和 `DeviceCategoryMapper.xml`，使用一条 `INSERT ... VALUES` 批量插入默认分类，避免循环单条写库。
- 默认分类统一设置 `systemDefault=true`，排序值按 10、20、30... 固定返回；既有系统默认分类删除保护继续生效。
- 已调整相关测试中的手动分类名称，避免测试自建分类和默认分类重名导致误判。
- 已同步 `docs/requirements.md`、`docs/database.md`、`docs/api.md`、`README.md` 和本任务记录。

验证记录：

- `mvn -Dtest=DeviceCategoryServiceTest test` 通过。
- `mvn -Dtest=DeviceCategoryServiceTest,DeviceAssetServiceTest,DashboardControllerTest,DashboardServiceTest test` 通过，相关分类、设备和看板测试共 17 个用例通过。
- `mvn test` 通过，后端全量 120 个测试通过。

### P18.2 当前家庭上下文自愈

目标：

- 修复前端从 `localStorage` 恢复 `currentFamilyId` 时可能拿到过期或不属于当前账号的家庭 ID 的问题。
- 登录、刷新页面和切换账号后，当前家庭必须始终落在当前用户可访问的家庭列表内。
- 如果当前账号暂时没有家庭空间，清理本地家庭上下文，避免页面继续使用脏 `familyId` 调接口。

范围：

- `frontend/src/stores/auth.ts`：收敛家庭列表加载后的当前家庭选择逻辑。
- `docs/requirements.md`、`docs/ui.md`、`docs/tasks.md`：同步家庭上下文恢复规则。

验收标准：

- `auth.loadFamilies()` 后，如果本地 `currentFamilyId` 属于当前家庭列表，则保持不变。
- 如果本地 `currentFamilyId` 不存在于当前家庭列表，则自动切到第一个可访问家庭。
- 如果家庭列表为空，则清空 `currentFamilyId` 并删除本地家庭缓存。
- 前端类型检查和 smoke 通过。

完成记录：

- 已在 `auth.ts` 新增 `resolveCurrentFamilyId`，集中判断本地家庭 ID 是否仍属于当前用户可访问家庭列表。
- `loadFamilies()` 现在会在每次刷新家庭列表后自动校正当前家庭：有效则保留，无效则切到第一个可访问家庭，列表为空则清理本地缓存。
- 登录响应没有返回 `currentFamilyId` 时会主动删除旧家庭缓存，避免切换账号后沿用上一账号的家庭 ID。
- `clearSession()` 复用 `clearCurrentFamily()`，本地令牌和家庭上下文清理路径更一致。
- `npm run smoke` 已增加对家庭上下文恢复 helper 和清理动作的静态契约检查。

验证记录：

- `npx.cmd vue-tsc --noEmit -p tsconfig.json` 通过。
- `npm.cmd run smoke` 通过。

### P18.3 新建设备默认分类体验

目标：

- 让 P18.1 初始化的默认分类真正服务首次设备录入，降低新用户新建设备时的配置负担。
- 新建设备表单在已有分类时自动选择“其他”作为兜底分类；如果没有“其他”，则使用当前家庭分类列表的第一项。
- 如果 URL 查询参数显式带入分类，则尊重外部入口，不覆盖用户上下文。

范围：

- `frontend/src/views/devices/DeviceFormView.vue`：新增设备加载分类后应用默认分类，并展示轻量提示。
- `docs/requirements.md`、`docs/ui.md`、`docs/tasks.md`：同步新增设备默认分类体验。

验收标准：

- 新建设备页加载分类后，空分类表单会自动预选“其他”或第一项分类。
- 编辑设备页不自动覆盖原有分类。
- 如果查询参数已带入 `categoryId`，不再触发兜底默认分类。
- 前端类型检查和 smoke 通过。

完成记录：

- 已在 `DeviceFormView.vue` 中新增查询参数读取 helper，支持从 URL 带入 `categoryId`、名称、购买日期、购买渠道和价格。
- 新建设备页加载家庭分类后，会优先预选“其他”；如果没有“其他”，则选择分类列表第一项。
- 默认分类只在新增页且表单分类为空时应用，编辑页不会覆盖设备原有分类。
- 默认预选后会显示轻量提示，用户手动切换或清空分类后提示消失。
- `npm run smoke` 已增加对设备表单默认分类 helper 和提示状态的静态契约检查。

验证记录：

- `npx.cmd vue-tsc --noEmit -p tsconfig.json` 通过。
- `npm.cmd run smoke` 通过。

### P18.4 新用户空数据引导

目标：

- 刚注册用户进入总览或凭证盒时，如果家庭空间还没有任何设备，不出现“空图表 + 空列表”的断层体验。
- 总览页明确提示“先创建第一台设备”，并说明设备档案会串起保修、耗材、维修和凭证。
- 凭证盒在没有设备可选择时，直接引导去创建设备，而不是只禁用上传按钮。

范围：

- `frontend/src/views/dashboard/DashboardView.vue`：在无设备时展示首个设备引导面板。
- `frontend/src/views/files/FileLibraryView.vue`：在无设备时展示凭证盒前置引导，隐藏后续凭证操作区。
- `frontend/scripts/smoke.mjs`：补充空数据引导静态契约检查。
- `docs/requirements.md`、`docs/ui.md`、`docs/tasks.md`、`README.md`：同步 P18.4 状态和体验规则。

验收标准：

- 新家庭空间无设备时，总览页能直接进入新增设备页。
- 凭证盒无设备时，不展示误导性的上传操作，只提示先创建设备档案。
- 已有设备时，原总览、凭证盒选择、上传、搜索和预览流程不受影响。
- 前端类型检查和 smoke 通过。

完成记录：

- 已在 `DashboardView.vue` 增加 `first-device-guide`，当家庭设备数为 0 时展示“添加第一台设备”和“了解凭证盒”入口。
- 已在 `FileLibraryView.vue` 增加 `credential-empty-guide`，当家庭暂无设备时隐藏凭证归档操作区，引导先创建设备档案。
- 凭证盒设备列表加载中不会误展示归档主流程，只有确认有设备后才展示凭证类型、上传、搜索和预览区域。
- `npm run smoke` 已补充总览首设备引导、凭证盒空设备引导和凭证盒流程守卫检查。

验证记录：

- `npx.cmd vue-tsc --noEmit -p tsconfig.json` 通过。
- `npm.cmd run smoke` 通过。

### P18.5 空数据加载态收口

目标：

- 收口 P18.4 新用户引导的加载态边界，避免页面首次加载前短暂显示“还没有设备”的误导状态。
- 凭证盒设备搜索无匹配时，应展示搜索无结果提示，不能把搜索结果为空误判为家庭没有任何设备。
- 无家庭上下文时不保留上一家庭的设备列表和凭证上下文，避免脏数据残留。

范围：

- `frontend/src/views/dashboard/DashboardView.vue`：总览空设备引导必须等待看板数据加载完成后再判断。
- `frontend/src/views/files/FileLibraryView.vue`：凭证盒增加设备列表加载完成标记，隐藏无设备时的上传入口，并区分搜索无匹配状态。
- `frontend/scripts/smoke.mjs`：补充加载态和搜索无结果静态契约检查。
- `docs/requirements.md`、`docs/ui.md`、`docs/tasks.md`、`README.md`：同步 P18.5 收口规则和状态。

验收标准：

- 总览页首次进入时不会在请求未完成前闪现首设备引导。
- 凭证盒首次进入时不会在设备列表请求未完成前闪现空设备引导或上传主流程。
- 凭证盒搜索关键字无匹配时展示“没有匹配设备”，用户可以清空搜索；只有未搜索且确认设备数为 0 时才展示“添加第一台设备”。
- 前端类型检查、smoke 和空白检查通过。

完成记录：

- 已在 `DashboardView.vue` 增加 `dataLoaded` 加载完成标记，首页统计未返回前不再展示首设备引导。
- 已在 `FileLibraryView.vue` 增加 `devicesLoaded`、`hasNoDeviceMatches` 和 `clearDeviceSearch`，凭证盒能区分未加载、家庭无设备和搜索无匹配。
- 凭证盒无设备时隐藏顶部上传入口和设备搜索工具栏；搜索无匹配时保留搜索工具栏并提供清空搜索动作。
- 家庭上下文清空或切换时会清空上一家庭的设备列表、选中设备和凭证盒上下文。
- `npm run smoke` 已补充总览加载守卫、凭证盒设备加载守卫、搜索空状态和清空搜索动作检查。

验证记录：

- `npx.cmd vue-tsc --noEmit -p tsconfig.json` 通过。
- `npm.cmd run smoke` 通过。
- `git diff --check` 通过；仅输出 Git 的 LF/CRLF 换行提示，无空白错误。


## 27. P19-P23 一次性完善计划

本轮目标是在不引入真实外部密钥和不可验证依赖的前提下，把 P19-P23 做成可运行、可测试、可演示的项目内闭环。真实邮件服务器、真实 Webhook 投递、生产域名和云端发布仍属于部署环境配置，不在代码中硬编码。

### P19 演示级体验复核

目标：

- 强化前端 smoke，覆盖公开首页、登录注册、总览、设备、凭证、家庭设置和辅助工具等演示入口。
- 演示文档和 README 说明当前 Docker 路径、演示账号、核心流程和增强边界。

验收标准：

- `npm run smoke` 能检查核心页面、关键空状态、家庭协作入口和票据草稿跳转契约。
- README 和演示文档能解释 P19-P23 当前完成状态。

### P20 家庭成员协作闭环

目标：

- 家庭所有者可以按账号或邮箱把已注册用户加入家庭。
- 家庭所有者可以把成员角色调整为 `OWNER` 或 `MEMBER`，但不能让家庭空间失去最后一个所有者。
- 家庭所有者可以移除成员，但不能移除自己或最后一个所有者。
- 家庭设置页提供邀请、角色调整和移除入口。

验收标准：

- 非家庭所有者不能邀请、调整角色或移除成员。
- 重复邀请同一用户返回业务错误。
- 被邀请用户刷新家庭列表后能看到新家庭空间。
- 后端 Service/Controller 测试覆盖成功和权限失败路径。

### P21 凭证智能化增强

目标：

- AI 票据解析结果进入新增设备表单时，带入设备名称、购买日期、购买价格、购买渠道和建议分类。
- 新增设备表单优先尊重 URL 中的 `categoryId`；如果只传入 `categoryName`，则按家庭分类名称匹配。
- AI 结果继续只作为草稿，不自动保存设备。

验收标准：

- 从辅助工具进入新增设备页时，建议分类能匹配 P18 默认分类。
- 编辑设备页不应用 AI 草稿参数。
- 前端类型检查和 smoke 通过。

### P22 通知与操作日志增强

目标：

- 增加 `sys_operation_log` 表、实体、Mapper、Service 和查询接口。
- 家庭成员邀请、角色调整、移除等关键协作操作写入操作日志。
- 提醒站内通知写入收敛到通知服务，为邮件/Webhook 后续扩展保留稳定边界。

验收标准：

- 家庭所有者执行协作操作后，可以通过 `/api/system/operation-logs` 查询到日志。
- 操作日志查询只返回当前登录用户所属家庭的日志；非成员不能越权读取。
- 通知服务不在事务中调用外部邮件/Webhook，本轮只落站内通知和扩展点。

### P23 生产化与 CI/CD

目标：

- GitHub Actions 支持手动触发，前端类型检查、构建、smoke 和安全审计分步执行。
- 增加生产准备检查脚本，覆盖 Docker Compose、环境变量模板、前端构建脚本和后端 Maven 配置。
- 文档明确部署前检查、回滚思路和仍需由部署平台配置的生产项。

验收标准：

- CI 配置不写入任何真实密钥。
- 生产检查脚本可本地执行并输出明确通过/失败项。
- README 能说明 CI、Docker 和生产准备检查的使用方式。

完成记录：

- P19 已扩展 `frontend/scripts/smoke.mjs`，静态检查覆盖公开首页、登录注册、看板、设备、凭证盒、家庭设置、AI 草稿跳转和系统日志入口。
- P20 已完成家庭成员协作闭环：后端支持邀请已注册用户、调整成员角色、移除成员和最后所有者保护；前端家庭设置页支持邀请弹窗、角色调整、移除确认和只读态提示。
- P21 已完成票据草稿分类预填：AI 解析结果跳转新增设备时会带入 `categoryName`，设备表单在分类加载后按名称匹配默认分类，匹配不到时继续使用 P18 的兜底分类策略。
- P22 已落地 `sys_operation_log`、`modules.system`、操作日志分页接口和 `NotificationService` 抽象；家庭邀请、角色调整、移除会记录操作日志，站内通知写入收敛到通知服务。
- P23 已增强 CI：支持 `workflow_dispatch`，前端类型检查、`dist-ci` 构建、smoke、安全审计分步执行；新增 `scripts/check-production-readiness.ps1` 用于本地和 CI 的生产准备检查。

验证记录：

- 后端全量测试通过：`mvn test`，共 126 个测试，0 失败、0 错误、0 跳过。
- 前端类型检查通过：`npx.cmd vue-tsc --noEmit -p tsconfig.json`。
- 前端 smoke 通过：`npm.cmd run smoke`。
- 前端 CI 构建通过：`npx.cmd vite build --outDir dist-ci --emptyOutDir`。
- 生产准备检查通过：`./scripts/check-production-readiness.ps1 -Strict`。
- 前端依赖审计以 critical 作为 CI 阈值通过；当前 `axios/form-data` high 和 `echarts` moderate 上游暂无直接修复，本轮记录为依赖升级窗口处理，不阻塞 P23 门禁。

## 28. P24 家庭数据导出计划

本轮目标是补齐一个高实用性的家庭数据出口：用户可以把家庭设备资产清单和维修费用报表导出为 CSV，便于做家庭盘点、维修复盘或离线归档。该能力不引入新依赖、不调用外部服务，也不把项目扩展成企业资产系统。

### P24.1 导出后端接口

目标：

- 增加家庭维度 CSV 导出接口，覆盖设备资产清单和维修费用报表。
- 所有导出先校验当前用户是否属于该家庭空间。
- 维修费用报表沿用当前费用统计口径：排除已取消记录，只导出有实际维修费用的记录。

范围：

- 新增导出 Controller / Service。
- 复用设备、分类、维修 Mapper，批量补齐分类名称和设备名称，避免 N+1。
- CSV 单元格统一转义，并防止 `= + - @` 等公式注入前缀被表格软件执行。

验收标准：

- `GET /api/families/{familyId}/exports/devices.csv` 返回 UTF-8 CSV 下载。
- `GET /api/families/{familyId}/exports/maintenance-costs.csv` 支持 `startDate`、`endDate` 日期筛选。
- 非家庭成员访问导出接口返回无权限。
- 后端测试覆盖成功导出、费用口径和越权访问。

### P24.2 前端下载入口

目标：

- 设备档案页提供“导出设备清单”入口。
- 维修记录页提供“导出费用报表”入口。
- 下载逻辑集中在 API 层，页面只负责编排当前家庭和筛选条件。

范围：

- `frontend/src/api/device.ts`、`frontend/src/api/maintenance.ts`：新增 Blob 下载方法。
- `DeviceListView.vue`、`MaintenanceView.vue`：增加导出按钮和加载态。
- `frontend/scripts/smoke.mjs`：增加导出入口静态契约检查。

验收标准：

- 前端类型检查通过。
- smoke 能检查导出 API 和页面入口。
- 导出按钮不会在没有家庭上下文时发起请求。

完成记录：

- 已新增 `modules.exporter`，包含 `FamilyExportController`、`FamilyExportService`
  和 CSV 文件结果对象。
- 设备资产清单导出会先校验家庭成员权限，再分页读取最多 5000 条设备数据，并批量补齐分类名称，避免按设备循环查询分类。
- 维修费用报表导出会按完成日期筛选，排除已取消记录，只导出有实际维修费用的记录，并批量补齐设备名称。
- CSV 输出带 UTF-8 BOM，单元格统一转义，并对 `= + - @` 开头的内容增加前缀保护，降低表格软件公式注入风险。
- 前端已在设备档案页增加“导出清单”，在维修记录页增加“导出费用报表”，维修导出复用日期筛选条件。
- `frontend/scripts/smoke.mjs` 已补充导出 API 与页面入口静态契约检查。

验证记录：

- P24 后端导出测试通过：`mvn -Dtest=FamilyExportServiceTest,FamilyExportControllerTest test`，共 4 个测试，0 失败、0 错误、0 跳过。
- 后端全量测试通过：`mvn test`，共 130 个测试，0 失败、0 错误、0 跳过。
- 前端类型检查通过：`npx.cmd vue-tsc --noEmit -p tsconfig.json`。
- 前端 smoke 通过：`npm.cmd run smoke`。
- 前端 CI 构建通过：`npx.cmd vite build --outDir dist-ci --emptyOutDir`。
- 空白检查通过：`git diff --check`；仅输出 Git 的 LF/CRLF 换行提示，无空白错误。

## 29. P25-P30 后续完善计划

后续开发继续围绕家庭设备生命周期，不扩展成企业资产后台，也不把 AI 做成强依赖。优先级从“日常使用价值高、可本地验证、风险可控”的能力开始。

### P25 设备二维码标签

目标：

- 让用户可以给设备生成二维码标签，贴在设备或保修卡上。
- 扫码后进入设备详情；如果未登录，则进入受限的只读设备卡片或提示登录。

验收标准：

- 设备详情页可以预览二维码标签。
- 支持下载 PNG 或打印标签。
- 二维码访问仍经过权限边界设计，不直接暴露家庭私密数据。

### P26 OCR 与智能归档

目标：

- 增加 OCR 抽象，先提供 Mock 或可关闭实现。
- 发票图片、保修卡和说明书 PDF 识别后，只生成可确认草稿，不自动覆盖用户数据。

验收标准：

- 上传凭证后可以触发识别草稿。
- 用户确认后才写入设备、保修或附件备注。
- OCR/AI 失败不影响文件上传和设备核心流程。

### P27 移动端与 PWA

目标：

- 优化手机端查看设备、凭证和维修记录的体验。
- 补 PWA 安装入口和基础离线壳，提升家庭成员现场使用体验。

验收标准：

- 手机宽度下登录、我的家、设备详情、凭证盒和维修记录不出现明显遮挡或横向溢出。
- 常用操作触控区域足够清晰。
- PWA 配置不缓存敏感接口响应。

### P28 外部通知渠道

目标：

- 在现有 `NotificationService` 边界后接入邮件/Webhook。
- 记录投递失败原因，允许后台或定时任务重试。

验收标准：

- 外部通知配置通过 `@ConfigurationProperties` 管理，不硬编码密钥或地址。
- 外部渠道失败不回滚提醒任务和站内通知。
- 测试默认使用 Mock，不依赖真实邮箱或 Webhook 服务。

### P29 性能与可观测性

目标：

- 对首页统计、设备列表、凭证盒聚合和导出接口做慢查询复核。
- 补关键接口日志指标和生产排障说明。
- 如果导出数据量超过同步 CSV 适用范围，再落 `fl_export_record` 异步导出。

验收标准：

- 常用查询有家庭维度、设备维度或状态日期索引支撑。
- 首页热点数据可以短期缓存，写操作后有刷新策略。
- 导出异步化有明确阈值、状态流转和失败重试设计。

### P30 生产发布收口

目标：

- 把生产部署需要的环境变量、域名、HTTPS、备份恢复、回滚和监控说明补完整。
- 保持 `.env.example` 只放示例值，不提交真实密钥。

验收标准：

- README 能给出本地、Docker 和生产三种启动/部署路径。
- 发布前检查脚本覆盖关键配置。
- 有可执行的备份恢复和回滚清单。

## 30. P27.1 移动端主导航修复

用户实际检查 `390px` 页面后发现，当前主布局只是把桌面侧栏改为纵向长列表，导致移动端首屏被导航占满，业务内容被推到下方。本批次先修复登录后主框架，不提前实现 P27.2 PWA。

目标：

- 手机端首屏直接呈现当前页面内容，不再展示整屏功能长条。
- 使用顶部轻量工具栏和底部 5 项主导航，形成符合移动应用习惯的结构。
- 桌面侧栏和现有路由保持不变。

范围：

- `frontend/src/layouts/MainLayout.vue`：增加移动底部标签栏，隐藏移动端桌面侧栏，家庭设置移入用户菜单。
- `frontend/src/views/dashboard/DashboardView.vue`：移动端摘要与指标改为紧凑两列信息布局。
- `frontend/scripts/smoke.mjs`：增加移动导航静态契约检查。
- `docs/ui.md`、`docs/tasks.md`：记录移动信息架构和验证结果。

验收标准：

- `390x844` 和 `320x568` 下首屏不再出现纵向完整菜单，页面无横向滚动。
- 总览指标不再全部以整行长卡片展示，核心状态在手机端使用两列网格。
- 底部导航固定展示首页、设备、日历、凭证、我的，触控区域不小于 44px。
- 页面内容不会被底部导航或安全区遮挡。
- 桌面 `1440px` 仍保留左侧导航，既有登录、家庭切换和退出登录行为不变。
- 前端类型检查、构建、smoke 和真实浏览器检查通过。

完成记录：

- `MainLayout.vue` 在 `840px` 以下完全隐藏桌面侧栏，固定底部导航最终收敛为首页、设备、日历、凭证、我的。
- 移动顶部栏只保留页面标题、家庭切换和用户入口；家庭设置已加入用户下拉菜单。
- 底部导航已增加安全区偏移和内容区底部留白，选中态同时使用背景材质、颜色和字重表达。
- `DashboardView.vue` 的移动摘要已压缩，健康指数改为横向信息块，首页只保留 4 个两列核心指标。
- `frontend/scripts/smoke.mjs` 已增加移动导航、当前页状态和安全区静态契约检查。

移动信息架构二次调整：

- 新增独立 `/calendar` 路由，旧 `/dashboard?focus=calendar` 继续兼容但不再作为移动端主入口。
- 移动底栏调整为“首页、设备、日历、凭证、我的”，AI 辅助退出一级导航。
- 移动首页隐藏周任务、房间、日历、图表和最近记录，只保留家庭状态摘要与四个核心指标。
- 移动日历页只渲染日历和当天提醒，避免与首页展示相同内容。
- 隐藏图表增加尺寸守卫，移动端不再初始化 `0x0` 的 ECharts 容器，切回桌面后可正常延迟初始化。

移动页面对齐治理：

- 增加浅色 `color-scheme` 元信息与全局声明，阻止移动浏览器强制暗色化当前浅色设计。
- 家庭成员、操作日志、维修、耗材、保修和提醒在 `720px` 以下由桌面表格切换为移动信息卡。
- 移动筛选表单、分页、弹窗和操作按钮使用统一响应式规则，消除固定宽度与按钮重叠。
- 移动筛选项改为纵向排列，表单内容和 Element Plus 控件允许收缩，带单位后缀的数字框不再撑出卡片。
- 保修、耗材和维修二级页保持“设备”底栏选中，提醒中心保持“日历”选中；顶部显示真实业务标题，不再错误回退到“首页”。
- 浏览器逐页扫描 `/dashboard`、`/devices`、`/calendar`、`/files`、`/settings/family`、`/maintenance`、`/consumables`、`/warranties`、`/reminders`、`/ai-tools` 的元素边界。

验证记录：

- 前端类型检查已随构建前置检查通过；生产构建使用 CI 同源命令
  `npx.cmd vite build --outDir dist-ci --emptyOutDir`，退出码为 0。
- 前端 smoke 通过：`npm.cmd run smoke`。
- 浏览器 `390x844` 首页：`scrollWidth=390`，日历和详细区块隐藏，只显示 4 个核心指标，底栏文案为首页、设备、日历、凭证、我的，控制台无错误。
- 浏览器 `390x844` 日历：独立地址为 `/calendar`，首页摘要和指标节点不存在，只显示月份日历与当天提醒，“日历”标签选中，控制台无错误。
- 浏览器 `320x568`：页面 `scrollWidth=320`，5 个导航触控区域均为 `56x54px`，控制台无错误。
- 浏览器 `390x844`：上述 10 个登录后页面均满足 `scrollWidth=390` 且可见元素越界数为 0；成员、维修、耗材、保修和提醒的桌面表格隐藏，移动卡片显示。
- 浏览器 `320x568`：上述 10 个页面均满足 `scrollWidth=320` 且可见元素越界数为 0。
- 浏览器模拟系统深色偏好时，`prefers-color-scheme: dark` 生效，但页面计算样式仍为 `color-scheme: light only`，背景和正文保持浅色设计。
- 浏览器视觉抽查“我的”和“维修记录”：家庭成员、统计、筛选器、按钮和底栏无重叠；维修页顶部显示“维修记录”，底栏“设备”处于选中态。
- 浏览器 `1440x1000`：桌面侧栏显示且宽度为 260px，移动底栏隐藏，核心指标保持四列，控制台无错误。
- `git diff --check` 通过；仅有 LF/CRLF 换行提示，无空白错误。

## 31. P27.2 PWA 安装与离线外壳

目标：

- 让现有响应式 Web 应用可以安装到手机或桌面，并以独立窗口启动。
- 在完全离线时提供稳定状态页，不缓存家庭业务数据或鉴权响应。
- 为 Service Worker 更新提供用户可控的刷新入口。

范围：

- `frontend/public/manifest.webmanifest`、PWA 图标和 `offline.html`。
- `frontend/public/service-worker.js`：安全缓存白名单、导航离线回退和更新消息。
- `frontend/src/pwa.ts`：安装事件、网络状态、Service Worker 注册与更新状态。
- `frontend/src/layouts/MainLayout.vue`：用户菜单安装/更新入口与离线状态提示。
- `frontend/index.html`、`frontend/scripts/smoke.mjs`：清单引用、移动元信息与 PWA 静态契约。

验收标准：

- Manifest 包含名称、图标、`standalone` 显示模式、`/dashboard` 启动地址和浅色主题。
- Service Worker 只预缓存公开离线资源，明确排除 API、附件和鉴权响应。
- 支持安装事件时用户菜单显示安装命令；已安装运行时隐藏。
- 离线导航返回本地离线页，恢复网络后可重试；不展示缓存业务数据。
- 新版本等待激活时用户可以主动更新，不强制刷新当前表单。
- 前端类型检查、smoke、生产构建与真实浏览器 PWA 检查通过。

完成记录：

- 新增 `manifest.webmanifest`、192/512 PNG 图标、独立离线页和版本化 Service Worker。
- Service Worker 安装缓存仅包含离线页、Manifest、favicon 和两张应用图标；API、Actuator、Swagger、跨域、非 GET 和带 `Authorization` 的请求全部保持网络访问。
- 新增 `src/pwa.ts`，统一管理安装事件、独立窗口状态、在线状态、Service Worker 注册和等待更新。
- 登录后用户菜单按能力显示“安装到设备”和“更新应用”，离线时主布局显示轻量状态条，不增加底部导航项。
- `index.html` 已补 Manifest、主题色、Android/iOS 独立应用元信息和 Apple Touch Icon。

验证记录：

- TDD RED：扩展 smoke 后因缺少 `public/manifest.webmanifest` 失败；实现静态层后继续因缺少“安装到设备”命令失败。
- TDD GREEN：`npm.cmd run smoke` 与 `npx.cmd vue-tsc --noEmit -p tsconfig.json` 均通过。
- `npx.cmd vite build --outDir dist-ci --emptyOutDir` 通过；保留 P29 待治理的既有大分块警告。
- 生产预览中 Service Worker 状态为 `activated` 且已控制页面，缓存名为 `fixledger-shell-v1`。
- Cache Storage 仅有 `/offline.html`、Manifest、favicon 和 192/512 图标；带鉴权头的 `/api` 探测请求未进入缓存。
- 浏览器断网访问 `/dashboard` 返回“当前网络不可用”离线页，`390x844` 下 `scrollWidth=390`，页面不包含设备或统计业务节点。
- 图标像素检查为 `192x192` 和 `512x512`，生产预览控制台无错误或警告。
- PWA 安装依赖 HTTPS 或同设备 `localhost`；手机访问普通局域网 HTTP 地址时只提供响应式 Web，正式手机安装由 P30 HTTPS 部署补齐。

## 32. P28 外部通知投递与重试

目标：

- 在现有站内通知基础上增加默认关闭的邮件和 Webhook 投递。
- 使用数据库 Outbox 解耦提醒事务与外部网络调用。
- 投递失败记录可控重试信息，单条失败不影响其他通知。

实现范围：

- 扩展 `fl_notification_record` 的收件目标、状态、尝试次数和重试时间字段。
- `NotificationService` 在提醒事务内写入站内通知与外部待投递记录。
- 增加原子领取、邮件/Webhook Sender、失败重试服务和独立调度器。
- 增加 `NotificationProperties`、SMTP 标准配置和 Docker/.env 示例。
- 测试默认禁用真实渠道，Sender 使用 Mock 或本地桩验证。

验收标准：

- 外部渠道关闭时只生成现有站内通知，行为保持兼容。
- 邮件启用且成员存在邮箱时写入 `EMAIL/PENDING`，Webhook 启用时每条提醒写入一条 `WEBHOOK/PENDING`。
- 调度器在事务外调用发送器，成功标记 `SENT`，失败标记 `FAILED` 并计算下一次重试。
- 多个任务中单条投递失败不阻塞后续记录；达到最大次数后不再自动领取。
- 配置、日志、数据库和测试中不出现真实密码、Webhook 密钥或完整外部响应正文。

完成记录：

- 提醒事务只写入 `IN_APP/SENT` 和外部渠道 `PENDING` 记录，不调用 SMTP 或 HTTP。
- 邮件按家庭成员邮箱批量入队，避免循环查询用户；Webhook 每条提醒只创建一条记录。
- 独立调度器按启用 Sender 过滤待处理渠道，防止已关闭渠道的历史记录占满批次。
- 条件更新原子领取通知，失败按指数退避重试，达到最大尝试次数后停止自动领取。
- `PROCESSING` 超过配置时限后自动恢复；达到上限的记录转为终态 `FAILED`。
- Webhook 默认要求 HTTPS，可选 HMAC-SHA256 签名；SMTP 和 Webhook 配置均来自环境变量。
- 测试环境显式关闭外部渠道，所有 Sender 测试使用 Mock 或本地桩，不发送真实请求。

验证记录：

- Outbox 入队、调度开关、竞争领取、单条失败隔离、退避和最大次数测试通过。
- 邮件地址、Webhook HTTPS/主机名、正数超时配置的启动期校验测试通过。
- H2 集成测试验证超时 `PROCESSING` 恢复、终止和未超时记录保持不变。
- 后端全量 151 个测试、前端 smoke/类型检查/生产构建、Docker Compose 配置解析和仓库静态检查通过。

## 33. P29 性能与可观测性

目标：

- 把首页 Redis 刷新标记升级为可降级的短 TTL 结果缓存。
- 把首页摘要从固定 7 次查询收敛为单次数据库聚合查询。
- 为缓存、回源、HTTP 请求和导出提供低基数 Micrometer 指标。
- 明确同步导出容量边界和未来异步化触发条件。

实现范围：

- `DashboardProperties`、首页摘要缓存服务和事务提交后失效接口。
- `DashboardStatisticsMapper` 与 MyBatis XML 聚合查询。
- 设备、保修、耗材和维修写服务接入缓存失效。
- Prometheus Registry、安全的 Actuator 暴露配置和运维排障文档。
- `ExportProperties`、5000 行同步上限、超限拒绝与导出指标。
- 设备默认列表索引和首页/凭证盒/导出查询索引复核。

验收标准：

- 缓存命中时不访问首页统计 Mapper；Redis 异常和非法缓存值可回源。
- 家庭权限校验不会因缓存命中被绕过，业务写事务提交后缓存失效。
- 首页回源只执行一次聚合 Mapper 调用，统计口径与现有接口一致。
- 同步导出超过阈值时不返回截断数据，当前阶段不提前创建异步导出表。
- 指标标签不包含用户、家庭、设备 ID 或异常正文。
- 后端测试、前端门禁、Compose 配置和静态检查通过。

完成记录：

- 首页摘要改为默认 2 分钟 Cache-Aside 缓存，家庭权限校验先于缓存读取，设备、保修、耗材和维修写事务提交后失效对应家庭缓存。
- 首页数据库回源从 7 次独立查询收敛为一次 MyBatis XML 聚合查询；非法缓存值会删除并回源，Redis 异常不阻塞首页。
- 增加缓存命中、首页回源、HTTP 请求和同步导出指标，Prometheus Registry 已安装，但 Actuator 默认仍只公开 `health,info`。
- 同步导出默认上限为 5000 行，通过多取一行识别超限并返回明确错误；异步导出只在真实超限或 P95 超过 3 秒后启动设计。
- 新建库增加设备默认列表索引，已有 Docker 数据卷的非破坏性升级步骤记录在 `docs/operations.md`。
- Element Plus 改为选择性安装，ECharts 改为模块化注册，Vite 按依赖边界拆包，生产构建不再出现超过 500 KiB 的分块警告。

验证记录：

- 后端全量 `mvn test` 通过，共 163 个测试，0 失败、0 错误、0 跳过。
- `DashboardPropertiesTest`、`ExportPropertiesTest` 覆盖缓存 TTL 和同步导出上限的启动期配置边界。
- `DashboardStatisticsMapperTest`、`DashboardCacheInvalidationTest`、`DashboardSummaryCacheServiceTest` 和 `FamilyExportLimitTest` 覆盖聚合口径、事务后失效、缓存降级与导出超限。
- `npm.cmd run smoke`、`npx.cmd vue-tsc --noEmit -p tsconfig.json` 和 `npx.cmd vite build --outDir dist-ci --emptyOutDir` 通过。
- 前端入口与首页基线分块由 `1074.33 KiB`、`1025.64 KiB` 降为入口业务块 `442.80 KiB`、首页页面块加图表 vendor `484.67 KiB`。
- `docker compose config --quiet` 与 `git diff --check` 通过。
