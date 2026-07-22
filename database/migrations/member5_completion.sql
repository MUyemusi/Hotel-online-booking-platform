USE zhisu_hotel;
SET NAMES utf8mb4;
SET @db_name = DATABASE();

CREATE TABLE IF NOT EXISTS price_rule (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    rule_name VARCHAR(100) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    room_type_id BIGINT DEFAULT NULL,
    coefficient DECIMAL(5,2) NOT NULL,
    priority INT NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'ENABLED',
    remark VARCHAR(255) DEFAULT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_price_rule_dates_status (start_date, end_date, status),
    INDEX idx_price_rule_room_priority (room_type_id, priority)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SET @sql = IF(NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema=@db_name AND table_name='hotel_info' AND column_name='logo_url'), 'ALTER TABLE hotel_info ADD COLUMN logo_url VARCHAR(500) DEFAULT NULL AFTER hotel_name', 'SELECT 1'); PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @sql = IF(NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema=@db_name AND table_name='hotel_info' AND column_name='introduction'), 'ALTER TABLE hotel_info ADD COLUMN introduction TEXT AFTER logo_url', 'SELECT 1'); PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @sql = IF(NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema=@db_name AND table_name='hotel_info' AND column_name='contact_phone'), 'ALTER TABLE hotel_info ADD COLUMN contact_phone VARCHAR(30) DEFAULT NULL AFTER address', 'SELECT 1'); PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @sql = IF(NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema=@db_name AND table_name='hotel_info' AND column_name='facilities'), 'ALTER TABLE hotel_info ADD COLUMN facilities TEXT AFTER contact_phone', 'SELECT 1'); PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @sql = IF(NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema=@db_name AND table_name='hotel_info' AND column_name='checkin_notice'), 'ALTER TABLE hotel_info ADD COLUMN checkin_notice TEXT AFTER facilities', 'SELECT 1'); PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @sql = IF(NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema=@db_name AND table_name='hotel_info' AND column_name='cancellation_policy'), 'ALTER TABLE hotel_info ADD COLUMN cancellation_policy TEXT AFTER checkin_notice', 'SELECT 1'); PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @sql = IF(NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema=@db_name AND table_name='hotel_info' AND column_name='traffic_info'), 'ALTER TABLE hotel_info ADD COLUMN traffic_info TEXT AFTER cancellation_policy', 'SELECT 1'); PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @sql = IF(NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema=@db_name AND table_name='hotel_info' AND column_name='faq_content'), 'ALTER TABLE hotel_info ADD COLUMN faq_content TEXT AFTER traffic_info', 'SELECT 1'); PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @sql = IF(NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema=@db_name AND table_name='hotel_info' AND column_name='status'), 'ALTER TABLE hotel_info ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT ''PUBLISHED'' AFTER faq_content', 'SELECT 1'); PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @has_phone = EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema=@db_name AND table_name='hotel_info' AND column_name='phone');
SET @sql = IF(@has_phone, 'UPDATE hotel_info SET contact_phone=COALESCE(contact_phone, phone)', 'SELECT 1'); PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @has_description = EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema=@db_name AND table_name='hotel_info' AND column_name='description');
SET @sql = IF(@has_description, 'UPDATE hotel_info SET introduction=COALESCE(introduction, description)', 'SELECT 1'); PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

INSERT INTO system_config (config_key, config_value, config_name, value_type, config_group, description, status) VALUES
('INVENTORY_WARNING_VALUE','2','Inventory warning value','INTEGER','INVENTORY','Inventory warning threshold','ENABLED'),
('IMAGE_UPLOAD_MAX_MB','5','Image upload limit MB','INTEGER','CONTENT','Maximum image upload size','ENABLED'),
('DEFAULT_HOLIDAY_COEFFICIENT','1.20','Default holiday coefficient','DECIMAL','PRICING','Default holiday price coefficient','ENABLED')
ON DUPLICATE KEY UPDATE config_name=VALUES(config_name), value_type=VALUES(value_type), config_group=VALUES(config_group), description=VALUES(description);
