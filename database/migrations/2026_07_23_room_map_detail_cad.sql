USE zhisu_hotel;
SET NAMES utf8mb4;

-- Room map detail extensions: room location attributes and CAD image preview.
SET @sql := (
    SELECT IF(COUNT(*) = 0,
        'ALTER TABLE room ADD COLUMN near_elevator TINYINT(1) NOT NULL DEFAULT 0 AFTER maintenance_reason',
        'SELECT ''room.near_elevator already exists''')
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'room'
      AND column_name = 'near_elevator'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql := (
    SELECT IF(COUNT(*) = 0,
        'ALTER TABLE room ADD COLUMN head_room TINYINT(1) NOT NULL DEFAULT 0 AFTER near_elevator',
        'SELECT ''room.head_room already exists''')
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'room'
      AND column_name = 'head_room'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql := (
    SELECT IF(COUNT(*) = 0,
        'ALTER TABLE room ADD COLUMN tail_room TINYINT(1) NOT NULL DEFAULT 0 AFTER head_room',
        'SELECT ''room.tail_room already exists''')
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'room'
      AND column_name = 'tail_room'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql := (
    SELECT IF(COUNT(*) = 0,
        'ALTER TABLE room ADD COLUMN cad_image_url VARCHAR(500) NULL AFTER tail_room',
        'SELECT ''room.cad_image_url already exists''')
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'room'
      AND column_name = 'cad_image_url'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
