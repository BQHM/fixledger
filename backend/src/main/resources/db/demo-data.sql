INSERT INTO sys_user (
  id,
  username,
  email,
  password_hash,
  nickname,
  status,
  created_at,
  updated_at,
  deleted
) VALUES (
  1,
  'demo',
  'demo@fixledger.local',
  '$2a$10$2qH93bnvqeBO3IriwFPY2O.yR.FTz2XVpwfuX5fLl05bVUBVyHRUq',
  '演示用户',
  'ENABLED',
  '2026-05-01 09:00:00',
  '2026-05-01 09:00:00',
  0
) ON DUPLICATE KEY UPDATE
  email = VALUES(email),
  nickname = VALUES(nickname),
  status = VALUES(status),
  updated_at = VALUES(updated_at),
  deleted = VALUES(deleted);

INSERT INTO fl_family_space (
  id,
  name,
  description,
  owner_user_id,
  created_at,
  updated_at,
  deleted
) VALUES (
  1,
  '演示家庭',
  '用于面试演示的家庭空间，覆盖设备、保修、耗材、维修和提醒闭环。',
  1,
  '2026-05-01 09:01:00',
  '2026-05-01 09:01:00',
  0
) ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  description = VALUES(description),
  owner_user_id = VALUES(owner_user_id),
  updated_at = VALUES(updated_at),
  deleted = VALUES(deleted);

INSERT INTO fl_family_member (
  id,
  family_id,
  user_id,
  role,
  joined_at,
  created_at,
  updated_at,
  deleted
) VALUES (
  1,
  1,
  1,
  'OWNER',
  '2026-05-01 09:01:00',
  '2026-05-01 09:01:00',
  '2026-05-01 09:01:00',
  0
) ON DUPLICATE KEY UPDATE
  role = VALUES(role),
  updated_at = VALUES(updated_at),
  deleted = VALUES(deleted);

INSERT INTO fl_device_category (
  id,
  family_id,
  name,
  icon,
  sort_order,
  system_default,
  created_at,
  updated_at,
  deleted
) VALUES
  (1, 1, '厨房设备', 'Kitchen', 10, 1, '2026-05-01 09:02:00', '2026-05-01 09:02:00', 0),
  (2, 1, '清洁设备', 'Brush', 20, 1, '2026-05-01 09:02:00', '2026-05-01 09:02:00', 0),
  (3, 1, '数码设备', 'Monitor', 30, 1, '2026-05-01 09:02:00', '2026-05-01 09:02:00', 0)
ON DUPLICATE KEY UPDATE
  icon = VALUES(icon),
  sort_order = VALUES(sort_order),
  system_default = VALUES(system_default),
  updated_at = VALUES(updated_at),
  deleted = VALUES(deleted);

INSERT INTO fl_device_asset (
  id,
  family_id,
  category_id,
  name,
  brand,
  model,
  serial_number,
  purchase_date,
  purchase_channel,
  purchase_price,
  location,
  status,
  remark,
  created_at,
  updated_at,
  deleted
) VALUES
  (
    1,
    1,
    1,
    '小米净水器 S1',
    '小米',
    'S1 800G',
    'DEMO-WATER-001',
    '2026-02-11',
    '京东自营',
    1999.00,
    '厨房',
    'NORMAL',
    '演示保修、耗材滤芯和维修记录的核心设备。',
    '2026-05-01 09:03:00',
    '2026-05-01 09:03:00',
    0
  ),
  (
    2,
    1,
    2,
    '戴森吸尘器 V12',
    'Dyson',
    'V12 Detect Slim',
    'DEMO-CLEAN-002',
    '2025-11-20',
    '天猫旗舰店',
    3999.00,
    '客厅',
    'NORMAL',
    '演示清洁设备保修和附件归档。',
    '2026-05-01 09:04:00',
    '2026-05-01 09:04:00',
    0
  ),
  (
    3,
    1,
    3,
    '华硕路由器 AX86U',
    'ASUS',
    'RT-AX86U',
    'DEMO-NET-003',
    '2024-07-15',
    '苏宁易购',
    1099.00,
    '书房',
    'REPAIRING',
    '演示维修中设备和费用趋势。',
    '2026-05-01 09:05:00',
    '2026-05-01 09:05:00',
    0
  )
