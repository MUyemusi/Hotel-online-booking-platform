USE zhisu_hotel;
SET NAMES utf8mb4;

-- Member 4: physical rooms, room status logs, room assignments, and check-in QR tokens.
-- This script is idempotent and only maintains member 4 tables/data.
-- room_assignment.order_id and checkin_qr_token.order_id are kept as indexed columns.
-- Order foreign keys are intentionally deferred until member 3 order integration is finalized.

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
    INDEX idx_room_status (room_status),
    INDEX idx_room_deleted (is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='room';

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

-- Safe upgrades for existing room tables created before member 4 concurrency fields.
SET @sql := (
    SELECT IF(COUNT(*) = 0,
        'ALTER TABLE room ADD COLUMN version INT NOT NULL DEFAULT 0 AFTER is_deleted',
        'SELECT ''room.version already exists''')
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'room'
      AND column_name = 'version'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql := (
    SELECT IF(COUNT(*) = 0,
        'ALTER TABLE room ADD COLUMN maintenance_reason VARCHAR(500) NULL AFTER room_status',
        'SELECT ''room.maintenance_reason already exists''')
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'room'
      AND column_name = 'maintenance_reason'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Safe upgrades for QR token status and lifecycle fields.
UPDATE checkin_qr_token SET token_status = 'ACTIVE' WHERE token_status = 'VALID';

SET @sql := (
    SELECT IF(COUNT(*) = 0,
        'ALTER TABLE checkin_qr_token ADD COLUMN revoked_at DATETIME NULL AFTER used_at',
        'SELECT ''checkin_qr_token.revoked_at already exists''')
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'checkin_qr_token'
      AND column_name = 'revoked_at'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql := (
    SELECT IF(COUNT(*) = 0,
        'ALTER TABLE checkin_qr_token ADD COLUMN revoked_reason VARCHAR(100) NULL AFTER revoked_at',
        'SELECT ''checkin_qr_token.revoked_reason already exists''')
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'checkin_qr_token'
      AND column_name = 'revoked_reason'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql := (
    SELECT IF(COUNT(*) = 0,
        'ALTER TABLE checkin_qr_token ADD COLUMN version INT NOT NULL DEFAULT 0 AFTER revoked_reason',
        'SELECT ''checkin_qr_token.version already exists''')
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'checkin_qr_token'
      AND column_name = 'version'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql := (
    SELECT IF(COUNT(*) = 0,
        'ALTER TABLE checkin_qr_token ADD UNIQUE KEY uk_checkin_qr_token_hash (token_hash)',
        'SELECT ''uk_checkin_qr_token_hash already exists''')
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'checkin_qr_token'
      AND index_name = 'uk_checkin_qr_token_hash'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql := (
    SELECT IF(COUNT(*) = 0,
        'ALTER TABLE checkin_qr_token ADD INDEX idx_checkin_qr_expire_at (expire_at)',
        'SELECT ''idx_checkin_qr_expire_at already exists''')
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'checkin_qr_token'
      AND index_name = 'idx_checkin_qr_expire_at'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql := (
    SELECT IF(COUNT(*) = 0,
        'ALTER TABLE checkin_qr_token ADD INDEX idx_checkin_qr_order_status (order_id, token_status)',
        'SELECT ''idx_checkin_qr_order_status already exists''')
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'checkin_qr_token'
      AND index_name = 'idx_checkin_qr_order_status'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

CREATE TEMPORARY TABLE IF NOT EXISTS member4_required_room_type_category (
    category VARCHAR(20) PRIMARY KEY
) ENGINE=Memory;

DELETE FROM member4_required_room_type_category;
INSERT INTO member4_required_room_type_category (category) VALUES
('SINGLE'), ('STANDARD'), ('SUPERIOR'), ('TWIN'), ('DELUXE'), ('FAMILY'), ('SUITE');

SELECT r.category AS missing_room_type_category
FROM member4_required_room_type_category r
LEFT JOIN room_type rt ON rt.category = r.category AND rt.is_deleted = 0 AND rt.status = 'ONLINE'
WHERE rt.id IS NULL
ORDER BY r.category;

INSERT INTO room (room_no, floor, room_type_id, room_status, remark, version)
SELECT CONCAT(f.floor_no, LPAD(n.room_seq, 2, '0')) AS room_no,
       f.floor_no,
       rt.id AS room_type_id,
       'AVAILABLE' AS room_status,
       CONCAT(f.floor_no, ' floor physical room seed') AS remark,
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
    WHERE is_deleted = 0
      AND status = 'ONLINE'
      AND category IN ('SINGLE', 'STANDARD', 'SUPERIOR', 'TWIN', 'DELUXE', 'FAMILY', 'SUITE')
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
END
WHERE (
    SELECT COUNT(DISTINCT rt2.category)
    FROM room_type rt2
    JOIN member4_required_room_type_category req ON req.category = rt2.category
    WHERE rt2.is_deleted = 0
      AND rt2.status = 'ONLINE'
) = 7
ON DUPLICATE KEY UPDATE
    floor = VALUES(floor),
    room_type_id = VALUES(room_type_id),
    updated_at = CURRENT_TIMESTAMP;

DROP TEMPORARY TABLE IF EXISTS member4_required_room_type_category;
