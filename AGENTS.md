# FixLedger 编码规范

Spring Boot 3.x + JDK 21 + MyBatis Plus + MySQL + Redis + Vue3 + Element Plus 家庭设备保修与耗材管理系统。写代码、改文档、设计接口和拆任务时必须遵守以下规则。

---

## 一、项目定位

FixLedger 是一个面向家庭场景的设备保修、维修、耗材更换和凭证归档工具，用来解决家电、数码设备、网络设备等在日常使用中出现的保修期难追踪、发票说明书分散、维修记录缺失、耗材更换容易遗忘等问题。

项目定位必须保持清晰：

- 主场景：家庭设备保修、维修、耗材更换管理。
- 核心目标：设备档案可管理、保修凭证可归档、耗材更换可提醒、维修过程可追踪。
- AI 定位：辅助录入、辅助分析、辅助总结，不能作为核心业务依赖。
- 不做方向：不做商城、不做企业固定资产盘点系统、不做泛后台管理模板、不做以 AI 为主题的产品。

任何新增功能都必须服务于家庭设备生命周期管理，不允许随意扩展成宽泛的“资产管理平台”。

---

## 二、项目结构

前后端分离项目，后端使用 Maven 多目录单应用结构，前端放在 `frontend/` 目录，文档放在 `docs/` 目录。

```text
fix-ledger/
├── backend/                           # Spring Boot 后端应用
│   ├── src/main/java/com/fixledger/
│   │   ├── FixLedgerApplication.java  # @SpringBootApplication + @EnableScheduling
│   │   │
│   │   ├── common/                    # 通用基础能力
│   │   │   ├── annotation/            # 通用注解，如操作日志、限流等
│   │   │   ├── config/                # Web、Redis、OpenAPI、Security、ObjectMapper 配置
│   │   │   ├── constant/              # 通用常量、Redis Key、业务字典常量
│   │   │   ├── exception/             # ErrorCode、BusinessException、GlobalExceptionHandler
│   │   │   ├── result/                # Result<T> 统一响应结构
│   │   │   ├── security/              # JWT、用户上下文、认证过滤器
│   │   │   ├── validation/            # 自定义校验器
│   │   │   └── utils/                 # 通用工具类
│   │   │
│   │   ├── infrastructure/            # 技术基础设施
│   │   │   ├── ai/                    # AI Client、Prompt、Mock AI 服务
│   │   │   ├── file/                  # 本地文件 / MinIO 存储、文件校验、附件访问
│   │   │   ├── mapper/                # MapStruct 映射器
│   │   │   ├── notification/          # 站内通知、邮件、Webhook 扩展
│   │   │   ├── redis/                 # RedisService、缓存 Key 管理
│   │   │   └── scheduler/             # 定时任务封装
│   │   │
│   │   └── modules/                   # 业务模块，每个模块自包含 MVC 分层
│   │       ├── auth/                  # 注册、登录、刷新 Token、退出登录
│   │       ├── user/                  # 用户资料、角色、权限
│   │       ├── family/                # 家庭空间、家庭成员
│   │       ├── asset/                 # 设备档案、设备分类、设备状态
│   │       ├── warranty/              # 保修记录、保修到期提醒
│   │       ├── consumable/            # 耗材项、更换周期、更换记录
│   │       ├── maintenance/           # 故障记录、维修过程、维修费用
│   │       ├── reminder/              # 提醒任务、通知记录、提醒去重
│   │       ├── dashboard/             # 首页统计、费用统计、提醒日历
│   │       ├── ai/                    # 票据信息提取、故障建议、维修总结
│   │       └── system/                # 操作日志、系统配置、字典数据
│   │
│   └── src/main/resources/
│       ├── application.yml            # 应用配置
│       ├── application-dev.yml        # 开发环境配置
│       ├── application-test.yml       # 测试环境配置
│       ├── mapper/                    # MyBatis XML
│       └── prompts/                   # AI Prompt 模板
│
├── frontend/                          # Vue3 前端应用
│   ├── src/
│   │   ├── api/                       # API 请求封装
│   │   ├── assets/                    # 静态资源
│   │   ├── components/                # 公共组件
│   │   ├── layouts/                   # 页面布局
│   │   ├── router/                    # Vue Router
│   │   ├── stores/                    # Pinia 状态管理
│   │   ├── styles/                    # 全局样式
│   │   ├── types/                     # TypeScript 类型
│   │   ├── utils/                     # 工具函数
│   │   └── views/                     # 页面组件
│   ├── package.json
│   └── vite.config.ts
│
├── docs/                              # 项目文档
│   ├── requirements.md                # 需求说明
│   ├── architecture.md                # 架构设计
│   ├── api.md                         # 接口设计
│   ├── database.md                    # 数据库设计
│   ├── ui.md                          # 页面与交互设计
│   └── tasks.md                       # 开发任务拆分
│
├── docker-compose.yml                 # Docker 编排
├── .env.example                       # 环境变量示例
├── AGENTS.md                          # 本规范
└── README.md
```

