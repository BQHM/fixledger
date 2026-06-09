# FixLedger API 设计

## 1. API 设计原则

- API 前缀统一使用 `/api`。
- 返回值统一使用 `Result<T>`。
- 认证方式使用 Bearer Token。
- 分页接口统一使用 `pageNum`、`pageSize`，响应统一返回 `PageResponse<T>`；`pageNum >= 1`，`1 <= pageSize <= 100`。
- 请求体使用 JSON。
- 文件上传使用 `multipart/form-data`。
- Controller 只做参数校验和 Service 调用。
- 业务异常统一返回错误码和错误信息。


## 1.1 接口契约边界

接口设计必须服务于 `docs/spec.md` 的家庭设备生命周期目标。新增或修改接口时，需要同步记录：

- 请求路径、认证要求、请求体、响应体和错误码。
- 是否带 `familyId`，以及家庭空间权限校验方式。
- 分页接口是否遵守 `pageNum >= 1`、`1 <= pageSize <= 100`。
- 是否涉及文件、AI、通知等外部能力；如果涉及，核心业务不能依赖外部能力成功。
- 如果接口契约发生破坏性变更，必须在 `docs/tasks.md` 记录调整说明，必要时新增 ADR。
## 2. 通用响应结构

### 2.1 成功响应

```json
{
  "code": 0,
  "message": "success",
  "data": {}
}
```

### 2.2 失败响应

```json
{
  "code": 4001,
  "message": "设备不存在",
  "data": null
}
```

### 2.3 错误响应的 HTTP 状态

失败响应体仍统一使用 `Result<Void>`，但 HTTP 状态需要按错误类别返回，避免所有业务异常都变成 `400`。

| HTTP 状态 | 适用场景 | 示例错误码 |
| --- | --- | --- |
| 400 | 请求参数错误、业务输入不合法 | `BAD_REQUEST`、日期无效、状态无效 |
| 401 | 未登录、Token 无效或登录状态失效 | `UNAUTHORIZED`、`TOKEN_INVALID` |
| 403 | 已登录但无访问权限 | `FORBIDDEN` |
| 404 | 资源不存在 | `DEVICE_NOT_FOUND`、`WARRANTY_NOT_FOUND`、`CONSUMABLE_NOT_FOUND` |
| 405 | 请求方法不支持 | `METHOD_NOT_ALLOWED` |
| 503 | 外部辅助服务暂不可用 | `AI_SERVICE_UNAVAILABLE` |
| 500 | 未预期系统异常 | `SYSTEM_ERROR` |

说明：

- `code` 是业务错误码，方便前端和后端定位具体业务问题。
- HTTP 状态是协议层错误分类，方便浏览器、网关、监控和第三方客户端理解错误类型。
- 文件下载接口成功时返回二进制流 `ResponseEntity<Resource>`，不包 `Result<T>`；下载失败时仍由全局异常处理器返回统一错误结构。

