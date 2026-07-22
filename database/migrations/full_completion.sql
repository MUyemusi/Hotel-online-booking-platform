USE zhisu_hotel;
SET NAMES utf8mb4;
SET @db_name = DATABASE();

CREATE TABLE IF NOT EXISTS cancellation_record (
 id BIGINT PRIMARY KEY AUTO_INCREMENT, order_id BIGINT NOT NULL, user_id BIGINT NOT NULL,
 cancel_type VARCHAR(30) NOT NULL, cancel_reason VARCHAR(500), cancel_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
 order_status_before VARCHAR(30), payment_status_before VARCHAR(30), refund_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
 refund_amount_fen INT NOT NULL DEFAULT 0, refund_id VARCHAR(64), operator_id BIGINT,
 created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
 INDEX idx_cancellation_order_id(order_id), INDEX idx_cancellation_user_id(user_id), INDEX idx_cancellation_time(cancel_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS scheduled_task_log (
 id BIGINT PRIMARY KEY AUTO_INCREMENT, task_name VARCHAR(100) NOT NULL, executed_at DATETIME NOT NULL,
 closed_orders INT NOT NULL DEFAULT 0, released_rooms INT NOT NULL DEFAULT 0, success TINYINT NOT NULL DEFAULT 1,
 error_message VARCHAR(1000), created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
 INDEX idx_scheduled_task_name_time(task_name,executed_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS auth_refresh_token (
 id BIGINT PRIMARY KEY AUTO_INCREMENT, user_id BIGINT NOT NULL, token_hash CHAR(64) NOT NULL,
 expires_at DATETIME NOT NULL, revoked_at DATETIME NULL, created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
 UNIQUE KEY uk_refresh_token_hash(token_hash), INDEX idx_refresh_token_user(user_id), INDEX idx_refresh_token_expiry(expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SET @sql=IF(NOT EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema=@db_name AND table_name='sys_user' AND column_name='password_hash'),'ALTER TABLE sys_user ADD COLUMN password_hash VARCHAR(255) NULL AFTER username','SELECT 1'); PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @has_old_password=EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema=@db_name AND table_name='sys_user' AND column_name='password');
SET @sql=IF(@has_old_password,'UPDATE sys_user SET password_hash=COALESCE(password_hash,password) WHERE password_hash IS NULL','SELECT 1'); PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @sql=IF(NOT EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema=@db_name AND table_name='sys_user' AND column_name='consecutive_cancel_count'),'ALTER TABLE sys_user ADD COLUMN consecutive_cancel_count INT NOT NULL DEFAULT 0 AFTER status','SELECT 1'); PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @sql=IF(NOT EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema=@db_name AND table_name='sys_user' AND column_name='ban_until'),'ALTER TABLE sys_user ADD COLUMN ban_until DATETIME NULL AFTER consecutive_cancel_count','SELECT 1'); PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @sql=IF(NOT EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema=@db_name AND table_name='sys_user' AND column_name='last_login_at'),'ALTER TABLE sys_user ADD COLUMN last_login_at DATETIME NULL AFTER ban_until','SELECT 1'); PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @sql=IF(@has_old_password,'ALTER TABLE sys_user DROP COLUMN password','SELECT 1'); PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql=IF(NOT EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema=@db_name AND table_name='payment_record' AND column_name='user_id'),'ALTER TABLE payment_record ADD COLUMN user_id BIGINT NULL AFTER order_id','SELECT 1'); PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @sql=IF(NOT EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema=@db_name AND table_name='payment_record' AND column_name='amount_fen'),'ALTER TABLE payment_record ADD COLUMN amount_fen INT NULL AFTER amount','SELECT 1'); PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @sql=IF(NOT EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema=@db_name AND table_name='payment_record' AND column_name='transaction_id'),'ALTER TABLE payment_record ADD COLUMN transaction_id VARCHAR(100) NULL AFTER payment_method','SELECT 1'); PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @sql=IF(NOT EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema=@db_name AND table_name='payment_record' AND column_name='idempotency_key'),'ALTER TABLE payment_record ADD COLUMN idempotency_key VARCHAR(128) NULL AFTER payment_status','SELECT 1'); PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @sql=IF(NOT EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema=@db_name AND table_name='payment_record' AND column_name='refund_amount'),'ALTER TABLE payment_record ADD COLUMN refund_amount DECIMAL(10,2) NULL AFTER paid_at','SELECT 1'); PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @sql=IF(NOT EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema=@db_name AND table_name='payment_record' AND column_name='refund_amount_fen'),'ALTER TABLE payment_record ADD COLUMN refund_amount_fen INT NULL AFTER refund_amount','SELECT 1'); PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @sql=IF(NOT EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema=@db_name AND table_name='payment_record' AND column_name='refund_transaction_id'),'ALTER TABLE payment_record ADD COLUMN refund_transaction_id VARCHAR(100) NULL AFTER refund_amount_fen','SELECT 1'); PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @sql=IF(NOT EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema=@db_name AND table_name='payment_record' AND column_name='refunded_at'),'ALTER TABLE payment_record ADD COLUMN refunded_at DATETIME NULL AFTER refund_transaction_id','SELECT 1'); PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

UPDATE payment_record pr JOIN booking_order bo ON bo.id=pr.order_id SET pr.user_id=COALESCE(pr.user_id,bo.user_id),pr.amount_fen=COALESCE(pr.amount_fen,ROUND(pr.amount*100));
UPDATE payment_record SET payment_status='PAID' WHERE payment_status='SUCCESS';
SET @sql=IF(NOT EXISTS(SELECT 1 FROM information_schema.statistics WHERE table_schema=@db_name AND table_name='payment_record' AND index_name='uk_payment_user_idempotency'),'ALTER TABLE payment_record ADD UNIQUE KEY uk_payment_user_idempotency(user_id,idempotency_key)','SELECT 1'); PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @has_old_cancel=EXISTS(SELECT 1 FROM information_schema.tables WHERE table_schema=@db_name AND table_name='order_cancel_record');
SET @sql=IF(@has_old_cancel,'INSERT INTO cancellation_record(order_id,user_id,cancel_type,cancel_reason,cancel_time,refund_amount,refund_amount_fen,operator_id,created_at) SELECT o.order_id,bo.user_id,o.cancel_type,o.cancel_reason,o.created_at,o.refund_amount,ROUND(o.refund_amount*100),o.cancel_by,o.created_at FROM order_cancel_record o JOIN booking_order bo ON bo.id=o.order_id WHERE NOT EXISTS(SELECT 1 FROM cancellation_record c WHERE c.order_id=o.order_id AND c.cancel_type=o.cancel_type AND c.created_at=o.created_at)','SELECT 1'); PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;