**技术栈**：JDK 21 / Spring Boot 3.x / Spring Security / JWT / MyBatis Plus / MySQL 8 / Redis 7 / Spring Scheduler / MapStruct / SpringDoc OpenAPI 或 Knife4j / MinIO 或本地文件存储 / Spring AI 或自定义 AI Client / Maven

**前端**：Vue 3 + TypeScript + Vite + Element Plus + Pinia + Vue Router + Axios + ECharts

---

## 三、分层架构

```text
Controller → Service → Mapper
                ↕
        Infrastructure（RedisService、FileStorageService、AiClient、NotificationService）
```

### Controller 层

- 只负责路由、参数校验、权限入口和调用 Service，禁止写业务逻辑。
- RESTful 风格优先：`/api/{module}`、`/api/{module}/{id}`、`/api/{module}/{id}/{action}`。
- 请求体必须使用 `@Valid` + `@RequestBody` 校验。
- 分页查询统一使用 `PageRequest` / `PageQuery` 类，不允许散落 `pageNum`、`pageSize` 参数。
- 返回值统一为 `Result<T>`，禁止直接返回裸对象或 `Map`。

### Service 层

- 负责业务编排、状态流转、事务边界和跨模块调用。
- 大 Service 必须拆分，例如 `DeviceAssetService`、`WarrantyReminderService`、`ConsumableScheduleService`、`MaintenanceFlowService`。
- 所有业务异常使用 `BusinessException(ErrorCode.XXX, message)`。
- AI 调用、文件上传、通知发送等外部操作必须与核心事务解耦。
- 涉及提醒、通知、AI 分析的任务优先异步或定时处理，不阻塞主流程。

### Mapper 层

- 使用 MyBatis Plus `BaseMapper<XxxEntity>`。
- 简单 CRUD 优先使用 MyBatis Plus 提供的方法。
- 复杂 SQL 写在 XML 中，禁止在 Service 中拼接 SQL。
- 列表查询必须考虑分页、索引和数据隔离条件。

### Infrastructure 层

- 封装技术细节，对业务层提供稳定接口。
- `RedisService` 统一管理 Redis 读写，不允许业务代码直接散落 `StringRedisTemplate` 操作。
- `FileStorageService` 屏蔽本地存储和 MinIO 差异。
- `AiClient` 屏蔽不同大模型 Provider 差异，并提供 Mock 实现。
- `NotificationService` 屏蔽站内通知、邮件、Webhook 等渠道差异。

---

## 四、JavaBean 后缀规则

| 后缀 | 用途 | 示例 |
| --- | --- | --- |
| `XxxEntity` | 数据库持久化对象 | `DeviceAssetEntity`、`WarrantyRecordEntity` |
| `XxxDTO` | 跨层数据传输 | `DeviceDetailDTO`、`DashboardSummaryDTO` |
| `XxxRequest` | 前端请求体 | `CreateDeviceRequest`、`CreateMaintenanceRequest` |
| `XxxResponse` | 前端响应体 | `DeviceListResponse`、`ReminderCalendarResponse` |
| `XxxQuery` | 查询条件 | `DevicePageQuery`、`MaintenancePageQuery` |
| `XxxMapper` | MyBatis Mapper | `DeviceAssetMapper` |
| `XxxConverter` | MapStruct 转换器 | `DeviceAssetConverter` |
| `XxxProperties` | 配置属性 | `JwtProperties`、`AiProperties` |

规则：

- 不可变请求对象优先使用 `record`，如 `CreateDeviceRequest`、`LoginRequest`。
- Entity 可以使用 Lombok，但必须避免暴露给前端。
- Entity 和 DTO / Response 的映射优先使用 MapStruct。
- 简单字段复制可以使用 `BeanUtils.copyProperties`，但复杂映射必须显式处理。
- **禁止直接返回 Entity 给前端**。

