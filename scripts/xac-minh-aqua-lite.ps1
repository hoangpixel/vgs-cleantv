param([string]$Sdk, [string]$ManagerApk, [string]$LauncherApk, [string]$PreviousLauncherApk, [string]$PreviousManagerApk)
$ErrorActionPreference = 'Stop'
$root = Split-Path $PSScriptRoot -Parent
Set-Location $root
if (!$Sdk) {
    $line = Get-Content local.properties | Where-Object { $_ -match '^sdk\.dir=' } | Select-Object -First 1
    if (!$line) { throw 'SDK not configured' }
    $Sdk = $line.Substring(8).Trim().Replace('\\', '\').Replace('\:', ':')
}
if (!$ManagerApk) { $ManagerApk = Join-Path $root 'app/build/outputs/apk/debug/app-debug.apk' }
if (!$LauncherApk) { $LauncherApk = Join-Path $root 'app/src/aqua/assets/tvhome.apk' }
$tools = Join-Path $Sdk 'build-tools/35.0.0'
Add-Type -AssemblyName System.IO.Compression.FileSystem
function Hash-Bytes([byte[]]$Bytes) {
    $sha = [Security.Cryptography.SHA256]::Create()
    try { return [BitConverter]::ToString($sha.ComputeHash($Bytes)).Replace('-', '').ToLowerInvariant() }
    finally { $sha.Dispose() }
}
function Read-ZipEntry($Zip, [string]$Name) {
    $entry = $Zip.GetEntry($Name)
    if (!$entry) { throw "Missing ZIP entry: $Name" }
    $inputStream = $entry.Open()
    $memory = [IO.MemoryStream]::new()
    try { $inputStream.CopyTo($memory); return ,$memory.ToArray() }
    finally { $inputStream.Dispose(); $memory.Dispose() }
}
function Inspect-Apk([string]$Path, [string]$ExpectedPackage) {
    if (!(Test-Path -LiteralPath $Path)) { throw "APK not built: $Path" }
    $badging = (& "$tools/aapt.exe" dump badging $Path | Out-String)
    if ($LASTEXITCODE -ne 0) { throw "Cannot read APK: $Path" }
    $package = [regex]::Match($badging, "package: name='([^']+)' versionCode='([0-9]+)'")
    if (!$package.Success -or $package.Groups[1].Value -ne $ExpectedPackage) { throw "Wrong package: $Path" }
    $min = [regex]::Match($badging, "sdkVersion:'([0-9]+)'").Groups[1].Value
    $target = [regex]::Match($badging, "targetSdkVersion:'([0-9]+)'").Groups[1].Value
    if ([int]$min -gt 28) { throw "Not compatible with Android 9: $Path" }
    $signing = (& "$tools/apksigner.bat" verify --min-sdk-version 28 --max-sdk-version 28 --verbose --print-certs $Path | Out-String)
    if ($LASTEXITCODE -ne 0) { throw "Invalid APK signature for Android 9: $Path" }
    $cert = [regex]::Match($signing, 'Signer #1 certificate SHA-256 digest: ([0-9a-f]+)').Groups[1].Value
    if (!$cert) { throw 'Signer certificate missing' }
    $abis = [regex]::Match($badging, 'native-code: ([^\r\n]+)').Groups[1].Value
    return [pscustomobject]@{
        path = (Resolve-Path -LiteralPath $Path).Path
        package = $ExpectedPackage
        versionCode = [int]$package.Groups[2].Value
        versionName = [regex]::Match($badging, "versionName='([^']+)'").Groups[1].Value
        minSdk = [int]$min
        targetSdk = [int]$target
        sha256 = (Get-FileHash -LiteralPath $Path -Algorithm SHA256).Hash.ToLowerInvariant()
        signerSha256 = $cert
        nativeAbis = $abis
        bytes = (Get-Item -LiteralPath $Path).Length
        signatureValidForApi28 = $true
    }
}
$launcher = Inspect-Apk $LauncherApk 'vn.anghon.aqua.launcher'
$manager = Inspect-Apk $ManagerApk 'vn.anghon.aqua.manager'
if ($launcher.versionCode -ne 16 -or $manager.versionCode -ne 115 -or $launcher.versionName -ne '2.3e' -or $manager.versionName -ne '2.3e') { throw 'Expected Aqua Galaxy v2.3e APKs' }
if ($launcher.signerSha256 -ne $manager.signerSha256) { throw 'Aqua signing certificates differ' }
$upgradeCompatible = $null
if ($PreviousLauncherApk -or $PreviousManagerApk) {
    if (!$PreviousLauncherApk -or !$PreviousManagerApk) { throw 'Supply both previous APKs for upgrade verification' }
    $previousLauncher = Inspect-Apk $PreviousLauncherApk 'vn.anghon.aqua.launcher'
    $previousManager = Inspect-Apk $PreviousManagerApk 'vn.anghon.aqua.manager'
    foreach ($pair in @(@($previousLauncher, $launcher), @($previousManager, $manager))) {
        if ($pair[0].versionName -notin @('2.1','2.2','2.3','2.3c') -or $pair[0].versionCode -ge $pair[1].versionCode -or
            $pair[0].signerSha256 -ne $pair[1].signerSha256) { throw 'Incompatible APK metadata for updating from a previous Aqua build' }
    }
    $upgradeCompatible = $true
}
if ($manager.nativeAbis -and !$manager.nativeAbis.Contains("'armeabi-v7a'")) { throw 'Manager is missing ARMv7 native libraries' }
if ($launcher.nativeAbis -and !$launcher.nativeAbis.Contains("'armeabi-v7a'")) { throw 'Launcher is missing ARMv7 native libraries' }
$managerZip = [IO.Compression.ZipFile]::OpenRead($ManagerApk)
$launcherZip = [IO.Compression.ZipFile]::OpenRead($LauncherApk)
try {
    $embeddedHash = Hash-Bytes (Read-ZipEntry $managerZip 'assets/tvhome.apk')
    if ($embeddedHash -ne $launcher.sha256) { throw 'Embedded launcher differs from the verified standalone APK' }
    foreach ($entry in @('assets/theme/vgs.jpg', 'assets/theme/macos.jpg', 'assets/tvhome-xiaomi.apk')) {
        if ($managerZip.GetEntry($entry)) { throw "Unexpected original branded launcher asset: $entry" }
    }
    $defaultsHash = Hash-Bytes (Read-ZipEntry $launcherZip 'assets/aqua-defaults.json')
    $sourceHash = (Get-FileHash themes/ang-hon-aqua-cosmic-plus/config.json).Hash.ToLowerInvariant()
    if ($defaultsHash -ne $sourceHash) { throw 'Packaged launcher defaults do not match source' }
    $defaults = [Text.Encoding]::UTF8.GetString((Read-ZipEntry $launcherZip 'assets/aqua-defaults.json')) | ConvertFrom-Json
    if (!$defaults.aqua_cinematic -or !$defaults.cinematic_home -or !$defaults.animations -or $defaults.reduced_motion) {
        throw 'APK does not contain the Cinematic v2 motion preset'
    }
    if (!$defaults.aqua_galaxy -or !$defaults.aqua_cosmic -or !$defaults.aqua_cosmic_plus -or !$defaults.date_below_clock -or $defaults.vgs_theme -ne 'ang-hon-aqua-cosmic-plus') { throw 'Missing Cosmic Plus v2.3e defaults' }
    $dexText = ($launcherZip.Entries | Where-Object { $_.FullName -match '^classes[0-9]*\.dex$' } | ForEach-Object {
        [Text.Encoding]::GetEncoding(28591).GetString((Read-ZipEntry $launcherZip $_.FullName))
    }) -join ''
    foreach ($marker in @('AquaCinematic', 'AquaGreeting', 'AquaSurface', 'setRimAccent', 'aqua_gradient_color', 'aqua_cinematic', 'aqua_galaxy', 'galaxy_indigo_color', 'aqua_cosmic', 'CosmicDecor', 'AquaHeader', 'aqua_cosmic_plus', 'CosmicPlusDecor')) {
        if (!$dexText.Contains($marker)) { throw "Missing Aqua launcher code in APK: $marker" }
    }
    $managerDex = ($managerZip.Entries | Where-Object { $_.FullName -match '^classes[0-9]*\.dex$' } | ForEach-Object {
        [Text.Encoding]::GetEncoding(28591).GetString((Read-ZipEntry $managerZip $_.FullName))
    }) -join ''
    foreach ($marker in @('ang-hon-aqua-lite', 'ang-hon-aqua-galaxy', 'ang-hon-aqua-cosmic', 'ang-hon-aqua-cosmic-plus', 'aqua_cosmic_plus')) {
        if (!$managerDex.Contains($marker)) { throw "Missing Aqua Manager preset in APK: $marker" }
    }
    foreach ($license in @('assets/licenses/cleantv-MIT.txt', 'assets/licenses/cleanyourtv-MIT.txt', 'assets/licenses/lexend-OFL.txt')) {
        if (!$launcherZip.GetEntry($license)) { throw "Launcher license notice missing: $license" }
    }
    $receiptFile = Join-Path $root 'app/src/aqua/assets/aqua-launcher.properties'
    if ((Hash-Bytes (Read-ZipEntry $managerZip 'assets/aqua-launcher.properties')) -ne (Get-FileHash $receiptFile).Hash.ToLowerInvariant()) {
        throw 'Embedded launcher receipt does not match the build receipt'
    }
    $manifest = (& "$tools/aapt.exe" dump xmltree $LauncherApk AndroidManifest.xml | Out-String)
    if ($LASTEXITCODE -ne 0 -or !$manifest.Contains('vn.anghon.aqua.launcher.theme')) { throw 'Wrong theme provider authority' }
} finally { $managerZip.Dispose(); $launcherZip.Dispose() }
& git diff --exit-code -- app/src/main/assets/tvhome.apk app/src/main/assets/tvhome-xiaomi.apk
if ($LASTEXITCODE -ne 0) { throw 'Original CleanTV launcher assets changed' }
$result = [pscustomobject]@{
    launcher = $launcher
    manager = $manager
    embeddedLauncherSha256 = $embeddedHash
    embeddedLauncherMatches = $true
    defaultsMatchSource = $true
    cinematicV2CodeAndDefaultsPresent = $true
    galaxyCodeAndDefaultsPresent = $true
    cosmicCodeAndDefaultsPresent = $true
    cosmicPlusCodeAndDefaultsPresent = $true
    managerPlusAndLegacyPresetsPresent = $true
    brandedWallpaperAssetsExcluded = $true
    originalLauncherAssetsUnchanged = $true
    launcherLicenseNoticesIncluded = $true
    upgradeFromPreviousMetadataCompatible = $upgradeCompatible
    deviceDeploymentPerformed = $false
}
$json = $result | ConvertTo-Json -Depth 5
[IO.File]::WriteAllText((Join-Path $root '.gradle/aqua-build-verification.json'), $json, [Text.UTF8Encoding]::new($false))
Write-Output $json
