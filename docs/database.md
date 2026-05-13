# FixLedger 数据库设计

## 1. 设计原则

数据库使用 MySQL 8，ORM 使用 MyBatis Plus。

设计原则：

- 表名使用小写下划线。
- 系统表使用 `sys_` 前缀。
- 业务表使用 `fl_` 前缀，表示 FixLedger。
- 主键统一使用 `id BIGINT`。
- 业务表优先逻辑删除。
- 家庭空间是核心数据隔离维度，需要隔离的表必须包含 `family_id`。
- 金额使用 `DECIMAL`，禁止浮点类型。
- 时间使用 `DATETIME`；Java 侧使用 `LocalDateTime`，纯日期使用 `LocalDate`。
- 附件只存元数据，文件内容存本地文件系统或 MinIO。

## 2. 通用字段

大部分业务表包含以下字段：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | BIGINT | 主键 |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |
| created_by | BIGINT | 创建人 |
| updated_by | BIGINT | 更新人 |
| deleted | TINYINT | 逻辑删除，0 未删除，1 已删除 |

建议由 MyBatis Plus 自动填充：

- `created_at`
- `updated_at`
- `created_by`
- `updated_by`
- `deleted`

## 3. 枚举约定

### 3.1 用户状态 `user_status`

| 值 | 说明 |
| --- | --- |
| ENABLED | 启用 |
| DISABLED | 禁用 |

### 3.2 家庭成员角色 `family_role`

| 值 | 说明 |
| --- | --- |
| OWNER | 所有者 |
| MEMBER | 成员 |
| VIEWER | 只读成员 |

### 3.3 设备状态 `device_status`

| 值 | 说明 |
| --- | --- |
| NORMAL | 正常使用 |
| PENDING_REPAIR | 待维修 |
| REPAIRING | 维修中 |
| REPAIRED | 已维修 |
| IDLE | 闲置 |
| SCRAPPED | 已报废 |

### 3.4 保修类型 `warranty_type`

| 值 | 说明 |
| --- | --- |
| OFFICIAL | 官方保修 |
| EXTENDED | 延保 |
| STORE | 店铺保修 |
| OTHER | 其他 |

### 3.5 耗材状态 `consumable_status`

| 值 | 说明 |
| --- | --- |
| NORMAL | 正常 |
| DUE_SOON | 即将到期 |
| OVERDUE | 已逾期 |
| DISABLED | 停用 |

### 3.6 维修状态 `maintenance_status`

| 值 | 说明 |
| --- | --- |
| PENDING | 待处理 |
| REPORTED | 已报修 |
| REPAIRING | 维修中 |
| COMPLETED | 已完成 |
| CANCELED | 已取消 |

### 3.7 提醒类型 `reminder_type`

| 值 | 说明 |
| --- | --- |
| WARRANTY_EXPIRE_SOON | 保修即将到期 |
| WARRANTY_EXPIRED | 保修已到期 |
| CONSUMABLE_REPLACE_SOON | 耗材即将更换 |
| CONSUMABLE_OVERDUE | 耗材已逾期 |
| MAINTENANCE_FOLLOW_UP | 维修待跟进 |

### 3.8 提醒状态 `reminder_status`

| 值 | 说明 |
| --- | --- |
| PENDING | 待提醒 |
| SENT | 已发送 |
| READ | 已读 |
| IGNORED | 已忽略 |
| FAILED | 发送失败 |

### 3.9 附件业务类型 `file_biz_type`

| 值 | 说明 |
| --- | --- |
| DEVICE | 设备附件 |
| WARRANTY | 保修附件 |
| MAINTENANCE | 维修附件 |
| CONSUMABLE | 耗材附件 |
| MANUAL | 说明书 |

### 3.10 AI 分析类型 `ai_analysis_type`

| 值 | 说明 |
| --- | --- |
| INVOICE_PARSE | 票据信息提取 |
| TROUBLESHOOTING | 故障排查建议 |
| MAINTENANCE_SUMMARY | 维修总结 |
| CARE_SUGGESTION | 保养建议 |

