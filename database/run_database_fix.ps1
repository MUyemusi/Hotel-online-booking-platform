param(
    [string]$Database = "zhisu_hotel",
    [string]$MysqlExe = "mysql",
    [string]$MysqlDumpExe = "mysqldump",
    [string]$HostName = "localhost",
    [int]$Port = 3306,
    [string]$Username = "root"
)
$ErrorActionPreference = "Stop"
if ($Database -notmatch '^[A-Za-z0-9_]+$') { throw "Database name contains unsupported characters." }
if (-not $env:DB_PASSWORD) { throw "Set DB_PASSWORD before running this script." }

$root = Split-Path -Parent $PSScriptRoot
$databaseDir = Join-Path $root "database"
$backup = Join-Path $databaseDir ("backup_before_database_fix_{0}.sql" -f (Get-Date -Format "yyyyMMdd_HHmmss"))
$temp = Join-Path ([IO.Path]::GetTempPath()) ("zhisu-migrate-" + [guid]::NewGuid().ToString("N") + ".sql")
$files = @(
    "database/modules/member5_pricing_content.sql",
    "database/migrations/member5_completion.sql",
    "database/migrations/full_completion.sql",
    "database/03_indexes_constraints.sql",
    "database/validate_schema.sql"
)

try {
    $env:MYSQL_PWD = $env:DB_PASSWORD
    & $MysqlDumpExe --default-character-set=utf8mb4 -h $HostName -P $Port -u $Username --result-file=$backup $Database
    if ($LASTEXITCODE -ne 0) { throw "Database backup failed with exit code $LASTEXITCODE" }
    if (-not (Test-Path -LiteralPath $backup) -or (Get-Item -LiteralPath $backup).Length -le 0) { throw "Database backup is empty." }

    $builder = [Text.StringBuilder]::new()
    [void]$builder.AppendLine("SET NAMES utf8mb4;")
    [void]$builder.AppendLine("USE $Database;")
    foreach ($relative in $files) {
        $sql = Get-Content -Raw -Encoding UTF8 (Join-Path $root $relative)
        $sql = $sql.Replace("USE zhisu_hotel;", "USE $Database;")
        $sql = $sql.Replace("zhisu_hotel.", $Database + ".")
        [void]$builder.AppendLine($sql)
    }
    [IO.File]::WriteAllText($temp, $builder.ToString(), [Text.UTF8Encoding]::new($false))
    $sourcePath = $temp.Replace([IO.Path]::DirectorySeparatorChar, '/')
    & $MysqlExe --default-character-set=utf8mb4 -h $HostName -P $Port -u $Username -e "source $sourcePath"
    if ($LASTEXITCODE -ne 0) { throw "Database migration failed with exit code $LASTEXITCODE" }
    Write-Host "Migration and validation completed. Backup: $backup"
} finally {
    Remove-Item -LiteralPath $temp -Force -ErrorAction SilentlyContinue
    Remove-Item Env:MYSQL_PWD -ErrorAction SilentlyContinue
}