---

## 五、异常与错误码

### ErrorCode 分域规则

| 域 | 范围 | 示例 |
| --- | --- | --- |
| 通用 | 1xxx | BAD_REQUEST(1001)、NOT_FOUND(1004) |
| 认证与用户 | 2xxx | USER_NOT_FOUND(2001)、PASSWORD_ERROR(2002) |
| 家庭空间 | 3xxx | FAMILY_SPACE_NOT_FOUND(3001) |
| 设备档案 | 4xxx | DEVICE_NOT_FOUND(4001)、DEVICE_STATUS_INVALID(4002) |
| 保修 | 5xxx | WARRANTY_NOT_FOUND(5001) |
| 耗材 | 6xxx | CONSUMABLE_NOT_FOUND(6001)、REPLACE_DATE_INVALID(6002) |
| 维修 | 7xxx | MAINTENANCE_NOT_FOUND(7001)、MAINTENANCE_STATUS_INVALID(7002) |
| 提醒通知 | 8xxx | REMINDER_NOT_FOUND(8001)、NOTIFICATION_SEND_FAILED(8002) |
| 文件存储 | 9xxx | FILE_UPLOAD_FAILED(9001)、FILE_TYPE_NOT_ALLOWED(9002) |
| AI 服务 | 10xxx | AI_SERVICE_UNAVAILABLE(10001)、AI_PARSE_FAILED(10002) |
| 系统配置 | 11xxx | CONFIG_NOT_FOUND(11001) |

### 异常处理规则

- 抛出：`throw new BusinessException(ErrorCode.XXX, "描述信息")`。
- **禁止** `throw new RuntimeException(...)`。
- 全局异常处理器 `GlobalExceptionHandler` 统一返回 `Result.error(code, message)`。
- `catch (BusinessException e) { throw e; }` 保留业务异常原样抛出。
- 禁止吞异常，禁止 `catch (Exception e) {}` 空处理。
- 第三方异常必须转换为业务异常或基础设施异常，并保留日志。

---

## 六、核心业务规则

### 设备档案

- 设备必须归属于一个家庭空间。
- 设备名称、分类、购买日期、状态为核心字段。
- 设备状态只能通过业务方法流转，禁止直接更新状态字段。
- 设备删除优先逻辑删除，避免丢失维修和保修历史。

### 保修管理

- 保修结束日期不得早于购买日期或保修开始日期。
- 即将过保提醒必须可配置提前天数，如 7 天、15 天、30 天。
- 同一设备同一天同一保修事项不得重复生成提醒。
- 保修凭证作为附件挂载到设备或保修记录下。

### 耗材管理

- 耗材必须绑定设备。
- 耗材更换周期以天为单位存储，展示层可转换为月或年。
- 每次更换耗材后必须生成更换记录，并重新计算下次提醒日期。
- 逾期未更换的耗材在首页看板中突出展示。

### 维修管理

- 维修状态流转建议：`待处理 -> 已报修 -> 维修中 -> 已完成`，可扩展 `已取消`。
- 维修记录必须保留故障描述、发生时间、处理结果和费用信息。
- 完成维修时允许同步更新设备状态。
- 维修费用统计必须排除已取消记录。

### 提醒通知

- 提醒任务由定时任务生成或刷新，不能依赖前端触发。
- Redis 用于提醒去重，Key 必须集中定义。
- 通知失败要记录失败原因，不允许影响主业务数据。

---

## 七、AI 服务调用

AI 是辅助能力，不允许成为核心业务强依赖。

### 可用场景

- 发票或订单文本提取设备名称、购买日期、价格、商家等字段。
- 根据故障描述生成初步排查建议。
- 根据维修历史生成设备维护总结。
- 根据耗材更换记录生成保养建议。

### 调用规则

- 所有 AI 调用必须通过统一 `AiClient` 或 `AiService`，禁止在业务代码中直接调用第三方 SDK。
- 必须支持 `mock` Provider，开发测试时可以不配置真实 API Key。
- AI 失败不能影响设备创建、维修记录保存、提醒生成等核心流程。
- AI 返回内容必须做长度限制和空值兜底。
- Prompt 模板放在 `resources/prompts/`，禁止长 Prompt 散落在 Java 代码中。
- 敏感信息不得发送给 AI，例如用户密码、JWT、完整手机号、身份证号等。

### 推荐接口

