
USE zhisu_hotel;
SET NAMES utf8mb4;

DROP PROCEDURE IF EXISTS validate_zhisu_schema;
DELIMITER //
CREATE PROCEDURE validate_zhisu_schema()
BEGIN
    DECLARE missing_tables INT DEFAULT 0;
    DECLARE missing_columns INT DEFAULT 0;
    DECLARE missing_configs INT DEFAULT 0;
    DECLARE legacy_foreign_keys INT DEFAULT 0;

    SELECT 10 - COUNT(*) INTO missing_tables
    FROM information_schema.tables
    WHERE table_schema = DATABASE()
      AND table_name IN ('sys_user','booking_order','payment_record','cancellation_record','scheduled_task_log',
                         'auth_refresh_token','hotel_info','room_type_image','price_rule','system_config');

    SELECT 14 - COUNT(*) INTO missing_columns
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND CONCAT(table_name, '.', column_name) IN (
          'sys_user.password_hash','sys_user.last_login_at','booking_order.payment_status','booking_order.stay_type','payment_record.idempotency_key',
          'hotel_info.logo_url','hotel_info.contact_phone','hotel_info.facilities','hotel_info.checkin_notice',
          'hotel_info.cancellation_policy','hotel_info.traffic_info','hotel_info.faq_content',
          'price_rule.priority','auth_refresh_token.token_hash'
      );

    SELECT 4 - COUNT(*) INTO missing_configs
    FROM system_config
    WHERE config_key IN ('INVENTORY_WARNING_VALUE','IMAGE_UPLOAD_MAX_MB','DEFAULT_HOLIDAY_COEFFICIENT','EARLY_MORNING_END_TIME');

    SELECT COUNT(*) INTO legacy_foreign_keys
    FROM information_schema.key_column_usage
    WHERE table_schema = DATABASE() AND referenced_table_name = 'hotel_order';

    IF missing_tables <> 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Schema validation failed: required tables are missing';
    END IF;
    IF missing_columns <> 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Schema validation failed: required columns are missing';
    END IF;
    IF missing_configs <> 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Schema validation failed: member5 system configs are missing';
    END IF;
    IF legacy_foreign_keys <> 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Schema validation failed: legacy hotel_order foreign keys remain';
    END IF;
END//
DELIMITER ;
CALL validate_zhisu_schema();
DROP PROCEDURE validate_zhisu_schema;