ON DUPLICATE KEY UPDATE
  category_id = VALUES(category_id),
  name = VALUES(name),
  brand = VALUES(brand),
  model = VALUES(model),
  serial_number = VALUES(serial_number),
  purchase_date = VALUES(purchase_date),
  purchase_channel = VALUES(purchase_channel),
  purchase_price = VALUES(purchase_price),
  location = VALUES(location),
  status = VALUES(status),
  remark = VALUES(remark),
  updated_at = VALUES(updated_at),
  deleted = VALUES(deleted);

INSERT INTO fl_warranty_record (
  id,
  family_id,
  device_id,
  warranty_type,
  start_date,
  end_date,
  remind_days_before,
  service_phone,
  service_address,
  service_note,
  created_at,
  updated_at,
  deleted
) VALUES
  (
    1,
    1,
    1,
    'OFFICIAL',
    '2026-02-11',
    '2027-02-11',
    30,
    '400-100-5678',
    '小米官方售后',
    '整机一年保修，滤芯属于耗材。',
    '2026-05-01 09:06:00',
    '2026-05-01 09:06:00',
    0
  ),
  (
    2,
    1,
    2,
    'OFFICIAL',
    '2025-11-20',
    '2026-11-20',
    30,
    '400-920-2808',
    'Dyson 官方售后',
    '保修卡和发票已归档。',
    '2026-05-01 09:07:00',
    '2026-05-01 09:07:00',
    0
  ),
  (
    3,
    1,
    3,
    'OFFICIAL',
    '2024-07-15',
    '2026-07-15',
    45,
    '400-600-6655',
    'ASUS 官方售后',
    '网络设备保修即将到期，用于演示提醒扫描。',
    '2026-05-01 09:08:00',
    '2026-05-01 09:08:00',
    0
  )
ON DUPLICATE KEY UPDATE
  warranty_type = VALUES(warranty_type),
  start_date = VALUES(start_date),
  end_date = VALUES(end_date),
  remind_days_before = VALUES(remind_days_before),
  service_phone = VALUES(service_phone),
  service_address = VALUES(service_address),
  service_note = VALUES(service_note),
  updated_at = VALUES(updated_at),
  deleted = VALUES(deleted);

INSERT INTO fl_consumable_item (
  id,
  family_id,
  device_id,
  name,
  brand,
  model,
  cycle_days,
  last_replaced_date,
  next_remind_date,
  remind_days_before,
  status,
  enabled,
  remark,
  created_at,
  updated_at,
  deleted
) VALUES
  (
    1,
    1,
    1,
    'PP 棉滤芯',
    '小米',
    'PPC-01',
    180,
    '2026-01-20',
    '2026-07-19',
    15,
    'NORMAL',
    1,
    '净水器一级滤芯，演示耗材提醒。',
    '2026-05-01 09:09:00',
    '2026-05-01 09:09:00',
    0
  ),
  (
    2,
    1,
    2,
    'HEPA 滤网',
    'Dyson',
    'HEPA-V12',
    365,
    '2025-06-01',
    '2026-06-01',
    30,
    'DUE_SOON',
    1,
    '清洁设备滤网，演示即将更换。',
    '2026-05-01 09:10:00',
    '2026-05-01 09:10:00',
    0
  )
ON DUPLICATE KEY UPDATE
  brand = VALUES(brand),
  model = VALUES(model),
  cycle_days = VALUES(cycle_days),
  last_replaced_date = VALUES(last_replaced_date),
  next_remind_date = VALUES(next_remind_date),
  remind_days_before = VALUES(remind_days_before),
  status = VALUES(status),
  enabled = VALUES(enabled),
  remark = VALUES(remark),
  updated_at = VALUES(updated_at),
  deleted = VALUES(deleted);