```java
public interface AiClient {
  InvoiceParseResult parseInvoiceText(String text);
  TroubleshootingSuggestion suggestTroubleshooting(DeviceContext context, String faultDescription);
  MaintenanceSummary summarizeMaintenance(DeviceContext context, List<MaintenanceRecordDTO> records);
}
```

---

## 八、Redis Key 规范

Redis Key 必须集中定义在常量类中，禁止在业务代码中随手拼接。

| 场景 | Key 示例 | 说明 |
| --- | --- | --- |
| 登录验证码 | `fixledger:captcha:{uuid}` | 验证码缓存 |
| JWT 黑名单 | `fixledger:auth:blacklist:{tokenId}` | 退出登录后 Token 失效 |
| 用户上下文 | `fixledger:user:profile:{userId}` | 用户基础信息缓存 |
| 提醒去重 | `fixledger:reminder:dedupe:{type}:{bizId}:{date}` | 避免重复提醒 |
| 首页统计 | `fixledger:dashboard:summary:{familyId}` | 首页看板缓存 |
| AI 任务状态 | `fixledger:ai:task:{taskId}` | AI 异步任务状态 |

规则：

- Key 前缀统一使用 `fixledger:`。
- TTL 必须显式设置，禁止无过期时间的临时缓存。
- 缓存更新必须考虑数据一致性，重要业务以数据库为准。

---

## 九、文件与附件规范

- 支持的附件类型：发票图片、保修卡图片、说明书 PDF、维修单图片、售后截图。
- 文件上传必须校验大小、扩展名和 MIME 类型。
- 文件访问必须鉴权，家庭空间外的用户不能访问附件。
- 文件元数据入库，文件内容存本地文件系统或 MinIO。
- 删除设备时不直接物理删除附件，优先逻辑删除或延迟清理。
- 文件路径和 Bucket 名称放配置文件，不允许硬编码。

---

## 十、事务规则

- `@Transactional` 只放在 Service 层。
- 保持事务范围最小。
- **禁止**在事务中调用外部 API，如 AI、MinIO、邮件、Webhook。
- **禁止**同类内部调用 `@Transactional` 方法，避免 AOP 代理不生效。
- 批量插入和批量更新必须使用批处理能力，避免循环单条写库。
- 状态流转方法必须加事务，保证状态与操作记录一致。

---

## 十一、日志规范

- 使用 SLF4J，类上可使用 `@Slf4j`。
- 结构化日志：`log.info("Device created: deviceId={}, familyId={}", deviceId, familyId)`。
- 异常必须作为最后一个参数：`log.error("Reminder send failed: reminderId={}", reminderId, e)`。
- **禁止** `log.error("Error: {}", e.getMessage())`，会丢失堆栈。
- 日志中不得输出密码、Token、API Key、完整手机号等敏感信息。
- 定时任务必须打印开始、结束、处理数量和失败数量。

---

## 十二、数据库规范

- 数据库使用 MySQL 8。
- ORM 使用 MyBatis Plus。
- 表名使用小写下划线，业务表建议使用 `fl_` 前缀。
- 主键统一使用 `id`，类型优先 `BIGINT`。
- 通用字段：`created_at`、`updated_at`、`created_by`、`updated_by`、`deleted`。
- 需要数据隔离的业务表必须包含 `family_id`。
- 常用查询条件必须加索引，如 `family_id`、`device_id`、`status`、`next_remind_at`。
- 金额使用 `DECIMAL`，禁止使用浮点类型。
- 时间使用 `DATETIME`，Java 侧使用 `LocalDateTime`；纯日期使用 `LocalDate`。
- 逻辑删除使用 `deleted` 字段，禁止随意物理删除核心业务数据。

核心表建议：

```text
sys_user
sys_role
sys_user_role
sys_operation_log
fl_family_space
fl_family_member
fl_device_category
fl_device_asset
fl_warranty_record
fl_consumable_item
fl_consumable_replace_record
fl_maintenance_record
fl_reminder_task
fl_notification_record
fl_file_resource
fl_ai_analysis
```

---

## 十三、配置管理

- 配置文件：`application.yml` + `application-{profile}.yml` + `.env`。
- 敏感信息放 `.env` 或环境变量，不入版本控制。
- `.env.example` 只放示例值，不放真实密钥。
- 业务配置使用 `@ConfigurationProperties`，如 `JwtProperties`、`FileStorageProperties`、`AiProperties`、`ReminderProperties`。
- **禁止** `@Value` 散落在 Service 中。
- 不同环境配置必须可切换：`dev`、`test`、`prod`。

