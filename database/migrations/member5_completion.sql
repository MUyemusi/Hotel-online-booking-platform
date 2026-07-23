

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

SET @sql = IF(NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema=@db_name AND table_name='booking_order' AND column_name='stay_type'), 'ALTER TABLE booking_order ADD COLUMN stay_type VARCHAR(30) NOT NULL DEFAULT ''STANDARD'' AFTER check_out_date', 'SELECT 1'); PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @has_phone = EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema=@db_name AND table_name='hotel_info' AND column_name='phone');
SET @sql = IF(@has_phone, 'UPDATE hotel_info SET contact_phone=COALESCE(contact_phone, phone)', 'SELECT 1'); PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @has_description = EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema=@db_name AND table_name='hotel_info' AND column_name='description');
SET @sql = IF(@has_description, 'UPDATE hotel_info SET introduction=COALESCE(introduction, description)', 'SELECT 1'); PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

INSERT INTO system_config (config_key, config_value, config_name, value_type, config_group, description, status) VALUES
('INVENTORY_WARNING_VALUE','2','库存预警值','INTEGER','INVENTORY','可售库存小于等于该值时触发预警','ENABLED'),
('IMAGE_UPLOAD_MAX_MB','5','图片上传大小限制','INTEGER','CONTENT','单张图片允许上传的最大MB数','ENABLED'),
('DEFAULT_HOLIDAY_COEFFICIENT','1.20','默认节假日价格系数','DECIMAL','PRICING','新增节假日规则时使用的默认系数','ENABLED'),
('EARLY_MORNING_END_TIME','06:00','凌晨入住截止时间','TIME','BOOKING','凌晨入住模式的每日截止时间','ENABLED')
ON DUPLICATE KEY UPDATE config_name=VALUES(config_name), value_type=VALUES(value_type), config_group=VALUES(config_group), description=VALUES(description);

INSERT INTO system_config (config_key, config_value, config_name, value_type, config_group, description, status) VALUES
('PAYMENT_TIMEOUT_MINUTES','15','订单支付超时分钟数','INTEGER','ORDER','未支付订单自动关闭前的等待分钟数','ENABLED'),
('CHECKIN_TIME','14:00','标准入住时间','TIME','FRONTDESK','酒店标准入住时间','ENABLED'),
('CHECKOUT_TIME','12:00','标准退房时间','TIME','FRONTDESK','酒店标准退房时间','ENABLED'),
('FRIDAY_PRICE_COEFFICIENT','1.05','周五价格系数','DECIMAL','PRICING','周五房价计算系数','ENABLED'),
('SATURDAY_PRICE_COEFFICIENT','1.10','周六价格系数','DECIMAL','PRICING','周六房价计算系数','ENABLED'),
('SUNDAY_PRICE_COEFFICIENT','1.05','周日价格系数','DECIMAL','PRICING','周日房价计算系数','ENABLED'),
('BOOKING_WINDOW_MONTHS','12','可提前预订月数','INTEGER','BOOKING','允许提前选择入住日期的最大月数','ENABLED'),
('MAX_STAY_NIGHTS','30','最长连续入住晚数','INTEGER','BOOKING','单笔订单允许的最长入住晚数','ENABLED')
ON DUPLICATE KEY UPDATE config_name=VALUES(config_name), value_type=VALUES(value_type), config_group=VALUES(config_group), description=VALUES(description);

-- Repair official seed rows by stable codes/dates; old text may already be mojibake.
UPDATE holiday_calendar SET
holiday_name = CASE
    WHEN holiday_code='MID_AUTUMN' THEN '中秋节假期'
    WHEN holiday_code='NATIONAL_DAY' AND day_type='MAKEUP_WORKDAY' THEN '国庆节调休工作日'
    WHEN holiday_code='NATIONAL_DAY' THEN '国庆节假期'
    ELSE holiday_name END,
remark = CASE
    WHEN day_type='MAKEUP_WORKDAY' THEN '官方放假安排中的调休工作日'
    WHEN holiday_code='MID_AUTUMN' THEN CONCAT('假期第', DAY(business_date) - 24, '天')
    WHEN holiday_code='NATIONAL_DAY' THEN CONCAT('假期第', DAY(business_date), '天')
    ELSE remark END
WHERE holiday_year=2026 AND source_status='OFFICIAL'
  AND holiday_code IN ('MID_AUTUMN','NATIONAL_DAY');

-- Add demo main images only when a room type does not already have one.
INSERT INTO room_type_image (room_type_id, image_url, image_type, sort_order)
SELECT seed.room_type_id, seed.image_url, 'MAIN', 1
FROM (
    SELECT 1 AS room_type_id, '/images/room-types/single-main.jpg' AS image_url
    UNION ALL SELECT 2, '/images/room-types/standard-main.jpg'
    UNION ALL SELECT 3, '/images/room-types/superior-main.jpg'
    UNION ALL SELECT 4, '/images/room-types/twin-main.jpg'
    UNION ALL SELECT 5, '/images/room-types/deluxe-main.jpg'
    UNION ALL SELECT 6, '/images/room-types/family-main.jpg'
    UNION ALL SELECT 7, '/images/room-types/suite-main.jpg'
) seed
JOIN room_type rt ON rt.id = seed.room_type_id
WHERE NOT EXISTS (
    SELECT 1 FROM room_type_image existing
    WHERE existing.room_type_id = seed.room_type_id AND existing.image_type = 'MAIN'
);