## 4. 表结构设计

## 4.1 sys_user 用户表

```sql
CREATE TABLE sys_user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(64) NOT NULL,
  email VARCHAR(128) DEFAULT NULL,
  phone VARCHAR(32) DEFAULT NULL,
  password_hash VARCHAR(255) NOT NULL,
  nickname VARCHAR(64) DEFAULT NULL,
  avatar_url VARCHAR(512) DEFAULT NULL,
  status VARCHAR(32) NOT NULL DEFAULT 'ENABLED',
  last_login_at DATETIME DEFAULT NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  created_by BIGINT DEFAULT NULL,
  updated_by BIGINT DEFAULT NULL,
  deleted TINYINT NOT NULL DEFAULT 0,
  UNIQUE KEY uk_sys_user_username (username),
  UNIQUE KEY uk_sys_user_email (email),
  KEY idx_sys_user_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

说明：

- 密码必须存储哈希值。
- 邮箱可选，但如果填写需要唯一。

## 4.2 sys_role 角色表（二期规划）

```sql
CREATE TABLE sys_role (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  role_code VARCHAR(64) NOT NULL,
  role_name VARCHAR(64) NOT NULL,
  description VARCHAR(255) DEFAULT NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  created_by BIGINT DEFAULT NULL,
  updated_by BIGINT DEFAULT NULL,
  deleted TINYINT NOT NULL DEFAULT 0,
  UNIQUE KEY uk_sys_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

## 4.3 sys_user_role 用户角色表（二期规划）

```sql
CREATE TABLE sys_user_role (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  role_id BIGINT NOT NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  created_by BIGINT DEFAULT NULL,
  updated_by BIGINT DEFAULT NULL,
  deleted TINYINT NOT NULL DEFAULT 0,
  UNIQUE KEY uk_sys_user_role (user_id, role_id),
  KEY idx_sys_user_role_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

## 4.4 sys_operation_log 操作日志表（二期规划）

```sql
CREATE TABLE sys_operation_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT DEFAULT NULL,
  family_id BIGINT DEFAULT NULL,
  module VARCHAR(64) NOT NULL,
  action VARCHAR(64) NOT NULL,
  biz_type VARCHAR(64) DEFAULT NULL,
  biz_id BIGINT DEFAULT NULL,
  request_method VARCHAR(16) DEFAULT NULL,
  request_uri VARCHAR(512) DEFAULT NULL,
  ip_address VARCHAR(64) DEFAULT NULL,
  success TINYINT NOT NULL DEFAULT 1,
  error_message VARCHAR(1024) DEFAULT NULL,
  created_at DATETIME NOT NULL,
  KEY idx_sys_operation_log_user_id (user_id),
  KEY idx_sys_operation_log_family_id (family_id),
  KEY idx_sys_operation_log_biz (biz_type, biz_id),
  KEY idx_sys_operation_log_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

## 4.5 fl_family_space 家庭空间表

```sql
CREATE TABLE fl_family_space (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(128) NOT NULL,
  description VARCHAR(512) DEFAULT NULL,
  owner_user_id BIGINT NOT NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  created_by BIGINT DEFAULT NULL,
  updated_by BIGINT DEFAULT NULL,
  deleted TINYINT NOT NULL DEFAULT 0,
  KEY idx_fl_family_space_owner (owner_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

## 4.6 fl_family_member 家庭成员表

```sql
CREATE TABLE fl_family_member (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  family_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  role VARCHAR(32) NOT NULL,
  joined_at DATETIME NOT NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  created_by BIGINT DEFAULT NULL,
  updated_by BIGINT DEFAULT NULL,
  deleted TINYINT NOT NULL DEFAULT 0,
  UNIQUE KEY uk_fl_family_member (family_id, user_id),
  KEY idx_fl_family_member_user_id (user_id),
  KEY idx_fl_family_member_role (family_id, role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

## 4.7 fl_device_category 设备分类表

```sql
CREATE TABLE fl_device_category (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  family_id BIGINT NOT NULL,
  name VARCHAR(64) NOT NULL,
  icon VARCHAR(128) DEFAULT NULL,
  sort_order INT NOT NULL DEFAULT 0,
  system_default TINYINT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  created_by BIGINT DEFAULT NULL,
  updated_by BIGINT DEFAULT NULL,
  deleted TINYINT NOT NULL DEFAULT 0,
  UNIQUE KEY uk_fl_device_category_name (family_id, name),
  KEY idx_fl_device_category_family (family_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

## 4.8 fl_device_asset 设备档案表

```sql
CREATE TABLE fl_device_asset (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  family_id BIGINT NOT NULL,
  category_id BIGINT DEFAULT NULL,
  name VARCHAR(128) NOT NULL,
  brand VARCHAR(128) DEFAULT NULL,
  model VARCHAR(128) DEFAULT NULL,
  serial_number VARCHAR(128) DEFAULT NULL,
  purchase_date DATE DEFAULT NULL,
  purchase_channel VARCHAR(128) DEFAULT NULL,
  purchase_price DECIMAL(12,2) DEFAULT NULL,
  location VARCHAR(128) DEFAULT NULL,
  status VARCHAR(32) NOT NULL DEFAULT 'NORMAL',
  cover_file_id BIGINT DEFAULT NULL,
  remark VARCHAR(1024) DEFAULT NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  created_by BIGINT DEFAULT NULL,
  updated_by BIGINT DEFAULT NULL,
  deleted TINYINT NOT NULL DEFAULT 0,
  KEY idx_fl_device_asset_family (family_id),
  KEY idx_fl_device_asset_category (category_id),
  KEY idx_fl_device_asset_status (family_id, status),
  KEY idx_fl_device_asset_brand (family_id, brand),
  KEY idx_fl_device_asset_purchase_date (purchase_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

## 4.9 fl_warranty_record 保修记录表

```sql
CREATE TABLE fl_warranty_record (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  family_id BIGINT NOT NULL,
  device_id BIGINT NOT NULL,
  warranty_type VARCHAR(32) NOT NULL DEFAULT 'OFFICIAL',
  start_date DATE NOT NULL,
  end_date DATE NOT NULL,
  remind_days_before INT NOT NULL DEFAULT 30,
  service_phone VARCHAR(64) DEFAULT NULL,
  service_address VARCHAR(255) DEFAULT NULL,
  service_note VARCHAR(1024) DEFAULT NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  created_by BIGINT DEFAULT NULL,
  updated_by BIGINT DEFAULT NULL,
  deleted TINYINT NOT NULL DEFAULT 0,
  KEY idx_fl_warranty_record_family (family_id),
  KEY idx_fl_warranty_record_device (device_id),
  KEY idx_fl_warranty_record_end_date (family_id, end_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

## 4.10 fl_consumable_item 耗材项表

```sql
CREATE TABLE fl_consumable_item (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  family_id BIGINT NOT NULL,
  device_id BIGINT NOT NULL,
  name VARCHAR(128) NOT NULL,
  brand VARCHAR(128) DEFAULT NULL,
  model VARCHAR(128) DEFAULT NULL,
  cycle_days INT NOT NULL,
  last_replaced_date DATE DEFAULT NULL,
  next_remind_date DATE DEFAULT NULL,
  remind_days_before INT NOT NULL DEFAULT 7,
  status VARCHAR(32) NOT NULL DEFAULT 'NORMAL',
  enabled TINYINT NOT NULL DEFAULT 1,
  remark VARCHAR(1024) DEFAULT NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  created_by BIGINT DEFAULT NULL,
  updated_by BIGINT DEFAULT NULL,
  deleted TINYINT NOT NULL DEFAULT 0,
  KEY idx_fl_consumable_item_family (family_id),
  KEY idx_fl_consumable_item_device (device_id),
  KEY idx_fl_consumable_item_next_remind (family_id, next_remind_date),
  KEY idx_fl_consumable_item_status (family_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

## 4.11 fl_consumable_replace_record 耗材更换记录表

```sql
CREATE TABLE fl_consumable_replace_record (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  family_id BIGINT NOT NULL,
  consumable_id BIGINT NOT NULL,
  device_id BIGINT NOT NULL,
  replaced_date DATE NOT NULL,
  cost DECIMAL(12,2) DEFAULT NULL,
  note VARCHAR(1024) DEFAULT NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  created_by BIGINT DEFAULT NULL,
  updated_by BIGINT DEFAULT NULL,
  deleted TINYINT NOT NULL DEFAULT 0,
  KEY idx_fl_consumable_replace_family (family_id),
  KEY idx_fl_consumable_replace_consumable (consumable_id),
  KEY idx_fl_consumable_replace_device (device_id),
  KEY idx_fl_consumable_replace_date (replaced_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

## 4.12 fl_maintenance_record 维修记录表

```sql
CREATE TABLE fl_maintenance_record (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  family_id BIGINT NOT NULL,
  device_id BIGINT NOT NULL,
  title VARCHAR(128) NOT NULL,
  fault_description VARCHAR(2048) NOT NULL,
  occurred_at DATETIME DEFAULT NULL,
  status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
  repair_channel VARCHAR(128) DEFAULT NULL,
  repair_contact VARCHAR(128) DEFAULT NULL,
  repair_cost DECIMAL(12,2) DEFAULT NULL,
  result_description VARCHAR(2048) DEFAULT NULL,
  completed_at DATETIME DEFAULT NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  created_by BIGINT DEFAULT NULL,
  updated_by BIGINT DEFAULT NULL,
  deleted TINYINT NOT NULL DEFAULT 0,
  KEY idx_fl_maintenance_family (family_id),
  KEY idx_fl_maintenance_device (device_id),
  KEY idx_fl_maintenance_status (family_id, status),
  KEY idx_fl_maintenance_occurred_at (occurred_at),
  KEY idx_fl_maintenance_completed_at (family_id, completed_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

## 4.13 fl_reminder_task 提醒任务表

```sql
CREATE TABLE fl_reminder_task (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  family_id BIGINT NOT NULL,
  user_id BIGINT DEFAULT NULL,
  reminder_type VARCHAR(64) NOT NULL,
  biz_type VARCHAR(64) NOT NULL,
  biz_id BIGINT NOT NULL,
  title VARCHAR(128) NOT NULL,
  content VARCHAR(1024) DEFAULT NULL,
  remind_at DATETIME NOT NULL,
  status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
  read_at DATETIME DEFAULT NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  created_by BIGINT DEFAULT NULL,
  updated_by BIGINT DEFAULT NULL,
  deleted TINYINT NOT NULL DEFAULT 0,
  KEY idx_fl_reminder_family_status (family_id, status),
  KEY idx_fl_reminder_user_status (user_id, status),
  KEY idx_fl_reminder_remind_at (remind_at),
  KEY idx_fl_reminder_biz (biz_type, biz_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

## 4.14 fl_notification_record 通知记录表

```sql
CREATE TABLE fl_notification_record (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  family_id BIGINT NOT NULL,
  user_id BIGINT DEFAULT NULL,
  reminder_id BIGINT DEFAULT NULL,
  channel VARCHAR(32) NOT NULL,
  title VARCHAR(128) NOT NULL,
  content VARCHAR(1024) DEFAULT NULL,
  status VARCHAR(32) NOT NULL,
  error_message VARCHAR(1024) DEFAULT NULL,
  sent_at DATETIME DEFAULT NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  created_by BIGINT DEFAULT NULL,
  updated_by BIGINT DEFAULT NULL,
  deleted TINYINT NOT NULL DEFAULT 0,
  KEY idx_fl_notification_family (family_id),
  KEY idx_fl_notification_user (user_id),
  KEY idx_fl_notification_status (status),
  KEY idx_fl_notification_sent_at (sent_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

## 4.15 fl_file_resource 文件资源表

```sql
CREATE TABLE fl_file_resource (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  family_id BIGINT NOT NULL,
  biz_type VARCHAR(64) NOT NULL,
  biz_id BIGINT NOT NULL,
  original_name VARCHAR(255) NOT NULL,
  storage_name VARCHAR(255) NOT NULL,
  storage_path VARCHAR(1024) NOT NULL,
  content_type VARCHAR(128) NOT NULL,
  file_size BIGINT NOT NULL,
  extension VARCHAR(32) DEFAULT NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  created_by BIGINT DEFAULT NULL,
  updated_by BIGINT DEFAULT NULL,
  deleted TINYINT NOT NULL DEFAULT 0,
  KEY idx_fl_file_resource_family (family_id),
  KEY idx_fl_file_resource_biz (family_id, biz_type, biz_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

## 4.16 fl_ai_analysis AI 分析结果表

```sql
CREATE TABLE fl_ai_analysis (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  family_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  analysis_type VARCHAR(64) NOT NULL,
  biz_type VARCHAR(64) DEFAULT NULL,
  biz_id BIGINT DEFAULT NULL,
  provider VARCHAR(64) NOT NULL DEFAULT 'mock',
  model VARCHAR(128) DEFAULT NULL,
  input_summary VARCHAR(1024) DEFAULT NULL,
  result_json TEXT DEFAULT NULL,
  status VARCHAR(32) NOT NULL,
  error_message VARCHAR(1024) DEFAULT NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  created_by BIGINT DEFAULT NULL,
  updated_by BIGINT DEFAULT NULL,
  deleted TINYINT NOT NULL DEFAULT 0,
  KEY idx_fl_ai_analysis_family (family_id),
  KEY idx_fl_ai_analysis_user (user_id),
  KEY idx_fl_ai_analysis_biz (biz_type, biz_id),
  KEY idx_fl_ai_analysis_type (analysis_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

## 5. 表关系

```mermaid
erDiagram
    SYS_USER ||--o{ FL_FAMILY_MEMBER : joins
    FL_FAMILY_SPACE ||--o{ FL_FAMILY_MEMBER : has
    FL_FAMILY_SPACE ||--o{ FL_DEVICE_CATEGORY : owns
    FL_FAMILY_SPACE ||--o{ FL_DEVICE_ASSET : owns
    FL_DEVICE_CATEGORY ||--o{ FL_DEVICE_ASSET : classifies
    FL_DEVICE_ASSET ||--o{ FL_WARRANTY_RECORD : has
    FL_DEVICE_ASSET ||--o{ FL_CONSUMABLE_ITEM : has
    FL_CONSUMABLE_ITEM ||--o{ FL_CONSUMABLE_REPLACE_RECORD : has
    FL_DEVICE_ASSET ||--o{ FL_MAINTENANCE_RECORD : has
    FL_FAMILY_SPACE ||--o{ FL_REMINDER_TASK : has
    FL_FAMILY_SPACE ||--o{ FL_FILE_RESOURCE : has
    FL_FAMILY_SPACE ||--o{ FL_AI_ANALYSIS : has
```

## 6. 索引设计说明

### 6.1 家庭空间隔离索引

所有需要家庭隔离的业务表都要有 `family_id` 索引：

- `fl_device_asset.family_id`
- `fl_warranty_record.family_id`
- `fl_consumable_item.family_id`
- `fl_maintenance_record.family_id`
- `fl_reminder_task.family_id`
- `fl_file_resource.family_id`

### 6.2 常用筛选索引

- 设备列表：`family_id + status`、`family_id + brand`、`category_id`。
- 保修提醒：`family_id + end_date`。
- 耗材提醒：`family_id + next_remind_date`。
- 维修列表：`family_id + status`、`device_id`。
- 提醒列表：`family_id + status`、`user_id + status`。

## 7. 数据隔离规则

- 当前用户只能访问自己所属家庭空间的数据。
- 查询业务数据时必须带 `family_id` 条件。
- 更新或删除数据前必须校验数据所属 `family_id`。
- 附件下载必须校验 `family_id`。
- AI 分析结果也必须归属家庭空间。

## 8. 逻辑删除规则

使用 `deleted` 字段。

需要逻辑删除的表：

- 设备。
- 保修记录。
- 耗材项。
- 耗材更换记录。
- 维修记录。
- 附件资源。
- AI 分析结果。

不建议物理删除核心业务数据，因为设备生命周期记录需要可追溯。

## 9. 初始化数据

### 9.1 系统角色（二期规划）

当前版本暂未创建 `sys_role` 和 `sys_user_role`，系统角色作为 RBAC 二期规划。

| role_code | role_name |
| --- | --- |
| ADMIN | 系统管理员 |
| USER | 普通用户 |

### 9.2 默认设备分类

规划中每个家庭空间创建后初始化；当前演示数据已为 `family_id=1` 初始化厨房设备、清洁设备和数码设备，新家庭自动初始化默认分类待增强：

- 数码设备。
- 大家电。
- 小家电。
- 网络设备。
- 厨房设备。
- 清洁设备。
- 家居设备。
- 其他。


## 10. P7 演示数据

P7 增加 `backend/src/main/resources/db/demo-data.sql` 和 `backend/src/main/resources/db/schema.sql`，用于本地 Docker 或开发环境演示。默认不会在普通开发启动时执行，只有设置以下环境变量时才会加载：

```dotenv
SQL_INIT_MODE=always
SQL_DATA_LOCATIONS=classpath:db/demo-data.sql
```

演示数据包含：

| 数据 | 说明 |
| --- | --- |
| 默认用户 | `demo / fixledger123` |
| 默认家庭空间 | `演示家庭`，`family_id=1` |
| 设备分类 | 厨房设备、清洁设备、数码设备 |
| 示例设备 | 小米净水器、戴森吸尘器、华硕路由器 |
| 保修记录 | 覆盖正常保修和即将到期保修 |
| 耗材记录 | 覆盖滤芯、滤网和更换历史 |
| 维修记录 | 覆盖已完成维修和维修中记录 |
| 提醒与通知 | 覆盖耗材即将更换和保修即将到期 |
| AI 分析 | 覆盖 Mock 故障排查建议留痕；接口测试可能额外产生票据解析记录 |

演示 SQL 使用固定主键和 `ON DUPLICATE KEY UPDATE`，方便重复执行；真实生产环境不应启用演示数据初始化。
## 11. 后续扩展表

### 11.1 fl_device_qrcode 设备二维码表

用于生成设备标签二维码。

### 11.2 fl_manual_text_index 说明书文本索引表

用于说明书 PDF 文本解析和搜索。

### 11.3 fl_webhook_config Webhook 配置表

用于扩展外部通知。

### 11.4 fl_export_record 导出记录表

用于导出家庭设备资产清单和维修费用报表。
## 12. P9.1 当前表结构对齐说明

截至 P9.1，当前实际初始化脚本位于 `backend/src/main/resources/db/schema.sql`，已建表如下：

- `sys_user`
- `fl_family_space`
- `fl_family_member`
- `fl_device_category`
- `fl_device_asset`
- `fl_warranty_record`
- `fl_file_resource`
- `fl_consumable_item`
- `fl_consumable_replace_record`
- `fl_maintenance_record`
- `fl_reminder_task`
- `fl_notification_record`
- `fl_ai_analysis`

当前暂未实现的规划表：

- `sys_role`
- `sys_user_role`
- `sys_operation_log`

当前演示数据库查询结果显示，演示环境已经包含用户、家庭、设备分类、设备、保修、耗材、更换记录、维修记录、提醒、通知、附件和 AI 分析数据，能够支撑面试演示核心闭环。