### 2.4 分页响应

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "pageNum": 1,
    "pageSize": 10,
    "total": 100,
    "pages": 10,
    "records": []
  }
}
```

## 3. 认证约定

需要登录的接口携带：

```http
Authorization: Bearer <accessToken>
```

登录、注册接口不需要 Token。

## 4. 错误码概览

| 范围 | 模块 |
| --- | --- |
| 1xxx | 通用错误 |
| 2xxx | 认证与用户 |
| 3xxx | 家庭空间 |
| 4xxx | 设备档案 |
| 5xxx | 保修 |
| 6xxx | 耗材 |
| 7xxx | 维修 |
| 8xxx | 提醒通知 |
| 9xxx | 文件存储 |
| 10xxx | AI 服务 |
| 11xxx | 系统配置 |

## 5. Auth 认证接口

### 5.1 用户注册

```http
POST /api/auth/register
```

请求：

```json
{
  "username": "zhangsan",
  "email": "zhangsan@example.com",
  "password": "123456",
  "nickname": "张三"
}
```

响应：

```json
{
  "userId": 1,
  "username": "zhangsan",
  "nickname": "张三"
}
```

规则：

- 用户名唯一。
- 邮箱如果填写必须唯一。
- 密码加密存储。
- 注册成功后默认创建家庭空间。

### 5.2 用户登录

```http
POST /api/auth/login
```

请求：

```json
{
  "account": "zhangsan",
  "password": "123456"
}
```

响应：

```json
{
  "accessToken": "token",
  "expiresIn": 86400,
  "user": {
    "id": 1,
    "username": "zhangsan",
    "nickname": "张三"
  },
  "currentFamilyId": 1
}
```

### 5.3 退出登录

```http
POST /api/auth/logout
Authorization: Bearer <accessToken>
```

响应：

```json
true
```

规则：

- 后端会解析当前 JWT 的 `jti`，写入 Redis 黑名单。
- 黑名单 TTL 使用 Token 剩余有效期，避免 Redis 长期保存已过期令牌。
- 退出后的旧 Token 再访问受保护接口会返回未认证。

### 5.4 获取当前用户

```http
GET /api/auth/me
```

响应：

```json
{
  "id": 1,
  "username": "zhangsan",
  "nickname": "张三",
  "email": "zhangsan@example.com"
}
```

## 6. Family 家庭空间接口

### 6.1 查询家庭空间列表

```http
GET /api/families
```

响应：

```json
[
  {
    "id": 1,
    "name": "我的家",
    "role": "OWNER",
    "ownerUserId": 1
  }
]
```

### 6.2 创建家庭空间

```http
POST /api/families
```

请求：

```json
{
  "name": "我的家",
  "description": "家庭设备管理"
}
```

响应：

```json
{
  "id": 1,
  "name": "我的家"
}
```

### 6.3 修改家庭空间

```http
PUT /api/families/{familyId}
```

请求：

```json
{
  "name": "杭州的家",
  "description": "杭州住处设备"
}
```

### 6.4 查询家庭成员

```http
GET /api/families/{familyId}/members
```

### 6.5 邀请家庭成员（二期）

```http
POST /api/families/{familyId}/members/invite
```

请求：

```json
{
  "email": "member@example.com",
  "role": "MEMBER"
}
```

## 7. Device Category 设备分类接口

### 7.1 查询分类列表

```http
GET /api/families/{familyId}/device-categories
```

响应：

```json
[
  {
    "id": 1,
    "name": "数码设备",
    "icon": "Monitor",
    "sortOrder": 1,
    "systemDefault": true
  }
]
```

### 7.2 新增分类

```http
POST /api/families/{familyId}/device-categories
```

请求：

```json
{
  "name": "影音设备",
  "icon": "VideoCamera",
  "sortOrder": 10
}
```

### 7.3 修改分类

```http
PUT /api/families/{familyId}/device-categories/{categoryId}
```

### 7.4 删除分类

```http
DELETE /api/families/{familyId}/device-categories/{categoryId}
```

规则：

- 分类下存在设备时不允许删除。

## 8. Device 设备档案接口

### 8.1 分页查询设备

```http
GET /api/families/{familyId}/devices?pageNum=1&pageSize=10&keyword=净水器&categoryId=1&status=NORMAL&brand=小米
```

响应：

```json
{
  "pageNum": 1,
  "pageSize": 10,
  "total": 1,
  "pages": 1,
  "records": [
    {
      "id": 1,
      "name": "小米净水器",
      "brand": "小米",
      "model": "S1",
      "categoryName": "厨房设备",
      "purchaseDate": "2025-03-01",
      "purchasePrice": 1999.00,
      "location": "厨房",
      "status": "NORMAL",
      "warrantyStatus": null,
      "nextReminderDate": null
    }
  ]
}
```

说明：

- 当前设备分页查询支持 `keyword`、`categoryId`、`status`、`brand`。
- `warrantyStatus` 和 `nextReminderDate` 是列表响应预留字段，当前后端返回 `null`；保修状态和下次提醒可在后续做列表聚合优化。

### 8.2 创建设备

```http
POST /api/families/{familyId}/devices
```

请求：

```json
{
  "categoryId": 1,
  "name": "小米净水器",
  "brand": "小米",
  "model": "S1",
  "serialNumber": "SN123456",
  "purchaseDate": "2025-03-01",
  "purchaseChannel": "京东",
  "purchasePrice": 1999.00,
  "location": "厨房",
  "remark": "厨房主净水器"
}
```

响应：

```json
{
  "id": 1
}
```

### 8.3 查询设备详情

```http
GET /api/families/{familyId}/devices/{deviceId}
```

响应：

```json
{
  "id": 1,
  "name": "小米净水器",
  "brand": "小米",
  "model": "S1",
  "status": "NORMAL",
  "purchaseDate": "2025-03-01",
  "purchasePrice": 1999.00,
  "location": "厨房",
  "warranties": [],
  "consumables": [],
  "maintenanceRecords": [],
  "files": []
}
```

### 8.4 修改设备

```http
PUT /api/families/{familyId}/devices/{deviceId}
```

### 8.5 删除设备

```http
DELETE /api/families/{familyId}/devices/{deviceId}
```

规则：

- 逻辑删除。
- 不物理删除维修历史和附件元数据。

### 8.6 修改设备状态

```http
PATCH /api/families/{familyId}/devices/{deviceId}/status
```

请求：

```json
{
  "status": "REPAIRING",
  "reason": "已送修"
}
```

## 9. Warranty 保修接口

### 9.1 查询设备保修记录

```http
GET /api/families/{familyId}/devices/{deviceId}/warranties
```

### 9.2 创建保修记录

```http
POST /api/families/{familyId}/devices/{deviceId}/warranties
```

请求：

`warrantyType` 可选值：`OFFICIAL`、`EXTENDED`、`STORE`、`OTHER`。

```json
{
  "warrantyType": "OFFICIAL",
  "startDate": "2025-03-01",
  "endDate": "2027-03-01",
  "remindDaysBefore": 30,
  "servicePhone": "400-xxx-xxxx",
  "serviceAddress": "官方售后网点",
  "serviceNote": "整机保修 2 年"
}
```

### 9.3 修改保修记录

```http
PUT /api/families/{familyId}/warranties/{warrantyId}
```

### 9.4 删除保修记录

```http
DELETE /api/families/{familyId}/warranties/{warrantyId}
```

### 9.5 查询即将过保设备

```http
GET /api/families/{familyId}/warranties/expiring?pageNum=1&pageSize=10&days=30
```

## 10. Consumable 耗材接口

### 10.1 查询设备耗材

```http
GET /api/families/{familyId}/devices/{deviceId}/consumables
```

响应：

```json
[
  {
    "id": 1,
    "name": "PP 棉滤芯",
    "cycleDays": 180,
    "lastReplacedDate": "2025-03-01",
    "nextRemindDate": "2025-09-01",
    "status": "NORMAL"
  }
]
```

### 10.2 新增耗材

```http
POST /api/families/{familyId}/devices/{deviceId}/consumables
```

请求：

```json
{
  "name": "PP 棉滤芯",
  "brand": "小米",
  "model": "PPC-001",
  "cycleDays": 180,
  "lastReplacedDate": "2025-03-01",
  "remindDaysBefore": 7,
  "remark": "半年一换"
}
```

### 10.3 修改耗材

```http
PUT /api/families/{familyId}/consumables/{consumableId}
```

### 10.4 删除耗材

```http
DELETE /api/families/{familyId}/consumables/{consumableId}
```

### 10.5 记录耗材更换

```http
POST /api/families/{familyId}/consumables/{consumableId}/replace-records
```

请求：

```json
{
  "replacedDate": "2025-09-01",
  "cost": 89.00,
  "note": "自行更换"
}
```

规则：

- 创建更换记录后自动更新耗材的 `lastReplacedDate` 和 `nextRemindDate`。

### 10.6 查询耗材更换记录

```http
GET /api/families/{familyId}/consumables/{consumableId}/replace-records
```

### 10.7 查询即将更换耗材

```http
GET /api/families/{familyId}/consumables/due-soon?pageNum=1&pageSize=10&days=7
```

## 11. Maintenance 维修接口

### 11.1 分页查询维修记录

```http
GET /api/families/{familyId}/maintenance-records?pageNum=1&pageSize=10&deviceId=1&status=REPAIRING
```

### 11.2 创建维修记录

```http
POST /api/families/{familyId}/devices/{deviceId}/maintenance-records
```

请求：

```json
{
  "title": "净水器出水变慢",
  "faultDescription": "出水速度明显变慢，机器偶尔有异响",
  "occurredAt": "2026-05-10 10:30:00",
  "repairChannel": "官方售后",
  "repairContact": "400-xxx-xxxx"
}
```

响应：

```json
{
  "id": 1
}
```

### 11.3 查询维修详情

```http
GET /api/families/{familyId}/maintenance-records/{maintenanceId}
```

### 11.4 修改维修记录

```http
PUT /api/families/{familyId}/maintenance-records/{maintenanceId}
```

### 11.5 维修状态流转

```http
PATCH /api/families/{familyId}/maintenance-records/{maintenanceId}/status
```

请求：

```json
{
  "status": "COMPLETED",
  "resultDescription": "更换滤芯后恢复正常",
  "repairCost": 89.00,
  "completedAt": "2026-05-11 18:00:00",
  "syncDeviceRepaired": true
}
```

规则：

- 状态只能按规则流转。
- 完成维修后可同步更新设备状态。

### 11.6 删除维修记录

```http
DELETE /api/families/{familyId}/maintenance-records/{maintenanceId}
```

### 11.7 维修费用统计

```http
GET /api/families/{familyId}/maintenance-records/cost-summary?startDate=2026-05-01&endDate=2026-05-31
```

响应：

```json
{
  "totalCost": 268.00,
  "recordCount": 2
}
```

规则：

- 已取消维修不计入费用统计。
- 日期范围按 `completedAt` 过滤。

## 12. Reminder 提醒接口

### 12.1 查询提醒列表

```http
GET /api/families/{familyId}/reminders?pageNum=1&pageSize=10&status=PENDING&type=WARRANTY_EXPIRE_SOON
```

响应：

```json
{
  "pageNum": 1,
  "pageSize": 10,
  "total": 1,
  "pages": 1,
  "records": [
    {
      "id": 1,
      "reminderType": "WARRANTY_EXPIRE_SOON",
      "bizType": "WARRANTY",
      "bizId": 1,
      "title": "净水器保修即将到期",
      "content": "小米净水器将在 2026-05-20 过保",
      "remindAt": "2026-05-10 08:00:00",
      "status": "PENDING"
    }
  ]
}
```

### 12.2 查询未读提醒数量

```http
GET /api/families/{familyId}/reminders/unread-count
```

响应：

```json
{
  "count": 3
}
```

### 12.3 标记提醒已读

```http
PATCH /api/families/{familyId}/reminders/{reminderId}/read
```

### 12.4 忽略提醒

```http
PATCH /api/families/{familyId}/reminders/{reminderId}/ignore
```

### 12.5 手动触发提醒扫描（开发/管理员）

```http
POST /api/families/{familyId}/reminders/scan
```

响应：

```json
{
  "warrantyCreated": 2,
  "consumableCreated": 1,
  "notificationCreated": 3,
  "skippedDuplicate": 4,
  "failedCount": 0
}
```

说明：

- 开发阶段用于验证定时任务逻辑。
- 生产环境应限制管理员权限。
- 同一事项同一天通过 Redis Key 去重，避免重复生成提醒。

## 13. File 附件接口

### 13.1 上传附件

```http
POST /api/families/{familyId}/files
Content-Type: multipart/form-data
```

参数：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| file | File | 是 | 文件 |
| bizType | string | 是 | DEVICE / WARRANTY / MANUAL / MAINTENANCE / CONSUMABLE |
| bizId | long | 是 | 业务 ID |

响应：

```json
{
  "id": 1,
  "originalName": "invoice.jpg",
  "contentType": "image/jpeg",
  "fileSize": 123456,
  "bizType": "DEVICE",
  "bizId": 1
}
```

限制：单文件最大 20MB；当前允许 `jpg`、`jpeg`、`png`、`pdf`。

### 13.2 查询业务附件

```http
GET /api/families/{familyId}/files?bizType=DEVICE&bizId=1
```

### 13.3 查询设备凭证盒

```http
GET /api/families/{familyId}/devices/{deviceId}/credential-box
```

用途：按设备一次聚合购买凭证、说明书、保修凭证、维修凭证和耗材凭证，前端不再分别查询保修、维修、耗材和多组附件。

响应：

```json
{
  "deviceId": 1,
  "deviceName": "小米净水器 S1",
  "location": "厨房",
  "completionPercent": 80,
  "archivedTypeCount": 4,
  "totalTypeCount": 5,
  "totalFileCount": 6,
  "totalFileSize": 1048576,
  "groups": [
    {
      "bizType": "DEVICE",
      "title": "购买凭证",
      "shortTitle": "发票",
      "description": "购买发票、订单截图和支付凭证",
      "targets": [
        {
          "bizId": 1,
          "label": "小米净水器 S1"
        }
      ],
      "files": [
        {
          "id": 10,
          "originalName": "invoice.jpg",
          "contentType": "image/jpeg",
          "fileSize": 123456,
          "bizType": "DEVICE",
          "bizId": 1,
          "targetLabel": "小米净水器 S1"
        }
      ]
    }
  ]
}
```

规则：

- 固定返回 `DEVICE`、`MANUAL`、`WARRANTY`、`MAINTENANCE`、`CONSUMABLE` 五类分组。
- `targets` 表示当前类型可挂载的业务对象；设备和说明书指向设备 ID，保修、维修、耗材指向对应记录 ID。
- 后端统一校验登录用户是否属于该家庭空间，并且查询业务对象时同时带 `familyId` 和 `deviceId`。
- 上传、下载、预览和删除仍复用附件接口，凭证盒接口只负责聚合读取。

### 13.4 下载附件

```http
GET /api/families/{familyId}/files/{fileId}/download
```

### 13.5 删除附件

```http
DELETE /api/families/{familyId}/files/{fileId}
```


### 13.6 查询说明书全文

```http
GET /api/families/{familyId}/devices/{deviceId}/manuals/search?keyword=reset
```

用途：在指定设备下搜索已归档说明书的文本索引和文件名，用于从凭证盒快速定位 PDF 说明书。

响应：

```json
[
  {
    "fileId": 20,
    "fileName": "router-manual.pdf",
    "contentType": "application/pdf",
    "fileSize": 204800,
    "snippet": "hold the reset button for 8 seconds"
  }
]
```

规则：

- 需要登录，并校验当前用户属于 `familyId` 对应家庭空间。
- 查询前会校验 `deviceId` 属于当前家庭空间。
- `keyword` 必填，最大 64 个字符；空关键词返回 `BAD_REQUEST`。
- 结果只返回说明书附件元数据和短片段，不返回完整索引文本。
- P16.2 第一版只索引上传时可直接提取到的 PDF 文本和文件名；扫描件 OCR、复杂 PDF 解析和真实 AI Provider 不属于当前接口依赖。

### 13.7 RustFS 存储说明

接口路径、请求参数和响应结构不因 RustFS 接入而变化。上传时后端会将文件内容写入 RustFS Bucket，并把对象 Key 保存到 `fl_file_resource.storage_path`。`storage-type=rustfs`、`s3` 或 `minio` 均复用 S3 兼容实现。下载时仍通过：

```http
GET /api/families/{familyId}/files/{fileId}/download
```

后端先校验登录用户是否属于该家庭空间，再从 RustFS 读取对象流返回。前端不直接访问 RustFS 对象地址。

## 14. Dashboard 看板接口

### 14.1 首页总览

```http
GET /api/families/{familyId}/dashboard/summary
```

响应：

```json
{
  "deviceTotal": 25,
  "warrantyExpiringCount": 3,
  "warrantyExpiredCount": 8,
  "consumableDueSoonCount": 2,
  "consumableOverdueCount": 1,
  "repairingCount": 1,
  "monthlyMaintenanceCost": 268.00
}
```

### 14.2 设备分类分布

```http
GET /api/families/{familyId}/dashboard/device-category-distribution
```

响应：

```json
[
  {
    "categoryName": "数码设备",
    "count": 8
  }
]
```

### 14.3 维修费用趋势

```http
GET /api/families/{familyId}/dashboard/maintenance-cost-trend?months=6
```

响应：

```json
[
  {
    "month": "2026-01",
    "cost": 120.00
  }
]
```

### 14.4 提醒日历

```http
GET /api/families/{familyId}/dashboard/reminder-calendar?startDate=2026-05-01&endDate=2026-05-31
```

响应：

```json
[
  {
    "date": "2026-05-20",
    "count": 1,
    "reminders": [
      {
        "id": 1,
        "reminderType": "WARRANTY_EXPIRE_SOON",
        "title": "净水器保修即将到期",
        "status": "PENDING",
        "bizType": "WARRANTY",
        "bizId": 1,
        "remindAt": "2026-05-20 08:00:00"
      }
    ]
  }
]
```

## 15. AI 辅助接口

### 15.1 发票文本提取

```http
POST /api/families/{familyId}/ai/invoice-parse
```

请求：

```json
{
  "text": "商品名称：戴森吸尘器 V12\n购买日期：2026-01-15\n金额：3999\n销售方：京东自营"
}
```

响应：

```json
{
  "analysisId": 1,
  "deviceName": "戴森吸尘器 V12",
  "purchaseDate": "2026-01-15",
  "price": 3999.00,
  "seller": "京东自营",
  "suggestedCategory": "清洁设备"
}
```

规则：

- AI 结果不能直接创建设备，必须由用户确认。
- 开发测试默认使用 Mock Provider，不需要真实 API Key。
- AI 分析结果会写入 `fl_ai_analysis` 便于追溯。

### 15.2 故障排查建议

```http
POST /api/families/{familyId}/ai/troubleshooting
```

请求：

```json
{
  "deviceId": 1,
  "maintenanceId": 10,
  "faultDescription": "净水器出水变慢，机器有异响"
}
```

说明：`maintenanceId` 可选，用于把本次故障建议和已有维修记录建立追溯关系。

响应：

```json
{
  "analysisId": 2,
  "summary": "设备出现出水变慢和异响，可能与滤芯、进水压力或管路堵塞有关。",
  "suggestions": [
    "检查滤芯是否达到更换周期",
    "检查进水阀是否完全打开",
    "检查管路是否弯折或堵塞"
  ]
}
```

### 15.3 维修记录总结

```http
POST /api/families/{familyId}/ai/maintenance-summary
```

请求：

```json
{
  "deviceId": 1
}
```

响应：

```json
{
  "analysisId": 3,
  "summary": "该设备近一年共有 2 次维修，主要问题集中在滤芯堵塞和出水速度下降。",
  "careSuggestion": "建议按 6 个月周期更换滤芯，并定期检查进水压力。"
}
```

## 16. System 系统接口（二期规划）

### 16.1 查询操作日志

```http
GET /api/system/operation-logs?pageNum=1&pageSize=10&module=DEVICE
```

需要管理员权限；当前版本暂未实现系统管理 Controller。

### 16.2 查询系统字典（二期规划）

```http
GET /api/system/dictionaries?type=device_status
```

响应：

```json
[
  {
    "label": "正常使用",
    "value": "NORMAL"
  }
]
```

## 17. 接口优先级

### 17.1 MVP 必须实现

- Auth 认证接口。
- Family 家庭空间接口。
- Device Category 分类接口。
- Device 设备档案接口。
- Warranty 保修接口。
- Consumable 耗材接口。
- Maintenance 维修接口。
- Reminder 基础提醒接口。
- Dashboard 首页总览接口。
- File 上传和查询接口。
- AI Mock 接口。

### 17.2 二期实现

- 家庭成员邀请。
- 邮件通知。
- 操作日志查询和系统字典接口。
- AI 真实 Provider。
- 对象存储临时访问 URL（可选，当前 RustFS 下载仍由后端鉴权后转发对象流）。

## 18. 接口安全要求

- 除登录注册外，接口必须认证。
- 所有带 `familyId` 的接口必须校验用户是否属于该家庭空间。
- 文件下载必须校验权限。
- AI 接口不能接收敏感字段。
- 管理接口必须限制角色。
- AI 接口返回内容需要做长度限制和空值兜底，不能让 AI 结果自动覆盖用户数据。

## 19. P7 本地演示与 OpenAPI

P7 提供 Docker Compose 后端演示环境。启动后可以通过以下地址查看服务状态和接口文档：

```text
http://localhost:8080/actuator/health
http://localhost:8080/swagger-ui.html
http://localhost:8080/v3/api-docs
```

演示账号：

```text
username: demo
password: fixledger123
familyId: 1
```

推荐演示调用顺序：

1. `POST /api/auth/login` 获取 `accessToken`。
2. `GET /api/families` 查看默认家庭空间。
3. `GET /api/families/1/devices?pageNum=1&pageSize=10` 查看示例设备。
4. `GET /api/families/1/dashboard/summary` 查看首页统计。
5. `POST /api/families/1/reminders/scan` 手动触发提醒扫描。
6. `POST /api/families/1/ai/troubleshooting` 演示 Mock AI 故障建议。

演示数据只用于本地和面试展示，生产环境不要启用 `SQL_INIT_MODE=always`。
## 20. P10.3 当前接口实现对齐说明

截至 P10.3 接口复核，当前后端 Controller 已实现的接口范围如下：

- `AuthController`：注册、登录、退出登录、当前用户。
- `FamilyController`：家庭列表、创建家庭、修改家庭、家庭成员列表。
- `DeviceCategoryController`：设备分类列表、新增、修改、删除。
- `DeviceAssetController`：设备分页、创建、详情、修改、删除、状态修改。
- `WarrantyController`：设备保修列表、新增、修改、删除、即将过保查询。
- `ConsumableController`：设备耗材列表、新增、修改、删除、记录更换、更换记录列表、即将更换查询。
- `MaintenanceController`：维修分页、创建、详情、修改、状态流转、删除、费用统计。
- `ReminderController`：提醒分页、未读数量、标记已读、忽略、手动扫描。
- `FileResourceController`：附件上传、查询、下载、逻辑删除。
- `DashboardController`：我的家总览、分类分布、维修费用趋势、提醒日历。
- `AiController`：票据文本提取、故障排查建议、维修总结。

当前暂未实现的接口能力：

- 家庭成员邀请、移除和角色调整。
- 系统操作日志、系统字典和管理员接口。
- 邮件、Webhook 等外部通知接口。
- 对象存储临时访问 URL（当前 RustFS 下载仍由后端鉴权后转发对象流）。
- Refresh Token 和多端会话管理接口。

接口分页统一遵守 `pageNum >= 1`、`1 <= pageSize <= 100`，前端 Axios 请求拦截器也会对分页参数做兜底修正。

P10.3 复核结论：

- 当前设备分页接口没有 `warrantyStatus` 查询参数，列表中的 `warrantyStatus` 和 `nextReminderDate` 是后续聚合优化预留字段。
- 当前提醒扫描接口只生成保修和耗材提醒，维修待跟进仅保留提醒类型。
- 当前 RustFS 文件下载仍走后端鉴权转发，不暴露对象存储临时 URL。
