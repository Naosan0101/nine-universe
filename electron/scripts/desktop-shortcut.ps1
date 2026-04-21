# Creates "Nine Universe.lnk" on the desktop pointing to win-unpacked exe.
# Run from electron folder after: npm run dist:dir

$ErrorActionPreference = "Stop"
$electronRoot = Split-Path $PSScriptRoot -Parent
$exe = Join-Path $electronRoot "dist\win-unpacked\Nine Universe.exe"

if (-not (Test-Path -LiteralPath $exe)) {
	Write-Host "Missing: $exe" -ForegroundColor Red
	Write-Host "Run: npm run dist:dir (in electron folder) first." -ForegroundColor Yellow
	exit 1
}

$desktop = [Environment]::GetFolderPath("Desktop")
if ([string]::IsNullOrEmpty($desktop)) {
	$desktop = Join-Path $env:USERPROFILE "Desktop"
}
$lnkPath = Join-Path $desktop "Nine Universe.lnk"

$shell = New-Object -ComObject WScript.Shell
$shortcut = $shell.CreateShortcut($lnkPath)
$shortcut.TargetPath = (Resolve-Path -LiteralPath $exe).Path
$shortcut.WorkingDirectory = Split-Path -LiteralPath $exe
$shortcut.Description = "Nine Universe"
$shortcut.Save()

Write-Host "Desktop shortcut created: $lnkPath" -ForegroundColor Green
