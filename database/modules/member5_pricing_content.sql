USE zhisu_hotel;
SET NAMES utf8mb4;

-- Member 5: content, system configuration, holiday calendar, and rolling prices.

CREATE TABLE IF NOT EXISTS system_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    config_key VARCHAR(100) NOT NULL,
    config_value VARCHAR(500) NOT NULL,
    config_name VARCHAR(100) NOT NULL,
    value_type VARCHAR(20) NOT NULL DEFAULT 'STRING',
    config_group VARCHAR(50) NOT NULL DEFAULT 'GENERAL',
    description VARCHAR(500) DEFAULT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ENABLED',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_system_config_key (config_key),
    INDEX idx_system_config_group (config_group)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='system_config';

CREATE TABLE IF NOT EXISTS hotel_banner (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(100) NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    link_url VARCHAR(500) DEFAULT NULL,
    sort_no INT NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'OFFLINE',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_banner_status_sort (status, sort_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='hotel_banner';

CREATE TABLE IF NOT EXISTS announcement (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(150) NOT NULL,
    content TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    is_top TINYINT NOT NULL DEFAULT 0,
    publish_time DATETIME DEFAULT NULL,
    offline_time DATETIME DEFAULT NULL,
    created_by BIGINT DEFAULT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_announcement_status_time (status, publish_time),
    INDEX idx_announcement_top_time (is_top, publish_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='announcement';

CREATE TABLE IF NOT EXISTS holiday_calendar (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    business_date DATE NOT NULL,
    holiday_name VARCHAR(100) NOT NULL,
    holiday_code VARCHAR(50) NOT NULL,
    day_type VARCHAR(30) NOT NULL,
    coefficient DECIMAL(5,2) NOT NULL DEFAULT 1.00,
    holiday_year INT NOT NULL,
    source_status VARCHAR(20) NOT NULL DEFAULT 'OFFICIAL',
    status VARCHAR(20) NOT NULL DEFAULT 'ENABLED',
    remark VARCHAR(255) DEFAULT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_holiday_business_date (business_date),
    INDEX idx_holiday_year (holiday_year),
    INDEX idx_holiday_type_status (day_type, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='holiday_calendar';


CREATE TABLE IF NOT EXISTS price_rule (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    rule_name VARCHAR(100) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    room_type_id BIGINT DEFAULT NULL COMMENT 'NULL means all room types',
    coefficient DECIMAL(5,2) NOT NULL,
    priority INT NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'ENABLED',
    remark VARCHAR(255) DEFAULT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_price_rule_dates_status (start_date, end_date, status),
    INDEX idx_price_rule_room_priority (room_type_id, priority)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='price_rule';

CREATE TABLE IF NOT EXISTS daily_price (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    room_type_id BIGINT NOT NULL,
    business_date DATE NOT NULL,
    base_price DECIMAL(10,2) NOT NULL,
    date_coefficient DECIMAL(5,2) NOT NULL DEFAULT 1.00,
    final_price DECIMAL(10,2) NOT NULL,
    price_source VARCHAR(50) NOT NULL,
    price_description VARCHAR(150) DEFAULT NULL,
    holiday_calendar_id BIGINT DEFAULT NULL,
    source_status VARCHAR(20) NOT NULL DEFAULT 'SYSTEM',
    manual_price DECIMAL(10,2) DEFAULT NULL,
    manual_override TINYINT NOT NULL DEFAULT 0,
    override_reason VARCHAR(200) DEFAULT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_daily_price_room_date (room_type_id, business_date),
    INDEX idx_daily_price_date (business_date),
    INDEX idx_daily_price_room_type (room_type_id),
    INDEX idx_daily_price_source (price_source)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='daily_price';

CREATE TABLE IF NOT EXISTS price_operation_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    room_type_id BIGINT NOT NULL,
    room_type_name VARCHAR(100) NOT NULL,
    old_price DECIMAL(10,2) NOT NULL,
    new_price DECIMAL(10,2) NOT NULL,
    change_reason VARCHAR(200) NOT NULL,
    operator_id BIGINT DEFAULT NULL,
    operator_name VARCHAR(100) DEFAULT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_price_log_room_type (room_type_id),
    INDEX idx_price_log_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='price_operation_log';

INSERT INTO system_config (config_key, config_value, config_name, value_type, config_group, description) VALUES
('MAX_BOOKING_DAYS', '7', 'Maximum booking days', 'INTEGER', 'BOOKING', 'Maximum number of days allowed in a single booking'),
('INVENTORY_WARNING_VALUE', '2', 'Inventory warning value', 'INTEGER', 'INVENTORY', 'Warn when available inventory is at or below this value'),
('IMAGE_UPLOAD_MAX_MB', '5', 'Image upload limit MB', 'INTEGER', 'CONTENT', 'Maximum image upload size in MB'),
('DEFAULT_HOLIDAY_COEFFICIENT', '1.20', 'Default holiday coefficient', 'DECIMAL', 'PRICING', 'Default coefficient for a newly created holiday rule'),
('PAYMENT_TIMEOUT_MINUTES', '15', 'Payment timeout minutes', 'INTEGER', 'ORDER', 'Minutes before an unpaid order times out'),
('CANCEL_THRESHOLD', '3', 'Cancel threshold', 'INTEGER', 'CREDIT', 'Consecutive cancellation threshold before a booking ban'),
('BAN_DAYS', '7', 'Booking ban days', 'INTEGER', 'CREDIT', 'Default booking ban length in days'),
('CHECKIN_TIME', '14:00', 'Check-in time', 'TIME', 'FRONTDESK', 'Default check-in time'),
('CHECKOUT_TIME', '12:00', 'Check-out time', 'TIME', 'FRONTDESK', 'Default check-out time'),
('FRIDAY_PRICE_COEFFICIENT', '1.05', 'Friday price coefficient', 'DECIMAL', 'PRICING', 'Friday room price coefficient'),
('SATURDAY_PRICE_COEFFICIENT', '1.10', 'Saturday price coefficient', 'DECIMAL', 'PRICING', 'Saturday room price coefficient'),
('SUNDAY_PRICE_COEFFICIENT', '1.05', 'Sunday price coefficient', 'DECIMAL', 'PRICING', 'Sunday room price coefficient'),
('MAJOR_HOLIDAY_COEFFICIENT', '1.30', 'Major holiday coefficient', 'DECIMAL', 'PRICING', 'Spring Festival and National Day coefficient'),
('BOOKING_WINDOW_MONTHS', '12', 'Booking window months', 'INTEGER', 'BOOKING', 'Maximum months ahead for check-in date'),
('MAX_STAY_NIGHTS', '30', 'Maximum stay nights', 'INTEGER', 'BOOKING', 'Maximum number of nights allowed in one booking')
ON DUPLICATE KEY UPDATE
config_value = VALUES(config_value),
config_name = VALUES(config_name),
value_type = VALUES(value_type),
config_group = VALUES(config_group),
description = VALUES(description);

INSERT INTO holiday_calendar (business_date, holiday_name, holiday_code, day_type, coefficient, holiday_year, source_status, status, remark) VALUES
('2026-09-20', 'National Day makeup workday', 'NATIONAL_DAY', 'MAKEUP_WORKDAY', 1.00, 2026, 'OFFICIAL', 'ENABLED', 'Sunday workday by official holiday arrangement'),
('2026-09-25', 'Mid-Autumn Festival holiday', 'MID_AUTUMN', 'HOLIDAY', 1.20, 2026, 'OFFICIAL', 'ENABLED', 'Holiday day 1'),
('2026-09-26', 'Mid-Autumn Festival holiday', 'MID_AUTUMN', 'HOLIDAY', 1.20, 2026, 'OFFICIAL', 'ENABLED', 'Holiday day 2'),
('2026-09-27', 'Mid-Autumn Festival holiday', 'MID_AUTUMN', 'HOLIDAY', 1.20, 2026, 'OFFICIAL', 'ENABLED', 'Holiday day 3'),
('2026-10-01', 'National Day holiday', 'NATIONAL_DAY', 'HOLIDAY', 1.30, 2026, 'OFFICIAL', 'ENABLED', 'Holiday day 1'),
('2026-10-02', 'National Day holiday', 'NATIONAL_DAY', 'HOLIDAY', 1.30, 2026, 'OFFICIAL', 'ENABLED', 'Holiday day 2'),
('2026-10-03', 'National Day holiday', 'NATIONAL_DAY', 'HOLIDAY', 1.30, 2026, 'OFFICIAL', 'ENABLED', 'Holiday day 3'),
('2026-10-04', 'National Day holiday', 'NATIONAL_DAY', 'HOLIDAY', 1.30, 2026, 'OFFICIAL', 'ENABLED', 'Holiday day 4'),
('2026-10-05', 'National Day holiday', 'NATIONAL_DAY', 'HOLIDAY', 1.30, 2026, 'OFFICIAL', 'ENABLED', 'Holiday day 5'),
('2026-10-06', 'National Day holiday', 'NATIONAL_DAY', 'HOLIDAY', 1.30, 2026, 'OFFICIAL', 'ENABLED', 'Holiday day 6'),
('2026-10-07', 'National Day holiday', 'NATIONAL_DAY', 'HOLIDAY', 1.30, 2026, 'OFFICIAL', 'ENABLED', 'Holiday day 7'),
('2026-10-10', 'National Day makeup workday', 'NATIONAL_DAY', 'MAKEUP_WORKDAY', 1.00, 2026, 'OFFICIAL', 'ENABLED', 'Saturday workday by official holiday arrangement')
ON DUPLICATE KEY UPDATE
holiday_name = VALUES(holiday_name),
holiday_code = VALUES(holiday_code),
day_type = VALUES(day_type),
coefficient = VALUES(coefficient),
holiday_year = VALUES(holiday_year),
source_status = VALUES(source_status),
status = VALUES(status),
remark = VALUES(remark);

-- =========================================================
-- 成员5：基础价格操作日志
-- 记录房型基础价格调整历史
