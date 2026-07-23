
USE zhisu_hotel;
SET NAMES utf8mb4;

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='room_type';

CREATE TABLE IF NOT EXISTS room_type_image (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    room_type_id BIGINT NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    image_type VARCHAR(10) NOT NULL DEFAULT 'DETAIL',
    sort_order INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_room_type_image_type_id (room_type_id),
    INDEX idx_room_type_image_type (image_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='room_type_image';

CREATE TABLE IF NOT EXISTS room_type_facility (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    room_type_id BIGINT NOT NULL,
    facility_name VARCHAR(50) NOT NULL,
    facility_icon VARCHAR(100),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_room_type_facility (room_type_id, facility_name),
    INDEX idx_room_type_facility_type_id (room_type_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='room_type_facility';

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='daily_inventory';

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='inventory_log';

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='room';

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
    stay_type VARCHAR(30) NOT NULL DEFAULT 'STANDARD',
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='booking_order';

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='order_guest';

CREATE TABLE IF NOT EXISTS order_daily_price (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    price_date DATE NOT NULL,
    daily_price DECIMAL(10,2) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_order_daily_price (order_id, price_date),
    INDEX idx_order_daily_price_order_id (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='order_daily_price';

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
    cancel_type VARCHAR(20) NOT NULL,
    cancel_reason VARCHAR(500),
    cancel_time DATETIME NOT NULL,
    order_status_before VARCHAR(30),
    payment_status_before VARCHAR(30),
    refund_amount DECIMAL(10,2),
    refund_amount_fen INT,
    refund_id VARCHAR(64),
    operator_id BIGINT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_cancellation_order_id (order_id),
    INDEX idx_cancellation_user_id (user_id),
    INDEX idx_cancellation_time (cancel_time)
    INDEX idx_cancellation_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='cancellation_record';

CREATE TABLE IF NOT EXISTS payment_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    payment_no VARCHAR(64) NOT NULL UNIQUE,
    order_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    amount_fen INT NOT NULL,
    user_id BIGINT NOT NULL,
    payment_method VARCHAR(20) NOT NULL,
    transaction_id VARCHAR(100),
    payment_status VARCHAR(20) NOT NULL DEFAULT 'UNPAID',
    idempotency_key VARCHAR(128),
    paid_at DATETIME,
    refund_amount DECIMAL(10,2),
    refund_amount_fen INT,
    refund_transaction_id VARCHAR(100),
    amount DECIMAL(10,2) NOT NULL,
    amount_fen INT NOT NULL,
    transaction_id VARCHAR(64),
    paid_at DATETIME,
    refund_amount DECIMAL(10,2),
    refund_amount_fen INT,
    refund_transaction_id VARCHAR(64),
    refunded_at DATETIME,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_payment_user_idempotency (user_id, idempotency_key),
    INDEX idx_payment_order_id (order_id),
    INDEX idx_payment_user_id (user_id),
    INDEX idx_payment_status (payment_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='payment_record';

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='scheduled_task_log';

CREATE TABLE IF NOT EXISTS auth_refresh_token (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    token_hash CHAR(64) NOT NULL,
    expires_at DATETIME NOT NULL,
    revoked_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_refresh_token_hash (token_hash),
    INDEX idx_refresh_token_user (user_id),
    INDEX idx_refresh_token_expiry (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='auth_refresh_token';
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='room_status_log';

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='room_assignment';

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='checkin_qr_token';

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='hotel_info';

CREATE TABLE IF NOT EXISTS hotel_notice (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(100) NOT NULL,
    content TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PUBLISHED',
    sort_order INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_hotel_notice_status_sort (status, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='hotel_notice';

CREATE TABLE IF NOT EXISTS hotel_faq (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    question VARCHAR(200) NOT NULL,
    answer TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PUBLISHED',
    sort_order INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_hotel_faq_status_sort (status, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='hotel_faq';

CREATE TABLE IF NOT EXISTS hotel_facility (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    facility_name VARCHAR(50) NOT NULL,
    facility_icon VARCHAR(100),
    description VARCHAR(255),
    status VARCHAR(20) NOT NULL DEFAULT 'ENABLED',
    sort_order INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_hotel_facility_status_sort (status, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='hotel_facility';

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='hotel_policy';

CREATE TABLE IF NOT EXISTS traffic_info (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    traffic_type VARCHAR(50) NOT NULL,
    title VARCHAR(100) NOT NULL,
    content TEXT NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_traffic_info_type_sort (traffic_type, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='traffic_info';
