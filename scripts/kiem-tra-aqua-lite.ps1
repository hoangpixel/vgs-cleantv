$ErrorActionPreference = 'Stop'
Set-Location (Split-Path $PSScriptRoot -Parent)
$defaults = Get-Content -Encoding UTF8 -Raw themes/ang-hon-aqua-cosmic-plus/config.json | ConvertFrom-Json
if (!$defaults.aqua_cosmic -or !$defaults.aqua_cosmic_plus -or !$defaults.date_below_clock) { throw 'Missing Cosmic Plus/header preset' }
if ($defaults.background_color -ne '#0B2030' -or $defaults.focus_accent_color -ne '#80DEDA') { throw 'Wrong palette' }
if (!$defaults.aqua_galaxy -or $defaults.galaxy_space_color -ne '#07121D' -or $defaults.galaxy_indigo_color -ne '#4256A8') { throw 'Wrong Galaxy palette' }
foreach ($key in @('reduced_motion', 'advanced_effects', 'app_glow', 'dock_glass', 'fade_rows', 'wallpaper')) {
    if ($defaults.$key -ne $false) { throw "Unexpected enabled option in Cinematic preset: $key" }
}
foreach ($key in @('aqua_cinematic', 'cinematic_home', 'animations', 'ambient_glow', 'focus_overshoot', 'cinematic_dots', 'show_greeting', 'lunar_date')) {
    if ($defaults.$key -ne $true) { throw "Missing Cinematic feature: $key" }
}
if ($defaults.sky_intensity -ne 35 -or $defaults.cinematic_art -ne 50 -or $defaults.cinematic_scrim -ne 100 -or $defaults.glass_style -ne 'glass') { throw 'Cinematic baseline changed' }
$catalog = Get-Content -Encoding UTF8 -Raw app/src/main/assets/nang-cao.json | ConvertFrom-Json
if (@($catalog | Where-Object khoa -eq 'focus_accent_color').Count -ne 1) { throw 'Missing or duplicate focus colour setting' }
if (@($catalog.khoa | Select-Object -Unique).Count -ne $catalog.Count) { throw 'Duplicate advanced configuration keys' }
if ($defaults.vgs_file -ne 1 -or $defaults.vgs_theme -ne 'ang-hon-aqua-cosmic-plus') { throw 'Default theme cannot be imported for restore' }
$tvSource = Get-Content -Encoding UTF8 -Raw app/src/main/java/zone/vgs/tvsach/adb/ThaoTacTv.kt
$aquaOnly = [regex]::Match($tvSource, '(?s)fun dayGiaoDienAqua\(.*?(?=\r?\n    fun dayCauHinh\()').Value
if (!$aquaOnly -or $aquaOnly -match 'batLaiGoiLoi|giaHanGac|KHOI_DONG_LAI_HOME|settings put|pm (grant|revoke|enable|disable)|set-home-activity') {
    throw 'Aqua theme path may alter system configuration'
}
if ($aquaOnly -notmatch '\.action\.APPLY_THEME') { throw 'Theme refresh action missing' }
foreach ($path in @('app/src/main/AndroidManifest.xml', 'app/src/aqua/res/drawable/aqua_icon.xml', 'app/src/aqua/res/drawable/aqua_banner.xml')) {
    [xml]$parsed = Get-Content -Encoding UTF8 -Raw $path
    if (!$parsed.DocumentElement) { throw "Invalid XML: $path" }
}
& git diff --exit-code -- app/src/main/assets/tvhome.apk app/src/main/assets/tvhome-xiaomi.apk
if ($LASTEXITCODE -ne 0) { throw 'Original launcher assets changed' }
& git diff --check
if ($LASTEXITCODE -ne 0) { throw 'Whitespace check failed' }
Write-Output 'PASS: palette, original Cinematic motion options, restorable config, configuration-only Aqua path, XML, configuration catalogue, original APK assets, whitespace.'
