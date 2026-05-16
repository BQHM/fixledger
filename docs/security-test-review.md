# P9 测试与安全审查记录

## 1. 审查目标

本记录用于沉淀 P9.3 测试体系补强和 P9.4 安全与数据隔离审查结论。它不是替代代码测试，而是把“为什么这些测试重要、哪些安全边界已经覆盖、还有哪些能力放到后续阶段”写清楚，方便后续维护和面试讲解。

本次审查遵守：

- `AGENTS.md`：家庭设备生命周期管理边界、统一异常、家庭空间隔离、文件安全、AI 辅助定位。
- `docs/spec.md`：测试策略、边界、成功标准。
- `docs/decisions/`：核心技术栈、RustFS 文件存储、AI 辅助定位、家庭场景 UI 决策。

## 2. P9.3 测试体系补强结论

P9.3 的目标不是追求测试数量，而是覆盖最容易被面试官追问、也最容易出线上事故的风险点。

| 风险点 | 覆盖方式 | 代表测试 |
| --- | --- | --- |
| 未登录访问受保护接口 | Spring Security + MockMvc 验证 401 | `AuthControllerTest.meWithoutTokenReturnsUnauthorized` |
| 登录后身份可用 | 注册、登录、携带 Bearer Token 查询当前用户 | `AuthControllerTest.meWithTokenReturnsCurrentUser` |
| 退出登录后旧 Token 失效 | JWT jti + Redis 黑名单 + MockMvc 回归 | `AuthControllerTest.logoutInvalidatesCurrentToken` |
| 无效 Token 被拒绝 | 认证过滤器捕获 JWT 解析异常并清空上下文 | `AuthControllerTest.invalidTokenReturnsUnauthorized` |
| 请求解析错误不暴露内部细节 | 全局异常处理返回通用文案 | `GlobalExceptionHandlerTest.badRequestDoesNotExposeInternalExceptionMessage` |
| 文件扩展名和 MIME 校验 | 上传非法扩展名、非法 MIME 均抛业务异常 | `FileResourceServiceTest.rejectInvalidExtensionAndMime` |
| 文件名路径穿越 | 拒绝 `../invoice.jpg` 等路径字符 | `FileResourceServiceTest.rejectPathTraversalFileName` |
| 附件下载越权 | 非家庭成员下载附件返回 `FORBIDDEN` | `FileResourceServiceTest.nonFamilyMemberCannotDownloadFile` |
| 家庭空间越权 | 非家庭成员访问家庭、设备、保修、耗材、维修、AI 数据被拒绝 | `FamilyServiceTest`、`DeviceAssetServiceTest`、`WarrantyServiceTest`、`ConsumableServiceTest`、`MaintenanceServiceTest`、`AiServiceTest` |
| 提醒去重和事务边界 | Redis 去重、数据库失败释放去重键、重复扫描跳过写库 | `ReminderServiceTest`、`ReminderServiceTransactionBoundaryTest` |
| 状态流转规则 | 设备报废后不可恢复、维修状态按规则推进、费用统计排除取消记录 | `DeviceAssetServiceTest`、`MaintenanceServiceTest` |

最近一次后端完整验证：

```powershell
cd D:\work\work_space\Project\FixLedger\backend
mvn test -q
```

验证结果：Surefire 报告汇总 80 个测试，失败 0，错误 0，跳过 0。

## 3. P9.4 安全与数据隔离结论

### 3.1 认证与退出登录

当前后端采用 Spring Security + JWT 的无状态认证模式。JWT 本身签发后在过期前默认可用，因此退出登录如果只删除前端本地 Token，并不能让旧 Token 立即失效。

P9.2.3 已补充：

- JWT 签发时写入唯一 `jti`。
- 退出登录时解析当前 Token，把 `jti` 写入 Redis 黑名单。
- 黑名单 Key 使用 `fixledger:auth:blacklist:{tokenId}`。
- Redis TTL 使用 Token 剩余有效期，避免长期保存无意义历史数据。
- `JwtAuthenticationFilter` 每次解析 Token 后检查黑名单。

