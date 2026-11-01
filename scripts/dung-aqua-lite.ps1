param([switch]$PrepareOnly, [string]$GradleExecutable)
$ErrorActionPreference = 'Stop'
$root = Split-Path $PSScriptRoot -Parent
Set-Location $root
function Git-Checked([string[]]$GitArgs) {
    & git @GitArgs
    if ($LASTEXITCODE -ne 0) { throw "git failed: $GitArgs" }
}
function Write-Utf8([string]$Path, [string]$Text) {
    [IO.File]::WriteAllText($Path, $Text.Replace("`r`n", "`n"), [Text.UTF8Encoding]::new($false))
}
function Can-Apply([string[]]$GitArgs) {
    $ErrorActionPreference = 'Continue'
    & git @GitArgs 2>$null
    return $LASTEXITCODE -eq 0
}
$pin = (Get-Content app/src/main/assets/upstream-commit.txt -Raw).Trim()
if (!(Test-Path upstream/.git)) {
    Git-Checked @('clone', 'https://github.com/Gathin23/cleanyourtv', 'upstream')
    Git-Checked @('-C', 'upstream', 'checkout', $pin)
}
# Build from an isolated archive: never apply patches, switch branches or rewrite resources in upstream/.
$buildRoot = Join-Path $root ('.gradle/aqua-launcher/' + [guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Force $buildRoot | Out-Null
Git-Checked @('-C', 'upstream', 'archive', '--format=zip', "--output=$buildRoot/source.zip", $pin, 'launcher')
Expand-Archive -LiteralPath "$buildRoot/source.zip" -DestinationPath $buildRoot
Git-Checked @('-C', $buildRoot, 'init', '-q')
foreach ($patch in @('launcher-vgs.patch', 'launcher-aqua-lite.patch')) {
    Git-Checked @('-C', $buildRoot, 'apply', '--check', '--binary', "$root/patches/$patch")
    Git-Checked @('-C', $buildRoot, 'apply', '--binary', "$root/patches/$patch")
}
if ($PrepareOnly) { Write-Output "Pinned source and patches verified in $buildRoot. No APK built."; exit 0 }
if (!(Test-Path "$env:JAVA_HOME/bin/java.exe")) { throw 'Set JAVA_HOME to JDK 17 or 21 (Gradle 8.9).' }
$ErrorActionPreference = 'Continue'
$javaVersion = (& "$env:JAVA_HOME/bin/java.exe" -version 2>&1 | ForEach-Object { $_.ToString() } | Out-String)
$ErrorActionPreference = 'Stop'
if ($javaVersion -notmatch 'version "(17|21)[.\"]') { throw "JDK 17 or 21 required; Android bytecode target stays 17. Found: $javaVersion" }
$sdk = $env:ANDROID_HOME
if (!$sdk) { $sdk = $env:ANDROID_SDK_ROOT }
if (!$sdk -and (Test-Path "$root/local.properties")) {
    $sdkLine = Get-Content "$root/local.properties" | Where-Object { $_ -match '^sdk\.dir=' } | Select-Object -First 1
    if ($sdkLine) { $sdk = $sdkLine.Substring(8).Trim().Replace('\\', '\').Replace('\:', ':') }
}
if (!$sdk -or !(Test-Path "$sdk/platforms/android-34/android.jar")) {
    throw 'Set ANDROID_HOME to an SDK containing platforms;android-34 and build-tools;34.0.0.'
}
if (!(Test-Path "$sdk/build-tools/34.0.0/apksigner.bat")) { throw 'Missing build-tools;34.0.0 (apksigner).' }
# Dedicated generated identity; keep all upstream copyright and font license files.
$launcher = Join-Path $buildRoot 'launcher'
New-Item -ItemType Directory -Force "$launcher/app/src/main/assets" | Out-Null
Copy-Item -LiteralPath "$root/themes/ang-hon-aqua-cosmic-plus/config.json" -Destination "$launcher/app/src/main/assets/aqua-defaults.json"
New-Item -ItemType Directory -Force "$launcher/app/src/main/assets/licenses" | Out-Null
Copy-Item -LiteralPath "$root/LICENSE" -Destination "$launcher/app/src/main/assets/licenses/cleantv-MIT.txt"
Copy-Item -LiteralPath "$root/upstream/LICENSE" -Destination "$launcher/app/src/main/assets/licenses/cleanyourtv-MIT.txt"
Copy-Item -LiteralPath "$launcher/FONT-LICENSE-LEXEND-OFL.txt" -Destination "$launcher/app/src/main/assets/licenses/lexend-OFL.txt"
Write-Utf8 "$launcher/local.properties" ("sdk.dir=" + $sdk.Replace('\', '/') + "`n")
$manifest = "$launcher/app/src/main/AndroidManifest.xml"
$manifestText = [IO.File]::ReadAllText($manifest).Replace('android:label="@string/app_name"', 'android:label="&#258;ng H&#242;n Aqua Lite"')
$manifestText = $manifestText.Replace('android:icon="@mipmap/ic_launcher"', 'android:icon="@drawable/aqua_icon"').Replace('android:banner="@drawable/banner"', 'android:banner="@drawable/aqua_banner"')
Copy-Item -LiteralPath "$root/app/src/aqua/res/drawable/aqua_icon.xml" -Destination "$launcher/app/src/main/res/drawable/aqua_icon.xml"
Copy-Item -LiteralPath "$root/app/src/aqua/res/drawable/aqua_banner.xml" -Destination "$launcher/app/src/main/res/drawable/aqua_banner.xml"
Write-Utf8 $manifest $manifestText
Push-Location $launcher
try {
    $gradleCommand = if ($GradleExecutable) { $GradleExecutable } else { './gradlew.bat' }
    & $gradleCommand --no-daemon --console=plain -g "$root/.gradle/aqua-cache" testDebugUnitTest assembleDebug '-PlauncherApplicationId=vn.anghon.aqua.launcher'
    if ($LASTEXITCODE -ne 0) { throw 'Launcher tests/build failed; assets were not changed.' }
} finally { Pop-Location }
$apk = "$launcher/app/build/outputs/apk/debug/app-debug.apk"
& "$sdk/build-tools/34.0.0/apksigner.bat" verify --verbose --print-certs $apk
if ($LASTEXITCODE -ne 0) { throw 'APK signature verification failed.' }
$badging = (& "$sdk/build-tools/34.0.0/aapt.exe" dump badging $apk | Out-String)
if ($LASTEXITCODE -ne 0 -or $badging -notmatch "package: name='vn.anghon.aqua.launcher'") { throw 'APK package verification failed.' }
$dest = Join-Path $root 'app/src/aqua/assets'
New-Item -ItemType Directory -Force $dest | Out-Null
Copy-Item -LiteralPath $apk -Destination "$dest/tvhome.apk"
$receipt = @(
    'applicationId=vn.anghon.aqua.launcher',
    "buildSourcePath=$($launcher.Replace('\', '/'))",
    "upstreamCommit=$pin",
    "launcherSha256=$((Get-FileHash -LiteralPath $apk -Algorithm SHA256).Hash.ToLowerInvariant())",
    "vgsPatchSha256=$((Get-FileHash -LiteralPath "$root/patches/launcher-vgs.patch" -Algorithm SHA256).Hash.ToLowerInvariant())",
    "aquaPatchSha256=$((Get-FileHash -LiteralPath "$root/patches/launcher-aqua-lite.patch" -Algorithm SHA256).Hash.ToLowerInvariant())",
    "defaultsSha256=$((Get-FileHash -LiteralPath "$root/themes/ang-hon-aqua-cosmic-plus/config.json" -Algorithm SHA256).Hash.ToLowerInvariant())"
)
Write-Utf8 "$dest/aqua-launcher.properties" (($receipt -join "`n") + "`n")
Write-Output "Verified debug launcher: $dest/tvhome.apk"
Write-Output "Launcher test results: $launcher/app/build/test-results/testDebugUnitTest"
Write-Output 'Build phone preview: .\gradlew.bat -PaquaLite=true :app:testDebugUnitTest :app:assembleDebug'
Write-Output 'No device commands were run. Debug certificate is local to this machine; retain it for future updates.'
