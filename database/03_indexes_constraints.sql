USE zhisu_hotel;
SET NAMES utf8mb4;

SET @db_name = DATABASE();

SET @sql = IF(NOT EXISTS (SELECT 1 FROM information_schema.table_constraints WHERE table_schema = @db_name AND constraint_name = 'fk_user_role_user'),
    'ALTER TABLE sys_user_role ADD CONSTRAINT fk_user_role_user FOREIGN KEY (user_id) REFERENCES sys_user(id) ON UPDATE CASCADE ON DELETE CASCADE',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(NOT EXISTS (SELECT 1 FROM information_schema.table_constraints WHERE table_schema = @db_name AND constraint_name = 'fk_user_role_role'),
    'ALTER TABLE sys_user_role ADD CONSTRAINT fk_user_role_role FOREIGN KEY (role_id) REFERENCES sys_role(id) ON UPDATE CASCADE ON DELETE RESTRICT',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(NOT EXISTS (SELECT 1 FROM information_schema.table_constraints WHERE table_schema = @db_name AND constraint_name = 'fk_credit_record_user'),
    'ALTER TABLE credit_record ADD CONSTRAINT fk_credit_record_user FOREIGN KEY (user_id) REFERENCES sys_user(id) ON UPDATE CASCADE ON DELETE RESTRICT',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(NOT EXISTS (SELECT 1 FROM information_schema.table_constraints WHERE table_schema = @db_name AND constraint_name = 'fk_credit_record_operator'),
    'ALTER TABLE credit_record ADD CONSTRAINT fk_credit_record_operator FOREIGN KEY (operator_id) REFERENCES sys_user(id) ON UPDATE CASCADE ON DELETE SET NULL',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(NOT EXISTS (SELECT 1 FROM information_schema.table_constraints WHERE table_schema = @db_name AND constraint_name = 'fk_operation_log_operator'),
    'ALTER TABLE operation_log ADD CONSTRAINT fk_operation_log_operator FOREIGN KEY (operator_id) REFERENCES sys_user(id) ON UPDATE CASCADE ON DELETE SET NULL',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(NOT EXISTS (SELECT 1 FROM information_schema.table_constraints WHERE table_schema = @db_name AND constraint_name = 'fk_room_type_image_type'),
    'ALTER TABLE room_type_image ADD CONSTRAINT fk_room_type_image_type FOREIGN KEY (room_type_id) REFERENCES room_type(id)',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(NOT EXISTS (SELECT 1 FROM information_schema.table_constraints WHERE table_schema = @db_name AND constraint_name = 'fk_room_type_facility_type'),
    'ALTER TABLE room_type_facility ADD CONSTRAINT fk_room_type_facility_type FOREIGN KEY (room_type_id) REFERENCES room_type(id)',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(NOT EXISTS (SELECT 1 FROM information_schema.table_constraints WHERE table_schema = @db_name AND constraint_name = 'fk_daily_inventory_type'),
    'ALTER TABLE daily_inventory ADD CONSTRAINT fk_daily_inventory_type FOREIGN KEY (room_type_id) REFERENCES room_type(id)',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(NOT EXISTS (SELECT 1 FROM information_schema.table_constraints WHERE table_schema = @db_name AND constraint_name = 'fk_inventory_log_type'),
    'ALTER TABLE inventory_log ADD CONSTRAINT fk_inventory_log_type FOREIGN KEY (room_type_id) REFERENCES room_type(id)',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(NOT EXISTS (SELECT 1 FROM information_schema.table_constraints WHERE table_schema = @db_name AND constraint_name = 'fk_room_type'),
    'ALTER TABLE room ADD CONSTRAINT fk_room_type FOREIGN KEY (room_type_id) REFERENCES room_type(id)',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(NOT EXISTS (SELECT 1 FROM information_schema.table_constraints WHERE table_schema = @db_name AND constraint_name = 'fk_order_user'),
    'ALTER TABLE booking_order ADD CONSTRAINT fk_order_user FOREIGN KEY (user_id) REFERENCES sys_user(id)',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(NOT EXISTS (SELECT 1 FROM information_schema.table_constraints WHERE table_schema = @db_name AND constraint_name = 'fk_order_room_type'),
    'ALTER TABLE booking_order ADD CONSTRAINT fk_order_room_type FOREIGN KEY (room_type_id) REFERENCES room_type(id)',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(NOT EXISTS (SELECT 1 FROM information_schema.table_constraints WHERE table_schema = @db_name AND constraint_name = 'fk_credit_record_order'),
    'ALTER TABLE credit_record ADD CONSTRAINT fk_credit_record_order FOREIGN KEY (order_id) REFERENCES booking_order(id)',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(NOT EXISTS (SELECT 1 FROM information_schema.table_constraints WHERE table_schema = @db_name AND constraint_name = 'fk_cancellation_order'),
    'ALTER TABLE cancellation_record ADD CONSTRAINT fk_cancellation_order FOREIGN KEY (order_id) REFERENCES booking_order(id)',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(NOT EXISTS (SELECT 1 FROM information_schema.table_constraints WHERE table_schema = @db_name AND constraint_name = 'fk_cancellation_user'),
    'ALTER TABLE cancellation_record ADD CONSTRAINT fk_cancellation_user FOREIGN KEY (user_id) REFERENCES sys_user(id)',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(NOT EXISTS (SELECT 1 FROM information_schema.table_constraints WHERE table_schema = @db_name AND constraint_name = 'fk_payment_order'),
    'ALTER TABLE payment_record ADD CONSTRAINT fk_payment_order FOREIGN KEY (order_id) REFERENCES booking_order(id)',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(NOT EXISTS (SELECT 1 FROM information_schema.table_constraints WHERE table_schema = @db_name AND constraint_name = 'fk_payment_user'),
    'ALTER TABLE payment_record ADD CONSTRAINT fk_payment_user FOREIGN KEY (user_id) REFERENCES sys_user(id)',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(NOT EXISTS (SELECT 1 FROM information_schema.table_constraints WHERE table_schema = @db_name AND constraint_name = 'fk_room_status_log_room'),
    'ALTER TABLE room_status_log ADD CONSTRAINT fk_room_status_log_room FOREIGN KEY (room_id) REFERENCES room(id)',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(NOT EXISTS (SELECT 1 FROM information_schema.table_constraints WHERE table_schema = @db_name AND constraint_name = 'fk_room_assignment_order'),
    'ALTER TABLE room_assignment ADD CONSTRAINT fk_room_assignment_order FOREIGN KEY (order_id) REFERENCES booking_order(id)',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(NOT EXISTS (SELECT 1 FROM information_schema.table_constraints WHERE table_schema = @db_name AND constraint_name = 'fk_room_assignment_room'),
    'ALTER TABLE room_assignment ADD CONSTRAINT fk_room_assignment_room FOREIGN KEY (room_id) REFERENCES room(id)',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(NOT EXISTS (SELECT 1 FROM information_schema.table_constraints WHERE table_schema = @db_name AND constraint_name = 'fk_checkin_qr_order'),
    'SELECT ''checkin_qr_token order foreign key deferred until booking_order constraints are finalized''',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @db_name AND table_name = 'daily_price')
    AND NOT EXISTS (SELECT 1 FROM information_schema.table_constraints WHERE table_schema = @db_name AND constraint_name = 'fk_daily_price_room_type'),
    'ALTER TABLE daily_price ADD CONSTRAINT fk_daily_price_room_type FOREIGN KEY (room_type_id) REFERENCES room_type(id)',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @db_name AND table_name = 'daily_price')
    AND EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @db_name AND table_name = 'holiday_calendar')
    AND NOT EXISTS (SELECT 1 FROM information_schema.table_constraints WHERE table_schema = @db_name AND constraint_name = 'fk_daily_price_holiday'),
    'ALTER TABLE daily_price ADD CONSTRAINT fk_daily_price_holiday FOREIGN KEY (holiday_calendar_id) REFERENCES holiday_calendar(id)',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @db_name AND table_name = 'price_operation_log')
    AND NOT EXISTS (SELECT 1 FROM information_schema.table_constraints WHERE table_schema = @db_name AND constraint_name = 'fk_price_log_room_type'),
    'ALTER TABLE price_operation_log ADD CONSTRAINT fk_price_log_room_type FOREIGN KEY (room_type_id) REFERENCES room_type(id)',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
