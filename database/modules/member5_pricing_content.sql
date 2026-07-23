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
('MAX_BOOKING_DAYS', '7', '最长预订天数', 'INTEGER', 'BOOKING', '单笔订单允许预订的最长天数'),
('INVENTORY_WARNING_VALUE', '2', '库存预警值', 'INTEGER', 'INVENTORY', '可售库存小于等于该值时触发预警'),
('IMAGE_UPLOAD_MAX_MB', '5', '图片上传大小限制', 'INTEGER', 'CONTENT', '单张图片允许上传的最大MB数'),
('DEFAULT_HOLIDAY_COEFFICIENT', '1.20', '默认节假日价格系数', 'DECIMAL', 'PRICING', '新增节假日规则时使用的默认系数'),
('PAYMENT_TIMEOUT_MINUTES', '15', '订单支付超时分钟数', 'INTEGER', 'ORDER', '未支付订单自动关闭前的等待分钟数'),
('CANCEL_THRESHOLD', '3', '连续取消限制阈值', 'INTEGER', 'CREDIT', '达到连续取消次数后限制预订'),
('BAN_DAYS', '7', '限制预订天数', 'INTEGER', 'CREDIT', '触发限制后的默认禁订天数'),
('CHECKIN_TIME', '14:00', '标准入住时间', 'TIME', 'FRONTDESK', '酒店标准入住时间'),
('CHECKOUT_TIME', '12:00', '标准退房时间', 'TIME', 'FRONTDESK', '酒店标准退房时间'),
('FRIDAY_PRICE_COEFFICIENT', '1.05', '周五价格系数', 'DECIMAL', 'PRICING', '周五房价计算系数'),
('SATURDAY_PRICE_COEFFICIENT', '1.10', '周六价格系数', 'DECIMAL', 'PRICING', '周六房价计算系数'),
('SUNDAY_PRICE_COEFFICIENT', '1.05', '周日价格系数', 'DECIMAL', 'PRICING', '周日房价计算系数'),
('MAJOR_HOLIDAY_COEFFICIENT', '1.30', '重大节假日价格系数', 'DECIMAL', 'PRICING', '春节和国庆节等重大节假日价格系数'),
('BOOKING_WINDOW_MONTHS', '12', '可提前预订月数', 'INTEGER', 'BOOKING', '允许提前选择入住日期的最大月数'),
('MAX_STAY_NIGHTS', '30', '最长连续入住晚数', 'INTEGER', 'BOOKING', '单笔订单允许的最长入住晚数'),
('EARLY_MORNING_END_TIME', '06:00', '凌晨入住截止时间', 'TIME', 'BOOKING', '凌晨入住模式的每日截止时间')
ON DUPLICATE KEY UPDATE
config_value = VALUES(config_value),
config_name = VALUES(config_name),
value_type = VALUES(value_type),
config_group = VALUES(config_group),
description = VALUES(description);

INSERT INTO holiday_calendar (business_date, holiday_name, holiday_code, day_type, coefficient, holiday_year, source_status, status, remark) VALUES
('2026-09-20', '国庆节调休工作日', 'NATIONAL_DAY', 'MAKEUP_WORKDAY', 1.00, 2026, 'OFFICIAL', 'ENABLED', '官方放假安排中的周日调休工作日'),
('2026-09-25', '中秋节假期', 'MID_AUTUMN', 'HOLIDAY', 1.20, 2026, 'OFFICIAL', 'ENABLED', '假期第1天'),
('2026-09-26', '中秋节假期', 'MID_AUTUMN', 'HOLIDAY', 1.20, 2026, 'OFFICIAL', 'ENABLED', '假期第2天'),
('2026-09-27', '中秋节假期', 'MID_AUTUMN', 'HOLIDAY', 1.20, 2026, 'OFFICIAL', 'ENABLED', '假期第3天'),
('2026-10-01', '国庆节假期', 'NATIONAL_DAY', 'HOLIDAY', 1.30, 2026, 'OFFICIAL', 'ENABLED', '假期第1天'),
('2026-10-02', '国庆节假期', 'NATIONAL_DAY', 'HOLIDAY', 1.30, 2026, 'OFFICIAL', 'ENABLED', '假期第2天'),
('2026-10-03', '国庆节假期', 'NATIONAL_DAY', 'HOLIDAY', 1.30, 2026, 'OFFICIAL', 'ENABLED', '假期第3天'),
('2026-10-04', '国庆节假期', 'NATIONAL_DAY', 'HOLIDAY', 1.30, 2026, 'OFFICIAL', 'ENABLED', '假期第4天'),
('2026-10-05', '国庆节假期', 'NATIONAL_DAY', 'HOLIDAY', 1.30, 2026, 'OFFICIAL', 'ENABLED', '假期第5天'),
('2026-10-06', '国庆节假期', 'NATIONAL_DAY', 'HOLIDAY', 1.30, 2026, 'OFFICIAL', 'ENABLED', '假期第6天'),
('2026-10-07', '国庆节假期', 'NATIONAL_DAY', 'HOLIDAY', 1.30, 2026, 'OFFICIAL', 'ENABLED', '假期第7天'),
('2026-10-10', '国庆节调休工作日', 'NATIONAL_DAY', 'MAKEUP_WORKDAY', 1.00, 2026, 'OFFICIAL', 'ENABLED', '官方放假安排中的周六调休工作日')
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
