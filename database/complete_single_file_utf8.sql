-- =========================================================
-- 智宿酒店完整数据库初始化脚本
-- 适用于MySQL 8.0+
-- 功能：完整初始化、包含所有表结构、约束、数据
-- =========================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =========================================================
-- 1. 创建数据库
-- =========================================================

DROP DATABASE IF EXISTS zhisu_hotel;
CREATE DATABASE zhisu_hotel DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE zhisu_hotel;

-- =========================================================
-- 2. 创建基础表 (01_base_tables.sql)
-- =========================================================

CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    nickname VARCHAR(50),
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(100),
    avatar_url VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'ENABLED',
    consecutive_cancel_count INT NOT NULL DEFAULT 0,
    ban_until DATETIME NULL,
    last_login_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_username (username),
    UNIQUE KEY uk_user_phone (phone),
    UNIQUE KEY uk_user_email (email),
    INDEX idx_user_status (status),
    INDEX idx_user_ban_until (ban_until)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_code VARCHAR(30) NOT NULL,
    role_name VARCHAR(50) NOT NULL,
    description VARCHAR(200),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS sys_user_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_role (user_id, role_id),
    INDEX idx_user_role_user_id (user_id),
    INDEX idx_user_role_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS credit_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    order_id BIGINT NULL,
    change_type VARCHAR(30) NOT NULL,
    before_count INT NOT NULL DEFAULT 0,
    after_count INT NOT NULL DEFAULT 0,
    ban_until DATETIME NULL,
    reason VARCHAR(255) NULL,
    operator_id BIGINT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_credit_user_id (user_id),
    INDEX idx_credit_order_id (order_id),
    INDEX idx_credit_change_type (change_type),
    INDEX idx_credit_created_at (created_at),
    UNIQUE KEY uk_credit_order_type (order_id, change_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS operation_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    operator_id BIGINT NULL,
    module VARCHAR(50) NOT NULL,
    action VARCHAR(50) NOT NULL,
    request_method VARCHAR(10) NULL,
    request_uri VARCHAR(255) NULL,
    result VARCHAR(20) NOT NULL,
    ip_address VARCHAR(45) NULL,
    detail VARCHAR(500) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_log_operator_id (operator_id),
    INDEX idx_log_module (module),
    INDEX idx_log_action (action),
    INDEX idx_log_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================================================
-- 3. 创建业务表 (02_business_tables.sql)
-- =========================================================

CREATE TABLE IF NOT EXISTS room_type (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    category VARCHAR(20) NOT NULL,
    base_price DECIMAL(10,2) NOT NULL,
    area INT,
    bed_type VARCHAR(30),
    bed_count INT NOT NULL DEFAULT 1,
    max_guests INT NOT NULL DEFAULT 2,
    has_breakfast TINYINT NOT NULL DEFAULT 0,
    has_window TINYINT NOT NULL DEFAULT 1,
    has_wifi TINYINT NOT NULL DEFAULT 1,
    has_ac TINYINT NOT NULL DEFAULT 1,
    allow_pets TINYINT NOT NULL DEFAULT 0,
    description TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'ONLINE',
    is_deleted TINYINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_room_type_status (status),
    INDEX idx_room_type_category (category),
    INDEX idx_room_type_deleted (is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS room_type_image (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    room_type_id BIGINT NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    image_type VARCHAR(10) NOT NULL DEFAULT 'DETAIL',
    sort_order INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_room_type_image_type_id (room_type_id),
    INDEX idx_room_type_image_type (image_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS room_type_facility (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    room_type_id BIGINT NOT NULL,
    facility_name VARCHAR(50) NOT NULL,
    facility_icon VARCHAR(100),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_room_type_facility (room_type_id, facility_name),
    INDEX idx_room_type_facility_type_id (room_type_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS daily_inventory (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    room_type_id BIGINT NOT NULL,
    inventory_date DATE NOT NULL,
    total_rooms INT NOT NULL DEFAULT 0,
    locked_rooms INT NOT NULL DEFAULT 0,
    booked_rooms INT NOT NULL DEFAULT 0,
    available_rooms INT NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_daily_inventory_type_date (room_type_id, inventory_date),
    INDEX idx_daily_inventory_date (inventory_date),
    INDEX idx_daily_inventory_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS inventory_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    room_type_id BIGINT NOT NULL,
    inventory_date DATE NOT NULL,
    change_type VARCHAR(30) NOT NULL,
    before_value INT NOT NULL,
    after_value INT NOT NULL,
    change_amount INT NOT NULL,
    operator VARCHAR(50),
    change_reason VARCHAR(200),
    order_id BIGINT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_inventory_log_type_date (room_type_id, inventory_date),
    INDEX idx_inventory_log_order_id (order_id),
    INDEX idx_inventory_log_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS room (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    room_no VARCHAR(20) NOT NULL UNIQUE,
    floor INT NOT NULL,
    room_type_id BIGINT NOT NULL,
    room_status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    maintenance_reason VARCHAR(500),
    remark VARCHAR(500),
    is_deleted TINYINT NOT NULL DEFAULT 0,
    version INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_room_floor (floor),
    INDEX idx_room_type_id (room_type_id),
    INDEX idx_room_status (room_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS booking_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_no VARCHAR(64) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    room_type_id BIGINT NOT NULL,
    room_type_name VARCHAR(100),
    room_type_snapshot TEXT,
    facilities_snapshot TEXT,
    room_count INT NOT NULL DEFAULT 1,
    check_in_date DATE NOT NULL,
    check_out_date DATE NOT NULL,
    total_nights INT NOT NULL DEFAULT 1,
    guest_count INT NOT NULL DEFAULT 1,
    total_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    total_amount_fen INT NOT NULL DEFAULT 0,
    idempotency_key VARCHAR(128),
    phone VARCHAR(20),
    order_status VARCHAR(30) NOT NULL DEFAULT 'PENDING_PAYMENT',
    payment_status VARCHAR(30) NOT NULL DEFAULT 'UNPAID',
    payment_timeout DATETIME,
    paid_at DATETIME,
    cancelled_at DATETIME,
    cancel_type VARCHAR(30),
    cancel_reason VARCHAR(500),
    remark VARCHAR(500),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_booking_order_user_id (user_id),
    INDEX idx_booking_order_room_type_id (room_type_id),
    INDEX idx_booking_order_status_timeout (order_status, payment_timeout),
    INDEX idx_booking_order_dates (check_in_date, check_out_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS order_guest (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    guest_name VARCHAR(50) NOT NULL,
    guest_phone VARCHAR(20),
    id_type VARCHAR(20),
    id_number VARCHAR(64),
    is_main TINYINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_order_guest_order_id (order_id),
    INDEX idx_order_guest_name (guest_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS order_daily_price (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    price_date DATE NOT NULL,
    daily_price DECIMAL(10,2) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_order_daily_price (order_id, price_date),
    INDEX idx_order_daily_price_order_id (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS cancellation_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    cancel_type VARCHAR(30) NOT NULL,
    cancel_reason VARCHAR(500),
    cancel_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    order_status_before VARCHAR(30),
    payment_status_before VARCHAR(30),
    refund_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    refund_amount_fen INT NOT NULL DEFAULT 0,
    refund_id VARCHAR(64),
    operator_id BIGINT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_cancellation_order_id (order_id),
    INDEX idx_cancellation_user_id (user_id),
    INDEX idx_cancellation_time (cancel_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS payment_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    payment_no VARCHAR(64) NOT NULL UNIQUE,
    order_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    amount_fen INT NOT NULL,
    payment_method VARCHAR(20) NOT NULL,
    transaction_id VARCHAR(100),
    payment_status VARCHAR(20) NOT NULL DEFAULT 'UNPAID',
    idempotency_key VARCHAR(128),
    paid_at DATETIME,
    refund_amount DECIMAL(10,2),
    refund_amount_fen INT,
    refund_transaction_id VARCHAR(100),
    refunded_at DATETIME,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_payment_user_idempotency (user_id, idempotency_key),
    INDEX idx_payment_order_id (order_id),
    INDEX idx_payment_user_id (user_id),
    INDEX idx_payment_status (payment_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS scheduled_task_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    task_name VARCHAR(100) NOT NULL,
    executed_at DATETIME NOT NULL,
    closed_orders INT NOT NULL DEFAULT 0,
    released_rooms INT NOT NULL DEFAULT 0,
    success TINYINT NOT NULL DEFAULT 1,
    error_message VARCHAR(1000),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_scheduled_task_name_time (task_name, executed_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS auth_refresh_token (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    token_hash CHAR(64) NOT NULL,
    expires_at DATETIME NOT NULL,
    revoked_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_refresh_token_hash (token_hash),
    INDEX idx_refresh_token_user (user_id),
    INDEX idx_refresh_token_expires_at (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS room_status_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    room_id BIGINT NOT NULL,
    before_status VARCHAR(20),
    after_status VARCHAR(20) NOT NULL,
    operator_id BIGINT,
    operator_type VARCHAR(50),
    reason VARCHAR(500),
    related_order_id BIGINT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_room_status_log_room_id (room_id),
    INDEX idx_room_status_log_order_id (related_order_id),
    INDEX idx_room_status_log_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS room_assignment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    room_id BIGINT NOT NULL,
    assignment_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    assigned_by BIGINT,
    assigned_at DATETIME,
    check_in_time DATETIME,
    check_out_time DATETIME,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_room_assignment_order_id (order_id),
    INDEX idx_room_assignment_room_id (room_id),
    INDEX idx_room_assignment_status (assignment_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS checkin_qr_token (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    token_hash VARCHAR(255) NOT NULL,
    token_status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    expire_at DATETIME NOT NULL,
    used_at DATETIME,
    revoked_at DATETIME,
    revoked_reason VARCHAR(100),
    version INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_checkin_qr_order_id (order_id),
    UNIQUE KEY uk_checkin_qr_token_hash (token_hash),
    INDEX idx_checkin_qr_status (token_status),
    INDEX idx_checkin_qr_expire_at (expire_at),
    INDEX idx_checkin_qr_order_status (order_id, token_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS hotel_info (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    hotel_name VARCHAR(100) NOT NULL,
    logo_url VARCHAR(500) DEFAULT NULL,
    introduction TEXT,
    address VARCHAR(255) DEFAULT NULL,
    contact_phone VARCHAR(30) DEFAULT NULL,
    facilities TEXT,
    checkin_notice TEXT,
    cancellation_policy TEXT,
    traffic_info TEXT,
    faq_content TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'PUBLISHED',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS hotel_notice (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(100) NOT NULL,
    content TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PUBLISHED',
    sort_order INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_hotel_notice_status_sort (status, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS hotel_faq (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    question VARCHAR(200) NOT NULL,
    answer TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PUBLISHED',
    sort_order INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_hotel_faq_status_sort (status, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS hotel_facility (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    facility_name VARCHAR(50) NOT NULL,
    facility_icon VARCHAR(100),
    description VARCHAR(255),
    status VARCHAR(20) NOT NULL DEFAULT 'ENABLED',
    sort_order INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_hotel_facility_status_sort (status, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS hotel_policy (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    policy_type VARCHAR(50) NOT NULL,
    title VARCHAR(100) NOT NULL,
    content TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PUBLISHED',
    sort_order INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_hotel_policy_type_status (policy_type, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS traffic_info (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    traffic_type VARCHAR(50) NOT NULL,
    title VARCHAR(100) NOT NULL,
    content TEXT NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_traffic_info_type_sort (traffic_type, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================================================
-- 4. 创建成员5的定价和内容表 (member5_pricing_content.sql)
-- =========================================================

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS holiday_calendar (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    business_date DATE NOT NULL,
    holiday_name VARCHAR(100) NOT NULL,
    holiday_code VARCHAR(50) NOT NULL,
    day_type VARCHAR(30) NOT NULL,
    coefficient DECIMAL(5,2) NOT NULL,
    holiday_year INT NOT NULL,
    source_status VARCHAR(20) NOT NULL DEFAULT 'OFFICIAL',
    status VARCHAR(20) NOT NULL DEFAULT 'ENABLED',
    remark VARCHAR(255) DEFAULT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_holiday_business_date (business_date),
    INDEX idx_holiday_year (holiday_year),
    INDEX idx_holiday_type_status (day_type, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================================================
-- 5. 基础数据
-- =========================================================

INSERT INTO sys_role (id, role_code, role_name, description) VALUES
(1, 'USER', 'Guest', 'Registered guest user'),
(2, 'FRONT_DESK', 'Front Desk', 'Front desk staff'),
(3, 'ADMIN', 'Administrator', 'System administrator')
ON DUPLICATE KEY UPDATE
role_code = VALUES(role_code),
role_name = VALUES(role_name),
description = VALUES(description);

INSERT INTO sys_user (id, username, password_hash, nickname, phone, email, status, consecutive_cancel_count) VALUES
(1, 'admin', '$2a$10$wKCAh/ezG5qrfEIXGOQ2IOzDiSRRVWda74Q119PAe2WtxOKESD6FG', 'Admin', '13800000000', 'admin@zhisu.local', 'ENABLED', 0),
(2, 'frontdesk', '$2a$10$af3SFLf8CU5z9Ve.LYFhA.XvXu3bnsV9wI03t1AFGFQ1Y2mMeY876', 'Front Desk', '13800000001', 'frontdesk@zhisu.local', 'ENABLED', 0),
(3, 'guest', '$2a$10$hTOf3Jv.cutfEQMm5a/3uDHLdPCEzcDJizKDaSMURDOodW8d2X7K', 'Test Guest', '13800000002', 'guest@zhisu.local', 'ENABLED', 0)
ON DUPLICATE KEY UPDATE
password_hash = VALUES(password_hash),
nickname = VALUES(nickname),
phone = VALUES(phone),
email = VALUES(email),
status = VALUES(status),
consecutive_cancel_count = VALUES(consecutive_cancel_count);

INSERT INTO sys_user_role (user_id, role_id) VALUES
(1, 3),
(2, 2),
(3, 1)
ON DUPLICATE KEY UPDATE role_id = VALUES(role_id);

INSERT INTO room_type (id, name, category, base_price, area, bed_type, bed_count, max_guests, has_breakfast, has_window, has_wifi, has_ac, allow_pets, description, status) VALUES
(1, 'Single Room', 'SINGLE', 128.00, 18, 'Single Bed', 1, 1, 0, 1, 1, 1, 0, 'Compact single room', 'ONLINE'),
(2, 'Standard Double', 'STANDARD', 228.00, 28, 'Double Bed', 1, 2, 0, 1, 1, 1, 0, 'Comfortable standard room', 'ONLINE'),
(3, 'Superior Double', 'SUPERIOR', 328.00, 35, 'Double Bed', 1, 2, 1, 1, 1, 1, 0, 'Premium superior room', 'ONLINE'),
(4, 'Business Twin', 'TWIN', 298.00, 32, 'Twin Beds', 2, 2, 1, 1, 1, 1, 0, 'Two single beds for business', 'ONLINE'),
(5, 'Deluxe Room', 'DELUXE', 458.00, 42, 'Double Bed', 1, 2, 1, 1, 1, 1, 0, 'Luxury deluxe room', 'ONLINE'),
(6, 'Family Room', 'FAMILY', 398.00, 45, 'Double + Single', 2, 3, 1, 1, 1, 1, 1, 'Comfortable for family', 'ONLINE'),
(7, 'Suite', 'SUITE', 688.00, 60, 'Double Bed', 1, 4, 1, 1, 1, 1, 0, 'Premium suite with living area', 'ONLINE')
ON DUPLICATE KEY UPDATE name = VALUES(name), category = VALUES(category), base_price = VALUES(base_price), area = VALUES(area), bed_type = VALUES(bed_type), bed_count = VALUES(bed_count), max_guests = VALUES(max_guests), description = VALUES(description), status = VALUES(status);

INSERT INTO room_type_facility (room_type_id, facility_name, facility_icon) VALUES
(1, 'Free WiFi', 'wifi'), (1, 'AC', 'wind'), (1, 'Hot Water', 'shower-head'),
(2, 'Free WiFi', 'wifi'), (2, 'TV', 'tv'), (2, 'Work Desk', 'briefcase'),
(3, 'Work Desk', 'coffee'), (3, 'TV', 'tv'), (3, 'Safe', 'shield'),
(4, 'Twin Beds', 'bed'), (4, 'Work Desk', 'briefcase'), (4, 'Premium WiFi', 'wifi'),
(5, 'Welcome Fruit', 'cup-soda'), (5, 'Bathtub', 'bath'), (5, 'Mineral Water', 'apple'),
(6, 'Kids Toys', 'baby'), (6, 'Family Amenities', 'blocks'), (6, 'Kids Slippers', 'footprints'),
(7, 'Living Area', 'sofa'), (7, 'Smart Doorbell', 'bell'), (7, 'Coffee', 'coffee')
ON DUPLICATE KEY UPDATE facility_icon = VALUES(facility_icon);

INSERT INTO room_type_image (room_type_id, image_url, image_type, sort_order) VALUES
(1, '/images/room-types/single-main.jpg', 'MAIN', 1),
(2, '/images/room-types/standard-main.jpg', 'MAIN', 1),
(3, '/images/room-types/superior-main.jpg', 'MAIN', 1),
(4, '/images/room-types/twin-main.jpg', 'MAIN', 1),
(5, '/images/room-types/deluxe-main.jpg', 'MAIN', 1),
(6, '/images/room-types/family-main.jpg', 'MAIN', 1),
(7, '/images/room-types/suite-main.jpg', 'MAIN', 1);

INSERT INTO hotel_info (id, hotel_name, logo_url, introduction, address, contact_phone, facilities, checkin_notice, cancellation_policy, traffic_info, faq_content, status) VALUES
(1, 'Zhisu Hotel', '/images/logo.png', 'Zhisu Hotel offers smart and comfortable accommodation', '88 Downtown Street', '400-800-2026', 'Free WiFi, Parking, AC', 'Check-in at 2pm, Check-out at 12pm', 'Free cancel 7 days before', 'Located in city center, easy access', 'Q: Can I check-in early? A: Yes, please contact front desk', 'PUBLISHED')
ON DUPLICATE KEY UPDATE hotel_name = VALUES(hotel_name), logo_url = VALUES(logo_url), introduction = VALUES(introduction), address = VALUES(address), contact_phone = VALUES(contact_phone), facilities = VALUES(facilities), checkin_notice = VALUES(checkin_notice), cancellation_policy = VALUES(cancellation_policy), traffic_info = VALUES(traffic_info), faq_content = VALUES(faq_content), status = VALUES(status);

INSERT INTO hotel_notice (id, title, content, status, sort_order) VALUES
(1, 'Check-in Notice', 'Please present valid ID at check-in', 'PUBLISHED', 1),
(2, 'Maintenance Notice', 'AC maintenance from 7am to 10am', 'PUBLISHED', 2)
ON DUPLICATE KEY UPDATE title = VALUES(title), content = VALUES(content), status = VALUES(status), sort_order = VALUES(sort_order);

INSERT INTO hotel_faq (id, question, answer, status, sort_order) VALUES
(1, 'Can I check-in early?', 'Yes, please contact front desk', 'PUBLISHED', 1),
(2, 'Is parking free?', 'Yes, free parking available', 'PUBLISHED', 2)
ON DUPLICATE KEY UPDATE question = VALUES(question), answer = VALUES(answer), status = VALUES(status), sort_order = VALUES(sort_order);

INSERT INTO hotel_facility (id, facility_name, facility_icon, description, status, sort_order) VALUES
(1, 'Free Parking', 'parking-circle', 'Free parking available', 'ENABLED', 1),
(2, 'Business Lounge', 'coffee', 'Business lounge in lobby', 'ENABLED', 2),
(3, 'High Speed WiFi', 'wifi', 'Free WiFi in public areas', 'ENABLED', 3)
ON DUPLICATE KEY UPDATE facility_name = VALUES(facility_name), facility_icon = VALUES(facility_icon), description = VALUES(description), status = VALUES(status), sort_order = VALUES(sort_order);

INSERT INTO hotel_policy (id, policy_type, title, content, status, sort_order) VALUES
(1, 'CHECKIN', 'Check-in Policy', 'Check-in at 2pm, Check-out at 12pm', 'PUBLISHED', 1),
(2, 'CANCEL', 'Cancellation Policy', 'Free cancel 7 days before', 'PUBLISHED', 2)
ON DUPLICATE KEY UPDATE policy_type = VALUES(policy_type), title = VALUES(title), content = VALUES(content), status = VALUES(status), sort_order = VALUES(sort_order);

INSERT INTO traffic_info (id, traffic_type, title, content, sort_order) VALUES
(1, 'SUBWAY', 'Subway Access', '8 min walk from Downtown Station', 1),
(2, 'AIRPORT', 'Airport Access', '40 min drive from airport', 2)
ON DUPLICATE KEY UPDATE traffic_type = VALUES(traffic_type), title = VALUES(title), content = VALUES(content), sort_order = VALUES(sort_order);

-- =========================================================
-- 6. 系统配置
-- =========================================================

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

-- =========================================================
-- 7. 创建索引和外键约束
-- =========================================================

SET @db_name = DATABASE();

SET @sql = IF(
    NOT EXISTS (
        SELECT 1
        FROM information_schema.table_constraints
        WHERE table_schema = @db_name
          AND constraint_name = 'fk_user_role_user'
    ),
    'ALTER TABLE sys_user_role ADD CONSTRAINT fk_user_role_user FOREIGN KEY (user_id) REFERENCES sys_user(id) ON UPDATE CASCADE ON DELETE CASCADE',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
    NOT EXISTS (
        SELECT 1
        FROM information_schema.table_constraints
        WHERE table_schema = @db_name
          AND constraint_name = 'fk_user_role_role'
    ),
    'ALTER TABLE sys_user_role ADD CONSTRAINT fk_user_role_role FOREIGN KEY (role_id) REFERENCES sys_role(id) ON UPDATE CASCADE ON DELETE RESTRICT',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
    NOT EXISTS (
        SELECT 1
        FROM information_schema.table_constraints
        WHERE table_schema = @db_name
          AND constraint_name = 'fk_credit_record_user'
    ),
    'ALTER TABLE credit_record ADD CONSTRAINT fk_credit_record_user FOREIGN KEY (user_id) REFERENCES sys_user(id) ON UPDATE CASCADE ON DELETE RESTRICT',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
    NOT EXISTS (
        SELECT 1
        FROM information_schema.table_constraints
        WHERE table_schema = @db_name
          AND constraint_name = 'fk_credit_record_operator'
    ),
    'ALTER TABLE credit_record ADD CONSTRAINT fk_credit_record_operator FOREIGN KEY (operator_id) REFERENCES sys_user(id) ON UPDATE CASCADE ON DELETE SET NULL',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
    NOT EXISTS (
        SELECT 1
        FROM information_schema.table_constraints
        WHERE table_schema = @db_name
          AND constraint_name = 'fk_operation_log_operator'
    ),
    'ALTER TABLE operation_log ADD CONSTRAINT fk_operation_log_operator FOREIGN KEY (operator_id) REFERENCES sys_user(id) ON UPDATE CASCADE ON DELETE SET NULL',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
    NOT EXISTS (
        SELECT 1
        FROM information_schema.table_constraints
        WHERE table_schema = @db_name
          AND constraint_name = 'fk_room_type_image_type'
    ),
    'ALTER TABLE room_type_image ADD CONSTRAINT fk_room_type_image_type FOREIGN KEY (room_type_id) REFERENCES room_type(id)',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
    NOT EXISTS (
        SELECT 1
        FROM information_schema.table_constraints
        WHERE table_schema = @db_name
          AND constraint_name = 'fk_room_type_facility_type'
    ),
    'ALTER TABLE room_type_facility ADD CONSTRAINT fk_room_type_facility_type FOREIGN KEY (room_type_id) REFERENCES room_type(id)',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
    NOT EXISTS (
        SELECT 1
        FROM information_schema.table_constraints
        WHERE table_schema = @db_name
          AND constraint_name = 'fk_daily_inventory_type'
    ),
    'ALTER TABLE daily_inventory ADD CONSTRAINT fk_daily_inventory_type FOREIGN KEY (room_type_id) REFERENCES room_type(id)',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
    NOT EXISTS (
        SELECT 1
        FROM information_schema.table_constraints
        WHERE table_schema = @db_name
          AND constraint_name = 'fk_inventory_log_type'
    ),
    'ALTER TABLE inventory_log ADD CONSTRAINT fk_inventory_log_type FOREIGN KEY (room_type_id) REFERENCES room_type(id)',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
    NOT EXISTS (
        SELECT 1
        FROM information_schema.table_constraints
        WHERE table_schema = @db_name
          AND constraint_name = 'fk_room_type'
    ),
    'ALTER TABLE room ADD CONSTRAINT fk_room_type FOREIGN KEY (room_type_id) REFERENCES room_type(id)',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(NOT EXISTS (SELECT 1 FROM information_schema.table_constraints WHERE table_schema = @db_name AND constraint_name = 'fk_order_user'),
    'ALTER TABLE booking_order ADD CONSTRAINT fk_order_user FOREIGN KEY (user_id) REFERENCES sys_user(id)',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(NOT EXISTS (SELECT 1 FROM information_schema.table_constraints WHERE table_schema = @db_name AND constraint_name = 'fk_order_room_type'),
    'ALTER TABLE booking_order ADD CONSTRAINT fk_order_room_type FOREIGN KEY (room_type_id) REFERENCES room_type(id)',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(NOT EXISTS (SELECT 1 FROM information_schema.table_constraints WHERE table_schema = @db_name AND constraint_name = 'fk_credit_record_order'),
    'ALTER TABLE credit_record ADD CONSTRAINT fk_credit_record_order FOREIGN KEY (order_id) REFERENCES booking_order(id)',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(NOT EXISTS (SELECT 1 FROM information_schema.table_constraints WHERE table_schema = @db_name AND constraint_name = 'fk_cancellation_order'),
    'ALTER TABLE cancellation_record ADD CONSTRAINT fk_cancellation_order FOREIGN KEY (order_id) REFERENCES booking_order(id)',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(NOT EXISTS (SELECT 1 FROM information_schema.table_constraints WHERE table_schema = @db_name AND constraint_name = 'fk_payment_order'),
    'ALTER TABLE payment_record ADD CONSTRAINT fk_payment_order FOREIGN KEY (order_id) REFERENCES booking_order(id)',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(NOT EXISTS (SELECT 1 FROM information_schema.table_constraints WHERE table_schema = @db_name AND constraint_name = 'fk_payment_user'),
    'ALTER TABLE payment_record ADD CONSTRAINT fk_payment_user FOREIGN KEY (user_id) REFERENCES sys_user(id)',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(NOT EXISTS (SELECT 1 FROM information_schema.table_constraints WHERE table_schema = @db_name AND constraint_name = 'fk_cancellation_user'),
    'ALTER TABLE cancellation_record ADD CONSTRAINT fk_cancellation_user FOREIGN KEY (user_id) REFERENCES sys_user(id)',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(NOT EXISTS (SELECT 1 FROM information_schema.table_constraints WHERE table_schema = @db_name AND constraint_name = 'fk_refresh_token_user'),
    'ALTER TABLE auth_refresh_token ADD CONSTRAINT fk_refresh_token_user FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
    NOT EXISTS (
        SELECT 1
        FROM information_schema.table_constraints
        WHERE table_schema = @db_name
          AND constraint_name = 'fk_room_status_log_room'
    ),
    'ALTER TABLE room_status_log ADD CONSTRAINT fk_room_status_log_room FOREIGN KEY (room_id) REFERENCES room(id)',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(NOT EXISTS (SELECT 1 FROM information_schema.table_constraints WHERE table_schema = @db_name AND constraint_name = 'fk_room_assignment_order'),
    'ALTER TABLE room_assignment ADD CONSTRAINT fk_room_assignment_order FOREIGN KEY (order_id) REFERENCES booking_order(id)',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
    NOT EXISTS (
        SELECT 1
        FROM information_schema.table_constraints
        WHERE table_schema = @db_name
          AND constraint_name = 'fk_room_assignment_room'
    ),
    'ALTER TABLE room_assignment ADD CONSTRAINT fk_room_assignment_room FOREIGN KEY (room_id) REFERENCES room(id)',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
    EXISTS (
        SELECT 1
        FROM information_schema.tables
        WHERE table_schema = @db_name
          AND table_name = 'daily_price'
    )
    AND NOT EXISTS (
        SELECT 1
        FROM information_schema.table_constraints
        WHERE table_schema = @db_name
          AND constraint_name = 'fk_daily_price_room_type'
    ),
    'ALTER TABLE daily_price ADD CONSTRAINT fk_daily_price_room_type FOREIGN KEY (room_type_id) REFERENCES room_type(id)',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
    EXISTS (
        SELECT 1
        FROM information_schema.tables
        WHERE table_schema = @db_name
          AND table_name = 'daily_price'
    )
    AND EXISTS (
        SELECT 1
        FROM information_schema.tables
        WHERE table_schema = @db_name
          AND table_name = 'holiday_calendar'
    )
    AND NOT EXISTS (
        SELECT 1
        FROM information_schema.table_constraints
        WHERE table_schema = @db_name
          AND constraint_name = 'fk_daily_price_holiday'
    ),
    'ALTER TABLE daily_price ADD CONSTRAINT fk_daily_price_holiday FOREIGN KEY (holiday_calendar_id) REFERENCES holiday_calendar(id)',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
    EXISTS (
        SELECT 1
        FROM information_schema.tables
        WHERE table_schema = @db_name
          AND table_name = 'price_operation_log'
    )
    AND NOT EXISTS (
        SELECT 1
        FROM information_schema.table_constraints
        WHERE table_schema = @db_name
          AND constraint_name = 'fk_price_log_room_type'
    ),
    'ALTER TABLE price_operation_log ADD CONSTRAINT fk_price_log_room_type FOREIGN KEY (room_type_id) REFERENCES room_type(id)',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- =========================================================
-- 8. 插入房间数据
-- =========================================================

INSERT INTO room (room_no, floor, room_type_id, room_status, remark, version)
SELECT
    CONCAT(f.floor_no, LPAD(n.room_seq, 2, '0')) AS room_no,
    f.floor_no,
    rt.id AS room_type_id,
    'AVAILABLE' AS room_status,
    'Initialized room' AS remark,
    0 AS version
FROM (
    SELECT 3 AS floor_no UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL
    SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8
) f
CROSS JOIN (
    SELECT 1 AS room_seq UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL
    SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL SELECT 10 UNION ALL
    SELECT 11 UNION ALL SELECT 12 UNION ALL SELECT 13 UNION ALL SELECT 14 UNION ALL SELECT 15 UNION ALL
    SELECT 16 UNION ALL SELECT 17 UNION ALL SELECT 18 UNION ALL SELECT 19 UNION ALL SELECT 20
) n
JOIN (
    SELECT category, MIN(id) AS id
    FROM room_type
    WHERE is_deleted = 0 AND status = 'ONLINE'
    GROUP BY category
) rt ON rt.category = CASE
    WHEN f.floor_no = 3 AND n.room_seq <= 5 THEN 'SINGLE'
    WHEN f.floor_no = 3 THEN 'STANDARD'
    WHEN f.floor_no = 4 AND n.room_seq <= 5 THEN 'STANDARD'
    WHEN f.floor_no = 4 THEN 'TWIN'
    WHEN f.floor_no = 5 AND n.room_seq <= 10 THEN 'SUPERIOR'
    WHEN f.floor_no = 5 THEN 'TWIN'
    WHEN f.floor_no = 6 AND n.room_seq <= 10 THEN 'DELUXE'
    WHEN f.floor_no = 6 THEN 'FAMILY'
    WHEN f.floor_no = 7 AND n.room_seq <= 10 THEN 'DELUXE'
    WHEN f.floor_no = 7 THEN 'SUITE'
    ELSE 'SUITE'
END;

-- 插入库存数据
INSERT INTO daily_inventory (
    room_type_id, inventory_date, total_rooms, locked_rooms, booked_rooms, available_rooms, status
)
SELECT
    room_type_id,
    inventory_date,
    COUNT(room.id) AS total_rooms,
    0, 0,
    COUNT(room.id) AS available_rooms,
    'ACTIVE' AS status
FROM room_type
JOIN room ON room.room_type_id = room_type.id AND room.is_deleted = 0
CROSS JOIN (
    SELECT CURDATE() AS inventory_date UNION ALL
    SELECT DATE_ADD(CURDATE(), INTERVAL 1 DAY) UNION ALL
    SELECT DATE_ADD(CURDATE(), INTERVAL 2 DAY) UNION ALL
    SELECT DATE_ADD(CURDATE(), INTERVAL 3 DAY) UNION ALL
    SELECT DATE_ADD(CURDATE(), INTERVAL 4 DAY) UNION ALL
    SELECT DATE_ADD(CURDATE(), INTERVAL 5 DAY) UNION ALL
    SELECT DATE_ADD(CURDATE(), INTERVAL 6 DAY)
) inventory_dates
GROUP BY room_type_id, inventory_date;

-- =========================================================
-- 完成
-- =========================================================
SET FOREIGN_KEY_CHECKS = 1;

SELECT 'Database initialization complete!' AS message;