---

## 十四、前端规范

- 前端使用 Vue3 + TypeScript + Vite + Element Plus。
- 页面放在 `views/`，公共组件放在 `components/`。
- API 请求集中在 `api/`，禁止页面里直接散落 Axios URL。
- 类型定义集中在 `types/`，禁止大量使用 `any`。
- 状态管理使用 Pinia。
- 路由权限在 Router Guard 中处理。
- 表格页必须支持分页、筛选和加载状态。
- 表单必须有前端校验，后端仍必须二次校验。
- 关键页面：设备列表、设备详情、保修提醒、耗材提醒、维修记录、首页看板。

---

## 十五、格式与命名

### Java

- 2 空格缩进，列限制 100 字符。
- 类名 UpperCamelCase，方法名 lowerCamelCase，常量 UPPER_SNAKE_CASE。
- 禁止通配符导入。
- 优先使用现代 Java 特性：`record`、`switch` 表达式、pattern matching、text blocks。
- 避免内联全限定类名，使用 import。
- 方法不要过长，超过 80 行优先拆分。
- 业务枚举必须有 code 和 description。

### TypeScript / Vue

- 2 空格缩进。
- 组件名 UpperCamelCase。
- 组合式 API 优先使用 `<script setup lang="ts">`。
- API 方法命名使用动词开头，如 `getDevicePage`、`createDevice`、`updateWarranty`。
- 页面组件只做展示和交互编排，复杂逻辑抽到 composables 或 stores。

---

## 十六、测试规范

- 后端使用 JUnit 5 + Mockito + AssertJ。
- 测试方法使用 `@DisplayName` 中文描述测试意图。
- 复杂测试使用 `@Nested` 按功能分组。
- Service 层必须覆盖核心业务：保修提醒、耗材提醒、维修状态流转、提醒去重。
- Controller 层测试接口参数校验和权限拦截。
- 集成测试使用 `application-test.yml`。
- Redis 相关测试可使用真实 Redis 或 Testcontainers。
- AI 测试默认使用 Mock Provider，不依赖真实 API。

---

## 十七、文档规范

- 需求变更先更新 `docs/requirements.md`。
- 架构调整更新 `docs/architecture.md`。
- 新增接口更新 `docs/api.md`。
- 新增或修改表结构更新 `docs/database.md`。
- 新增页面或交互变化更新 `docs/ui.md`。
- 开发任务拆分和进度更新 `docs/tasks.md`。
- README 面向项目展示，AGENTS 面向编码约束，不要混用。

---

## 十八、速查：禁止清单

| 禁止项 | 原因 |
| --- | --- |
| `throw new RuntimeException(...)` | 绕过统一异常处理，必须用 `BusinessException` |
| 直接返回 Entity 给前端 | 暴露内部结构和敏感字段 |
| `@Value` 散落在 Service 中 | 配置应集中到 `@ConfigurationProperties` |
| 事务内调用 AI、文件存储、邮件、Webhook | 占用数据库连接，外部失败影响事务 |
| 同类内部调用 `@Transactional` 方法 | AOP 代理不生效 |
| `catch (Exception e) {}` 静默忽略 | 隐藏真实错误 |
| 循环单条查询或写库 | 性能差，改用批量操作 |
| 硬编码密钥、路径、Bucket、URL | 安全和可维护性风险 |
| 日志输出密码、Token、API Key | 安全风险 |
| Controller 写业务逻辑 | 破坏分层，难测试 |
| 页面直接拼接接口 URL | API 管理混乱 |
| AI 结果直接覆盖用户数据 | AI 只能辅助，用户必须确认 |
| 删除设备时物理删除历史记录 | 破坏设备生命周期记录 |

---

## 十九、当前阶段执行原则

项目当前处于需求设计和脚手架准备阶段。后续执行时必须遵守：

1. 先补文档，再写代码；涉及需求、表结构、接口、页面的变更必须同步到 `docs/`。
2. 先完成核心闭环：设备档案 → 保修记录 → 耗材提醒 → 维修记录 → 首页看板。
3. Redis、文件存储、AI、通知等增强能力要解耦实现，不能阻塞核心功能。
4. AI 功能默认支持关闭和 Mock 模式。
5. 每个阶段完成后补充 README 或任务进度说明。