面试口径：

> JWT 是无状态的，退出登录不能只靠前端删除 Token。我给每个 Token 加了 jti，退出时把 jti 放入 Redis 黑名单，TTL 等于 Token 剩余有效期，这样旧 Token 立即失效，同时 Redis 不会无限增长。

### 3.2 家庭空间数据隔离

家庭空间隔离是 FixLedger 的核心权限模型。用户的设备、保修、耗材、维修、提醒、附件、AI 分析和看板数据都必须挂在 `family_id` 下。

审查结果：

- `asset`、`warranty`、`consumable`、`maintenance`、`reminder`、`dashboard`、`file`、`ai` 等业务 Service 均在入口调用 `familyService.checkFamilyMember(userId, familyId)`。
- 查询详情时不仅按业务 ID 查询，还会追加 `family_id` 条件，避免“猜 ID 越权”。
- 附件上传前会校验业务对象属于当前家庭，下载时通过后端鉴权后再读取对象存储内容。
- 认证模块不带 `familyId`，属于账号级能力，因此不需要家庭空间校验。

面试口径：

> 我没有只在前端隐藏菜单，也没有只靠用户传 familyId。后端 Service 第一层会校验用户是否是家庭成员，具体查设备、附件、提醒时也会同时按 id 和 family_id 查询，防止用户猜测其他家庭的数据 ID。

### 3.3 文件上传与 RustFS 安全边界

文件内容存储在 RustFS，文件元数据存储在 MySQL 的 `fl_file_resource`。业务层只依赖 `FileStorageService`，不直接依赖 RustFS SDK。

已具备的安全约束：

- 文件大小限制为 20MB。
- 允许扩展名：`jpg`、`jpeg`、`png`、`pdf`。
- 允许 MIME：`image/jpeg`、`image/png`、`application/pdf`。
- 文件名拒绝 `..`、`/`、`\` 等路径字符。
- 附件业务类型必须是设备、保修、耗材、维修或说明书等受控枚举。
- 上传前校验业务对象归属，下载前校验家庭成员权限。
- 删除附件当前是逻辑删除，物理文件清理留给后续异步任务。

面试口径：

> RustFS 只负责存文件内容，真正的权限控制在后端。前端不能直接拼对象存储地址访问文件，下载必须经过后端校验用户是否属于这个家庭，并且附件元数据也带 family_id。

### 3.4 日志与敏感信息

P9.2.3 已把 dev/test 下 `com.fixledger` 日志级别从 `debug` 收敛到 `info`，避免默认打印过细 SQL 参数、密码哈希、AI 内容或业务请求参数。

审查结果：

- 未发现 `System.out`、`System.err`、`printStackTrace`。
- 未发现日志直接输出密码、Token、API Key。
- AI 失败日志记录 `familyId`、`userId`、`deviceId` 等定位字段，不记录完整 Prompt 或完整输入文本。
- 未处理异常由 `GlobalExceptionHandler` 统一记录完整堆栈，但前端只收到通用错误响应。

## 4. 后续延期项

以下能力不阻塞 P9/P10 验收，进入 P11/P13/P15 等后续专项增强阶段：

- Refresh Token 与多端会话管理。
- 登录失败限流和验证码。
- 文件在线预览、对象存储临时 URL、附件异步清理任务。
- 真实 AI Provider 的生产密钥管理、用量控制和内容审计。
- 邮件、Webhook、短信等外部通知渠道。
- 前端 E2E 自动化测试。

## 5. 总结

P9.3/P9.4 的核心价值是把“能跑”升级为“有质量证据、能解释风险控制”。当前项目已经具备认证退出失效、家庭空间隔离、附件安全、提醒去重、异常脱敏和关键状态流转测试；后续增强将围绕更细的会话治理、自动化预览、安全扫描和生产级通知展开。
