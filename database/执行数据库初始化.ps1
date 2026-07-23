# =========================================================
# 智宿酒店 - 数据库初始化脚本
# =========================================================

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   智宿酒店数据库初始化工具" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 步骤1：查找MySQL安装位置
Write-Host "正在查找MySQL..." -ForegroundColor Yellow

$possiblePaths = @(
    "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe",
    "C:\Program Files\MySQL\MySQL Server 5.7\bin\mysql.exe",
    "C:\Program Files\MySQL\MySQL Server 8.4\bin\mysql.exe",
    "C:\Program Files\MySQL\MySQL Server 8.3\bin\mysql.exe",
    "C:\xampp\mysql\bin\mysql.exe",
    "C:\wamp64\bin\mysql\mysql8.0.31\bin\mysql.exe",
    "C:\wamp64\bin\mysql\mysql5.7.36\bin\mysql.exe"
)

$mysqlPath = $null

foreach ($path in $possiblePaths) {
    if (Test-Path $path) {
        Write-Host "找到MySQL: $path" -ForegroundColor Green
        $mysqlPath = $path
        break
    }
}

# 如果没找到，尝试从PATH中查找
if (-not $mysqlPath) {
    try {
        $mysqlCmd = Get-Command mysql -ErrorAction SilentlyContinue
        if ($mysqlCmd) {
            $mysqlPath = $mysqlCmd.Source
            Write-Host "从PATH中找到MySQL: $mysqlPath" -ForegroundColor Green
        }
    } catch {
        # 忽略错误
    }
}

if (-not $mysqlPath) {
    Write-Host ""
    Write-Host "❌ 未找到MySQL命令行工具！" -ForegroundColor Red
    Write-Host ""
    Write-Host "请尝试以下方法之一：" -ForegroundColor Yellow
    Write-Host "1. 使用MySQL Workbench打开并执行文件: " -ForegroundColor White
    Write-Host "   database\complete_single_file.sql" -ForegroundColor Cyan
    Write-Host "2. 使用其他MySQL管理工具（Navicat、phpMyAdmin等）" -ForegroundColor White
    Write-Host ""
    Read-Host "按回车键退出"
    exit 1
}

# 步骤2：执行SQL脚本
Write-Host ""
Write-Host "正在执行数据库初始化..." -ForegroundColor Yellow

$sqlFile = Join-Path $PSScriptRoot "complete_single_file.sql"

if (-not (Test-Path $sqlFile)) {
    Write-Host "❌ 找不到SQL文件: $sqlFile" -ForegroundColor Red
    Read-Host "按回车键退出"
    exit 1
}

try {
    $env:MYSQL_PWD = "root"
    & $mysqlPath --default-character-set=utf8mb4 -u root -proot < $sqlFile
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host ""
        Write-Host "✅ 数据库初始化完成！" -ForegroundColor Green
        Write-Host ""
        Write-Host "测试账号信息：" -ForegroundColor Cyan
        Write-Host "  管理员: admin / admin123" -ForegroundColor White
        Write-Host "  前台: frontdesk / frontdesk123" -ForegroundColor White
        Write-Host "  顾客: guest / guest123" -ForegroundColor White
    } else {
        Write-Host ""
        Write-Host "❌ 执行SQL时出现错误（可能是连接失败或权限问题）" -ForegroundColor Red
        Write-Host ""
        Write-Host "请尝试手动执行以下操作：" -ForegroundColor Yellow
        Write-Host "1. 使用MySQL Workbench或其他管理工具" -ForegroundColor White
        Write-Host "2. 打开文件: database\complete_single_file.sql" -ForegroundColor Cyan
        Write-Host "3. 连接到本地MySQL（用户名root，密码root）" -ForegroundColor White
        Write-Host "4. 执行整个SQL脚本" -ForegroundColor White
    }
} catch {
    Write-Host ""
    Write-Host "❌ 执行过程中发生错误: $_" -ForegroundColor Red
    Write-Host ""
    Write-Host "请尝试手动执行SQL脚本" -ForegroundColor Yellow
}

Write-Host ""
Read-Host "按回车键退出"
