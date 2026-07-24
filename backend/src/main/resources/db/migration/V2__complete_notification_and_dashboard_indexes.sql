-- Existing non-empty databases are baselined at version 0. V1 creates missing tables,
-- while this migration adds columns and indexes that CREATE TABLE IF NOT EXISTS cannot retrofit.

SET @notification_recipient_exists = (
  SELECT COUNT(*)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'fl_notification_record'
    AND column_name = 'recipient'
);
SET @notification_recipient_sql = IF(
  @notification_recipient_exists = 0,
  'ALTER TABLE fl_notification_record ADD COLUMN recipient VARCHAR(512) DEFAULT NULL',
  'SELECT 1'
);
PREPARE notification_recipient_statement FROM @notification_recipient_sql;
EXECUTE notification_recipient_statement;
DEALLOCATE PREPARE notification_recipient_statement;

SET @notification_attempt_count_exists = (
  SELECT COUNT(*)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'fl_notification_record'
    AND column_name = 'attempt_count'
);
SET @notification_attempt_count_sql = IF(
  @notification_attempt_count_exists = 0,
  CONCAT(
    'ALTER TABLE fl_notification_record ADD COLUMN attempt_count INT NOT NULL ',
    'DEFAULT 0'
  ),
  'SELECT 1'
);
PREPARE notification_attempt_count_statement FROM @notification_attempt_count_sql;
EXECUTE notification_attempt_count_statement;
DEALLOCATE PREPARE notification_attempt_count_statement;

SET @notification_next_retry_at_exists = (
  SELECT COUNT(*)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'fl_notification_record'
    AND column_name = 'next_retry_at'
);
SET @notification_next_retry_at_sql = IF(
  @notification_next_retry_at_exists = 0,
  'ALTER TABLE fl_notification_record ADD COLUMN next_retry_at DATETIME DEFAULT NULL',
  'SELECT 1'
);
PREPARE notification_next_retry_at_statement FROM @notification_next_retry_at_sql;
EXECUTE notification_next_retry_at_statement;
DEALLOCATE PREPARE notification_next_retry_at_statement;

SET @notification_last_attempt_at_exists = (
  SELECT COUNT(*)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'fl_notification_record'
    AND column_name = 'last_attempt_at'
);
SET @notification_last_attempt_at_sql = IF(
  @notification_last_attempt_at_exists = 0,
  'ALTER TABLE fl_notification_record ADD COLUMN last_attempt_at DATETIME DEFAULT NULL',
  'SELECT 1'
);
PREPARE notification_last_attempt_at_statement FROM @notification_last_attempt_at_sql;
EXECUTE notification_last_attempt_at_statement;
DEALLOCATE PREPARE notification_last_attempt_at_statement;

SET @device_list_index_exists = (
  SELECT COUNT(*)
  FROM information_schema.statistics
  WHERE table_schema = DATABASE()
    AND table_name = 'fl_device_asset'
    AND index_name = 'idx_fl_device_asset_list'
);
SET @device_list_index_sql = IF(
  @device_list_index_exists = 0,
  'ALTER TABLE fl_device_asset ADD INDEX idx_fl_device_asset_list (family_id, updated_at)',
  'SELECT 1'
);
PREPARE device_list_index_statement FROM @device_list_index_sql;
EXECUTE device_list_index_statement;
DEALLOCATE PREPARE device_list_index_statement;

SET @notification_dispatch_index_exists = (
  SELECT COUNT(*)
  FROM information_schema.statistics
  WHERE table_schema = DATABASE()
    AND table_name = 'fl_notification_record'
    AND index_name = 'idx_fl_notification_dispatch'
);
SET @notification_dispatch_index_sql = IF(
  @notification_dispatch_index_exists = 0,
  CONCAT(
    'ALTER TABLE fl_notification_record ADD INDEX idx_fl_notification_dispatch ',
    '(status, channel, next_retry_at, created_at)'
  ),
  'SELECT 1'
);
PREPARE notification_dispatch_index_statement FROM @notification_dispatch_index_sql;
EXECUTE notification_dispatch_index_statement;
DEALLOCATE PREPARE notification_dispatch_index_statement;

SET @notification_processing_index_exists = (
  SELECT COUNT(*)
  FROM information_schema.statistics
  WHERE table_schema = DATABASE()
    AND table_name = 'fl_notification_record'
    AND index_name = 'idx_fl_notification_processing'
);
SET @notification_processing_index_sql = IF(
  @notification_processing_index_exists = 0,
  CONCAT(
    'ALTER TABLE fl_notification_record ADD INDEX idx_fl_notification_processing ',
    '(status, last_attempt_at)'
  ),
  'SELECT 1'
);
PREPARE notification_processing_index_statement FROM @notification_processing_index_sql;
EXECUTE notification_processing_index_statement;
DEALLOCATE PREPARE notification_processing_index_statement;
