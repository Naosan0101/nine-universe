# Populates %LOCALAPPDATA%\electron-builder\Cache\winCodeSign\winCodeSign-2.6.0
# from the GitHub tag source zip (no symlinks), so electron-builder skips the
# winCodeSign-2.6.0.7z extract that fails on Windows without symlink privilege.
# See https://github.com/electron-userland/electron-builder/issues/8149

$ErrorActionPreference = 'Stop'

if (-not ($IsWindows -or $env:OS -match 'Windows')) {
    Write-Host 'Skipping winCodeSign cache prep (non-Windows).'
    exit 0
}

$tag = 'winCodeSign-2.6.0'
$base = Join-Path $env:LOCALAPPDATA 'electron-builder\Cache\winCodeSign'
$dest = Join-Path $base $tag
$marker = Join-Path $dest 'rcedit-x64.exe'

if (Test-Path -LiteralPath $marker) {
    Write-Host "winCodeSign cache already present: $dest"
    exit 0
}

$tmpRoot = Join-Path $env:TEMP ("eb-winCodeSign-{0}-{1}" -f $tag, [Guid]::NewGuid().ToString('N'))
$zipPath = Join-Path $tmpRoot "$tag.zip"
New-Item -ItemType Directory -Force -Path $tmpRoot | Out-Null

try {
    $url = "https://github.com/electron-userland/electron-builder-binaries/archive/refs/tags/$tag.zip"
    Write-Host "Downloading $url ..."
    Invoke-WebRequest -Uri $url -OutFile $zipPath -UseBasicParsing

    Write-Host 'Extracting zip...'
    Expand-Archive -LiteralPath $zipPath -DestinationPath $tmpRoot -Force

    $repoRoot = Get-ChildItem -LiteralPath $tmpRoot -Directory -ErrorAction SilentlyContinue |
        Where-Object { $_.Name -like 'electron-builder-binaries-*' } |
        Select-Object -First 1
    if (-not $repoRoot) {
        throw "Could not find extracted electron-builder-binaries-* under $tmpRoot"
    }

    $winCodeSignSrc = Join-Path $repoRoot.FullName 'winCodeSign'
    if (-not (Test-Path -LiteralPath $winCodeSignSrc)) {
        throw "Missing winCodeSign folder: $winCodeSignSrc"
    }

    New-Item -ItemType Directory -Force -Path $dest | Out-Null
    Copy-Item -Path (Join-Path $winCodeSignSrc '*') -Destination $dest -Recurse -Force

    if (-not (Test-Path -LiteralPath $marker)) {
        throw "Cache copy finished but marker missing: $marker"
    }

    Write-Host "winCodeSign cache ready: $dest"
}
finally {
    Remove-Item -LiteralPath $tmpRoot -Recurse -Force -ErrorAction SilentlyContinue
}
