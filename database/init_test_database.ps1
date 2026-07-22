param(
    [string]$Database = "zhisu_hotel_test",
    [string]$MysqlExe = "mysql",
    [string]$HostName = "localhost",
    [int]$Port = 3306,
    [string]$Username = "root"
)
$ErrorActionPreference = "Stop"
if ($Database -notmatch '^[A-Za-z0-9_]+_test$') { throw "For safety, database name must end with _test." }
if (-not $env:DB_PASSWORD) { throw "Set DB_PASSWORD before running this script." }
$root = Split-Path -Parent $PSScriptRoot
$files = @(
 "database/01_base_tables.sql",
 "database/02_business_tables.sql",
 "database/modules/member5_pricing_content.sql",
 "database/migrations/member5_completion.sql",
 "database/migrations/full_completion.sql",
 "database/03_indexes_constraints.sql",
 "database/04_initial_data.sql",
 "database/05_test_data.sql",
 "database/validate_schema.sql"
)
$temp = Join-Path ([IO.Path]::GetTempPath()) ("zhisu-init-" + [guid]::NewGuid().ToString("N") + ".sql")
try {
    $builder = [Text.StringBuilder]::new()
    [void]$builder.AppendLine("SET NAMES utf8mb4;")
    [void]$builder.AppendLine("DROP DATABASE IF EXISTS $Database;")
    [void]$builder.AppendLine("CREATE DATABASE $Database CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;")
    [void]$builder.AppendLine("USE $Database;")
    foreach ($relative in $files) {
        $content = Get-Content -Raw -Encoding UTF8 (Join-Path $root $relative)
        $content = $content.Replace("USE zhisu_hotel;", "")
        $content = $content.Replace("zhisu_hotel.", $Database + ".")
        [void]$builder.AppendLine($content)
    }
    [IO.File]::WriteAllText($temp,$builder.ToString(),[Text.UTF8Encoding]::new($false))
    $env:MYSQL_PWD=$env:DB_PASSWORD
    $sourcePath=$temp.Replace([IO.Path]::DirectorySeparatorChar, '/')
    & $MysqlExe --default-character-set=utf8mb4 -h $HostName -P $Port -u $Username -e "source $sourcePath"
    if($LASTEXITCODE -ne 0){throw "MySQL initialization failed with exit code $LASTEXITCODE"}
} finally {
    Remove-Item -LiteralPath $temp -Force -ErrorAction SilentlyContinue
    Remove-Item Env:MYSQL_PWD -ErrorAction SilentlyContinue
}
