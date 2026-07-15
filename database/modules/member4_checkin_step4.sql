USE zhisu_hotel;
SET NAMES utf8mb4;

-- Member 4 step 4: confirm check-in / room assignment safe upgrades.
-- Idempotent, non-destructive, and does not add legacy hotel_order foreign keys.

SET @sql := (
    SELECT IF(COUNT(*) > 0,
        'ALTER TABLE room_assignment DROP FOREIGN KEY fk_room_assignment_order',
        'SELECT ''legacy fk_room_assignment_order not present''')
    FROM information_schema.key_column_usage
    WHERE table_schema = DATABASE()
      AND table_name = 'room_assignment'
      AND constraint_name = 'fk_room_assignment_order'
      AND referenced_table_name = 'hotel_order'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql := (
    SELECT IF(COUNT(*) = 0,
        'ALTER TABLE room_assignment ADD COLUMN request_token VARCHAR(128) NULL AFTER check_out_time',
        'SELECT ''room_assignment.request_token already exists''')
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'room_assignment'
      AND column_name = 'request_token'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql := (
    SELECT IF(COUNT(*) = 0,
        'ALTER TABLE room_assignment ADD COLUMN version INT NOT NULL DEFAULT 0 AFTER request_token',
        'SELECT ''room_assignment.version already exists''')
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'room_assignment'
      AND column_name = 'version'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql := (
    SELECT IF(COUNT(*) = 0,
        'ALTER TABLE room_assignment ADD COLUMN is_deleted TINYINT NOT NULL DEFAULT 0 AFTER version',
        'SELECT ''room_assignment.is_deleted already exists''')
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'room_assignment'
      AND column_name = 'is_deleted'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql := (
    SELECT IF(COUNT(*) = 0,
        'ALTER TABLE room_assignment ADD INDEX idx_room_assignment_order_status (order_id, assignment_status)',
        'SELECT ''idx_room_assignment_order_status already exists''')
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'room_assignment'
      AND index_name = 'idx_room_assignment_order_status'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql := (
    SELECT IF(COUNT(*) = 0,
        'ALTER TABLE room_assignment ADD INDEX idx_room_assignment_room_status (room_id, assignment_status)',
        'SELECT ''idx_room_assignment_room_status already exists''')
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'room_assignment'
      AND index_name = 'idx_room_assignment_room_status'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql := (
    SELECT IF(COUNT(*) = 0,
        'ALTER TABLE room_assignment ADD UNIQUE KEY uk_room_assignment_order_room (order_id, room_id)',
        'SELECT ''uk_room_assignment_order_room already exists''')
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'room_assignment'
      AND index_name = 'uk_room_assignment_order_room'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql := (
    SELECT IF(COUNT(*) = 0,
        'ALTER TABLE room_assignment ADD INDEX idx_room_assignment_request_token (request_token)',
        'SELECT ''idx_room_assignment_request_token already exists''')
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'room_assignment'
      AND index_name = 'idx_room_assignment_request_token'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
