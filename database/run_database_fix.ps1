$ErrorActionPreference = "Stop"

$asciiRoot = "E:\Hotel-online-booking-platform"
$root = if (Test-Path -LiteralPath $asciiRoot) { $asciiRoot } else { Split-Path -Parent $PSScriptRoot }
$mysql = "E:\java\SQL\MySQL\MySQL Server 9.2\bin\mysql.exe"
$mysqldump = "E:\java\SQL\MySQL\MySQL Server 9.2\bin\mysqldump.exe"
$databaseDir = Join-Path $root "database"
$backup = Join-Path $databaseDir "backup_before_database_fix.sql"
$backupTemp = Join-Path $databaseDir "backup_before_database_fix.sql.tmp"
$log = Join-Path $databaseDir "database_fix_validation.log"
$initScript = (Join-Path $databaseDir "complete_init_fixed.sql").Replace("\", "/")

if (!(Test-Path -LiteralPath $mysql)) {
    throw "mysql.exe not found: $mysql"
}
if (!(Test-Path -LiteralPath $mysqldump)) {
    throw "mysqldump.exe not found: $mysqldump"
}

Write-Host "Step 1/3: backing up current zhisu_hotel to $backup"
Write-Host "MySQL will ask for the root password. Type it in this window only."
if (Test-Path -LiteralPath $backupTemp) {
    Remove-Item -LiteralPath $backupTemp -Force
}
& $mysqldump --default-character-set=utf8mb4 -u root -p zhisu_hotel --result-file="$backupTemp"
if ($LASTEXITCODE -ne 0) {
    throw "mysqldump failed with exit code $LASTEXITCODE"
}

$backupFile = Get-Item -LiteralPath $backupTemp
if ($backupFile.Length -le 0) {
    throw "Backup file was created but is empty."
}
Move-Item -LiteralPath $backupTemp -Destination $backup -Force
$backupFile = Get-Item -LiteralPath $backup
Write-Host "Backup OK: $($backupFile.FullName) ($($backupFile.Length) bytes)"

Write-Host "Step 2/3: rebuilding zhisu_hotel from complete_init_fixed.sql"
Write-Host "MySQL will ask for the root password again."
& $mysql --default-character-set=utf8mb4 -u root -p -e "source $initScript;" 2>&1 | Tee-Object -FilePath $log
if ($LASTEXITCODE -ne 0) {
    throw "Database initialization failed with exit code $LASTEXITCODE. See $log"
}

Write-Host "Step 3/3: running validation queries"
$validationSql = @(
    "SHOW CREATE DATABASE zhisu_hotel;",
    "USE zhisu_hotel;",
    "SHOW TABLES;",
    "SELECT COUNT(*) AS table_count FROM information_schema.tables WHERE table_schema = 'zhisu_hotel';",
    "SELECT * FROM sys_role;",
    "SELECT role_code, HEX(role_name) AS role_name_hex FROM sys_role ORDER BY id;",
    "SELECT * FROM room_type;",
    "SELECT id, HEX(name) AS room_type_name_hex FROM room_type ORDER BY id;",
    "SELECT COUNT(*) AS room_count FROM room;",
    "SELECT room_no, floor FROM room ORDER BY room_no;",
    "SELECT * FROM hotel_info;",
    "SELECT id, HEX(hotel_name) AS hotel_name_hex FROM hotel_info;",
    "SHOW CREATE TABLE sys_role;",
    "SHOW CREATE TABLE room_type;",
    "SHOW CREATE TABLE room;",
    "SHOW CREATE TABLE hotel_info;",
    "SELECT CONSTRAINT_NAME, TABLE_NAME, REFERENCED_TABLE_NAME FROM information_schema.KEY_COLUMN_USAGE WHERE TABLE_SCHEMA = 'zhisu_hotel' AND REFERENCED_TABLE_NAME IS NOT NULL ORDER BY TABLE_NAME, CONSTRAINT_NAME;"
) -join " "
& $mysql --default-character-set=utf8mb4 -u root -p -e $validationSql 2>&1 | Tee-Object -FilePath $log -Append
if ($LASTEXITCODE -ne 0) {
    throw "Validation failed with exit code $LASTEXITCODE. See $log"
}

Write-Host "Database fix and validation completed. Log: $log"
