USE zhisu_hotel;
SET NAMES utf8mb4;
SET @db_name = DATABASE();

SET @sql = IF(
    NOT EXISTS(
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = @db_name
          AND table_name = 'sys_user'
          AND column_name = 'last_login_at'
    ),
    'ALTER TABLE sys_user ADD COLUMN last_login_at DATETIME NULL AFTER ban_until',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
    EXISTS(
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = @db_name
          AND table_name = 'sys_user'
          AND column_name = 'password_hash'
    ),
    'UPDATE sys_user
     SET password_hash = ''$2a$10$wKCAh/ezG5qrfEIXGOQ2IOzDiSRRVWda74Q119PAe2WtxOKESD6FG'',
         status = ''ENABLED''
     WHERE username = ''admin''',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;