INSERT INTO fl_consumable_replace_record (
  id,
  family_id,
  consumable_id,
  device_id,
  replaced_date,
  cost,
  note,
  created_at,
  updated_at,
  deleted
) VALUES
  (
    1,
    1,
    1,
    1,
    '2026-01-20',
    129.00,
    '春节前更换 PP 棉滤芯，水流恢复正常。',
    '2026-05-01 09:11:00',
    '2026-05-01 09:11:00',
    0
  ),
  (
    2,
    1,
    2,
    2,
    '2025-06-01',
    299.00,
    '更换 HEPA 滤网并清洁尘杯。',
    '2026-05-01 09:12:00',
    '2026-05-01 09:12:00',
    0
  )
ON DUPLICATE KEY UPDATE
  replaced_date = VALUES(replaced_date),
  cost = VALUES(cost),
  note = VALUES(note),
  updated_at = VALUES(updated_at),
  deleted = VALUES(deleted);

INSERT INTO fl_maintenance_record (
  id,
  family_id,
  device_id,
  title,
  fault_description,
  occurred_at,
  status,
  repair_channel,
  repair_contact,
  repair_cost,
  result_description,
  completed_at,
  created_at,
  updated_at,
  deleted
) VALUES
  (
    1,
    1,
    1,
    '出水速度变慢',
    '净水器出水速度明显下降，伴随滤芯寿命提示。',
    '2026-03-10 20:30:00',
    'COMPLETED',
    '官方售后',
    '400-100-5678',
    129.00,
    '更换 PP 棉滤芯后恢复正常。',
    '2026-03-12 18:00:00',
    '2026-05-01 09:13:00',
    '2026-05-01 09:13:00',
    0
  ),
  (
    2,
    1,
    3,
    '无线网络频繁断连',
    '路由器在晚高峰频繁断网，重启后短暂恢复。',
    '2026-04-28 22:10:00',
    'REPAIRING',
    '官方售后',
    '400-600-6655',
    NULL,
    '已寄修，等待售后检测。',
    NULL,
    '2026-05-01 09:14:00',
    '2026-05-01 09:14:00',
    0
  )
ON DUPLICATE KEY UPDATE
  title = VALUES(title),
  fault_description = VALUES(fault_description),
  occurred_at = VALUES(occurred_at),
  status = VALUES(status),
  repair_channel = VALUES(repair_channel),
  repair_contact = VALUES(repair_contact),
  repair_cost = VALUES(repair_cost),
  result_description = VALUES(result_description),
  completed_at = VALUES(completed_at),
  updated_at = VALUES(updated_at),
  deleted = VALUES(deleted);

INSERT INTO fl_file_resource (
  id,
  family_id,
  biz_type,
  biz_id,
  original_name,
  storage_name,
  storage_path,
  content_type,
  file_size,
  extension,
  created_at,
  updated_at,
  deleted
) VALUES
  (
    1,
    1,
    'DEVICE',
    1,
    'xiaomi-water-invoice.pdf',
    'demo-xiaomi-water-invoice.pdf',
    'demo/device/xiaomi-water-invoice.pdf',
    'application/pdf',
    204800,
    'pdf',
    '2026-05-01 09:15:00',
    '2026-05-01 09:15:00',
    0
  ),
  (
    2,
    1,
    'WARRANTY',
    2,
    'dyson-warranty-card.jpg',
    'demo-dyson-warranty-card.jpg',
    'demo/warranty/dyson-warranty-card.jpg',
    'image/jpeg',
    512000,
    'jpg',
    '2026-05-01 09:16:00',
    '2026-05-01 09:16:00',
    0
  )
ON DUPLICATE KEY UPDATE
  original_name = VALUES(original_name),
  storage_name = VALUES(storage_name),
  storage_path = VALUES(storage_path),
  content_type = VALUES(content_type),
  file_size = VALUES(file_size),
  extension = VALUES(extension),
  updated_at = VALUES(updated_at),
  deleted = VALUES(deleted);

