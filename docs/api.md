# FixLedger API 设计

## 1. API 设计原则

- API 前缀统一使用 `/api`。
- 返回值统一使用 `Result<T>`。
- 认证方式使用 Bearer Token。
- 分页接口统一使用 `pageNum`、`pageSize`，响应统一返回 `PageResponse<T>`。
- 请求体使用 JSON。
- 文件上传使用 `multipart/form-data`。
- Controller 只做参数校验和 Service 调用。
- 业务异常统一返回错误码和错误信息。

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

### 2.3 分页响应

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
```

响应：

```json
true
```

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
GET /api/families/{familyId}/devices?pageNum=1&pageSize=10&keyword=净水器&categoryId=1&status=NORMAL
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
      "warrantyStatus": "VALID",
      "nextReminderDate": "2025-09-01"
    }
  ]
}
```

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
  "completedAt": "2026-05-11 18:00:00"
}
```

规则：

- 状态只能按规则流转。
- 完成维修后可同步更新设备状态。

### 11.6 删除维修记录

```http
DELETE /api/families/{familyId}/maintenance-records/{maintenanceId}
```

## 12. Reminder 提醒接口

### 12.1 查询提醒列表

```http
GET /api/families/{familyId}/reminders?pageNum=1&pageSize=10&status=PENDING&type=WARRANTY_EXPIRE_SOON
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

说明：

- 开发阶段用于验证定时任务逻辑。
- 生产环境应限制管理员权限。

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
| bizType | string | 是 | DEVICE / WARRANTY / MAINTENANCE / CONSUMABLE / MANUAL |
| bizId | long | 是 | 业务 ID |

响应：

```json
{
  "id": 1,
  "originalName": "invoice.jpg",
  "contentType": "image/jpeg",
  "fileSize": 123456
}
```

### 13.2 查询业务附件

```http
GET /api/families/{familyId}/files?bizType=DEVICE&bizId=1
```

### 13.3 下载附件

```http
GET /api/families/{familyId}/files/{fileId}/download
```

### 13.4 删除附件

```http
DELETE /api/families/{familyId}/files/{fileId}
```

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

### 15.2 故障排查建议

```http
POST /api/families/{familyId}/ai/troubleshooting
```

请求：

```json
{
  "deviceId": 1,
  "faultDescription": "净水器出水变慢，机器有异响"
}
```

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

## 16. System 系统接口

### 16.1 查询操作日志

```http
GET /api/system/operation-logs?pageNum=1&pageSize=10&module=DEVICE
```

需要管理员权限。

### 16.2 查询系统字典

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
- 操作日志查询。
- AI 真实 Provider。
- MinIO 临时访问 URL。

## 18. 接口安全要求

- 除登录注册外，接口必须认证。
- 所有带 `familyId` 的接口必须校验用户是否属于该家庭空间。
- 文件下载必须校验权限。
- AI 接口不能接收敏感字段。
- 管理接口必须限制角色。
