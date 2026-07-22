/*
  Zhisu Hotel database full initialization script.

  Purpose: initialize a new empty zhisu_hotel database, then apply schema,
  member module extensions, constraints, base data, and demonstration data.
  Database: zhisu_hotel on MySQL 8.0+/9.2.0.
  Order: 00_create_database.sql through 05_test_data.sql, with the
  member5 pricing/content module loaded before foreign keys.
  Existing databases: run migrations/full_completion.sql instead so existing data is preserved.
  Isolated rebuilds: use init_test_database.ps1 with a database name ending in _test.
  Test data: includes test accounts, 120 physical rooms, seven days of
  inventory, and one test order/payment pair.

  Recommended execution:
  mysql --default-character-set=utf8mb4 -u root -p
  Run from the repository root: source database/complete_init_fixed.sql;
*/

SET NAMES utf8mb4;
SOURCE database/00_create_database.sql;
SOURCE database/01_base_tables.sql;
SOURCE database/02_business_tables.sql;
SOURCE database/modules/member5_pricing_content.sql;
SOURCE database/migrations/member5_completion.sql;
SOURCE database/migrations/full_completion.sql;
SOURCE database/03_indexes_constraints.sql;
SOURCE database/04_initial_data.sql;
SOURCE database/05_test_data.sql;
SOURCE database/validate_schema.sql;

SELECT 'database' AS check_item, @@character_set_database AS character_set_database, @@collation_database AS collation_database;
SELECT 'table_count' AS check_item, COUNT(*) AS table_count
FROM information_schema.tables
WHERE table_schema = 'zhisu_hotel';
SELECT 'room_count' AS check_item, COUNT(*) AS room_count FROM zhisu_hotel.room;
