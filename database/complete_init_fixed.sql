/*
  Zhisu Hotel database full initialization script.

  Purpose: rebuild zhisu_hotel after backup, then initialize schema,
  member module extensions, constraints, base data, and test data.
  Database: zhisu_hotel on MySQL 8.0+/9.2.0.
  Order: 00_create_database.sql through 05_test_data.sql, with the
  member5 pricing/content module loaded before foreign keys.
  Rebuild behavior: drops only zhisu_hotel, then recreates it.
  Test data: includes test accounts, 120 physical rooms, seven days of
  inventory, and one test order/payment pair.

  Recommended execution:
  mysql --default-character-set=utf8mb4 -u root -p
  source E:/Hotel-online-booking-platform/database/complete_init_fixed.sql;
*/

SET NAMES utf8mb4;
DROP DATABASE IF EXISTS zhisu_hotel;

SOURCE E:/Hotel-online-booking-platform/database/00_create_database.sql;
SOURCE E:/Hotel-online-booking-platform/database/01_base_tables.sql;
SOURCE E:/Hotel-online-booking-platform/database/02_business_tables.sql;
SOURCE E:/Hotel-online-booking-platform/database/modules/member5_pricing_content.sql;
SOURCE E:/Hotel-online-booking-platform/database/03_indexes_constraints.sql;
SOURCE E:/Hotel-online-booking-platform/database/04_initial_data.sql;
SOURCE E:/Hotel-online-booking-platform/database/05_test_data.sql;

SELECT 'database' AS check_item, @@character_set_database AS character_set_database, @@collation_database AS collation_database;
SELECT 'table_count' AS check_item, COUNT(*) AS table_count
FROM information_schema.tables
WHERE table_schema = 'zhisu_hotel';
SELECT 'room_count' AS check_item, COUNT(*) AS room_count FROM zhisu_hotel.room;