INSERT INTO fl_reminder_task (
  id,
  family_id,
  user_id,
  reminder_type,
  biz_type,
  biz_id,
  title,
  content,
  remind_at,
  status,
  created_at,
  updated_at,
  deleted
) VALUES
  (
    1,
    1,
    1,
    'CONSUMABLE_REPLACE_SOON',
    'CONSUMABLE',
    2,
    'HEPA 滤网耗材即将更换',
    'HEPA 滤网的下次提醒日期为 2026-06-01',
    '2026-05-11 08:00:00',
    'PENDING',
    '2026-05-01 09:17:00',
    '2026-05-01 09:17:00',
    0
  ),
  (
    2,
    1,
    1,
    'WARRANTY_EXPIRE_SOON',
    'WARRANTY',
    3,
    '华硕路由器 AX86U保修即将到期',
    '华硕路由器 AX86U 的保修结束日期为 2026-07-15',
    '2026-05-31 08:00:00',
    'PENDING',
    '2026-05-01 09:18:00',
    '2026-05-01 09:18:00',
    0
  )
ON DUPLICATE KEY UPDATE
  reminder_type = VALUES(reminder_type),
  biz_type = VALUES(biz_type),
  biz_id = VALUES(biz_id),
  title = VALUES(title),
  content = VALUES(content),
  remind_at = VALUES(remind_at),
  status = VALUES(status),
  read_at = NULL,
  updated_at = VALUES(updated_at),
  deleted = VALUES(deleted);

INSERT INTO fl_notification_record (
  id,
  family_id,
  user_id,
  reminder_id,
  channel,
  title,
  content,
  status,
  sent_at,
  created_at,
  updated_at,
  deleted
) VALUES
  (
    1,
    1,
    1,
    1,
    'IN_APP',
    'HEPA 滤网耗材即将更换',
    'HEPA 滤网的下次提醒日期为 2026-06-01',
    'SENT',
    '2026-05-01 09:17:00',
    '2026-05-01 09:17:00',
    '2026-05-01 09:17:00',
    0
  ),
  (
    2,
    1,
    1,
    2,
    'IN_APP',
    '华硕路由器 AX86U保修即将到期',
    '华硕路由器 AX86U 的保修结束日期为 2026-07-15',
    'SENT',
    '2026-05-01 09:18:00',
    '2026-05-01 09:18:00',
    '2026-05-01 09:18:00',
    0
  )
ON DUPLICATE KEY UPDATE
  title = VALUES(title),
  content = VALUES(content),
  status = VALUES(status),
  sent_at = VALUES(sent_at),
  updated_at = VALUES(updated_at),
  deleted = VALUES(deleted);

INSERT INTO fl_ai_analysis (
  id,
  family_id,
  user_id,
  analysis_type,
  biz_type,
  biz_id,
  provider,
  model,
  input_summary,
  result_json,
  status,
  created_at,
  updated_at,
  deleted
) VALUES
  (
    1,
    1,
    1,
    'TROUBLESHOOTING',
    'MAINTENANCE',
    2,
    'mock',
    'mock-rule-engine',
    '路由器在晚高峰频繁断网，重启后短暂恢复。',
    '{"analysisId":null,"summary":"华硕路由器 AX86U 出现断网问题，建议先从网络链路和固件排查。","suggestions":["重启路由器和光猫，确认网线、宽带账号和上级网络状态。","检查设备固件版本和信号覆盖，必要时调整摆放位置。"]}',
    'SUCCESS',
    '2026-05-01 09:19:00',
    '2026-05-01 09:19:00',
    0
  )
ON DUPLICATE KEY UPDATE
  provider = VALUES(provider),
  model = VALUES(model),
  input_summary = VALUES(input_summary),
  result_json = VALUES(result_json),
  status = VALUES(status),
  updated_at = VALUES(updated_at),
  deleted = VALUES(deleted);