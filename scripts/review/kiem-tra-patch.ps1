param([string]$LauncherSource)
$ErrorActionPreference = 'Stop'
$root = Split-Path (Split-Path $PSScriptRoot -Parent) -Parent
Set-Location $root
$pin = (Get-Content -Raw app/src/main/assets/upstream-commit.txt).Trim()
$target = Join-Path $root ('.gradle/aqua-review/baseline-' + [guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Force $target | Out-Null
& git -C upstream archive --format=zip "--output=$target/source.zip" $pin launcher
if ($LASTEXITCODE -ne 0) { throw 'Cannot archive pinned launcher' }
Expand-Archive -LiteralPath "$target/source.zip" -DestinationPath $target
& git -C $target init -q
if ($LASTEXITCODE -ne 0) { throw 'Cannot initialize isolated verification tree' }
foreach ($patch in @('launcher-vgs.patch', 'launcher-aqua-lite.patch')) {
    & git -C $target apply --binary "$root/patches/$patch"
    if ($LASTEXITCODE -ne 0) { throw "Patch failed on clean pinned source: $patch" }
    if ($patch -eq 'launcher-vgs.patch') {
        $motionFiles = @('main/java/com/gathin/tvhome/AppAdapter.java', 'main/java/com/gathin/tvhome/Motion.java',
            'main/java/com/gathin/tvhome/HeroArt.java', 'main/java/com/gathin/tvhome/DockDecoration.java',
            'main/java/com/gathin/tvhome/GridNavigation.java',
            'main/java/com/gathin/tvhome/WallpaperPlayer.java', 'main/res/layout/activity_main.xml',
            'main/res/layout-iw/activity_main.xml', 'main/res/anim/tile_enter.xml')
        $motionHashes = @{}
        foreach ($relative in $motionFiles) {
            $motionHashes[$relative] = (Get-FileHash -LiteralPath (Join-Path "$target/launcher/app/src" $relative)).Hash
        }
        $originalMain = [IO.File]::ReadAllText("$target/launcher/app/src/main/java/com/gathin/tvhome/MainActivity.java")
        $originalGlass = [IO.File]::ReadAllText("$target/launcher/app/src/main/java/com/gathin/tvhome/GlassView.java")
    }
}
# The Aqua patch must reuse the CleanTV cinematic engine, not merely resemble its animation settings.
foreach ($relative in $motionFiles) {
    if ((Get-FileHash -LiteralPath (Join-Path "$target/launcher/app/src" $relative)).Hash -ne $motionHashes[$relative]) {
        throw "Cinematic engine/layout was changed: $relative"
    }
}
function Method-Text([string]$Source, [string]$Name) {
    $Source = $Source.Replace("`r`n", "`n")
    $method = [regex]::Match($Source, '(?m)^    (?:(?:private|public|protected) )?(?:void|boolean|Drawable) ' + [regex]::Escape($Name) + '\([^\r\n]*\) \{')
    if (!$method.Success) { throw "Method missing: $Name" }
    $start = $Source.IndexOf('{', $method.Index)
    $depth = 0
    for ($i = $start; $i -lt $Source.Length; $i++) {
        if ($Source[$i] -eq '{') { $depth++ }
        if ($Source[$i] -eq '}') { $depth--; if ($depth -eq 0) { return $Source.Substring($method.Index, $i - $method.Index + 1) } }
    }
    throw "Unbalanced method: $Name"
}
$aquaMain = [IO.File]::ReadAllText("$target/launcher/app/src/main/java/com/gathin/tvhome/MainActivity.java")
$normalizedMain = $aquaMain.Replace("`r`n", "`n")
$headerHook = "        if (cfg.dateBelowClock || cfg.aquaCosmic) moveDateBelowClock();`n        if (cfg.aquaCosmic) AquaHeader.apply(this);"
if (!$normalizedMain.Contains($headerHook)) { throw 'Cosmic header must be opt-in and follow the existing date reflow' }
$headerSource = [IO.File]::ReadAllText("$target/launcher/app/src/main/java/com/gathin/tvhome/AquaHeader.java")
if ($headerSource.IndexOf('column.removeView(greeting)') -ge $headerSource.IndexOf('column.addView(greeting, lp)') -or
    $headerSource -notmatch 'setAutoSizeTextTypeUniformWithConfiguration\(11, 12' -or
    $headerSource -notmatch 'setAlpha\(0.82f\)') { throw 'Unexpected cosmic greeting layout' }
$configSource = [IO.File]::ReadAllText("$target/launcher/app/src/main/java/com/gathin/tvhome/Config.java")
$cinematicSource = [IO.File]::ReadAllText("$target/launcher/app/src/main/java/com/gathin/tvhome/AquaCinematic.java")
$surfaceSource = [IO.File]::ReadAllText("$target/launcher/app/src/main/java/com/gathin/tvhome/AquaSurface.java")
if (!$configSource.Contains('result.aquaCosmicPlus = result.aquaCosmic && bool(json, "aqua_cosmic_plus", false);') -or
    !$cinematicSource.Contains('if (config.aquaCosmicPlus) surface.withCosmicPlusDecor();') -or
    !$cinematicSource.Contains('else if (config.aquaCosmic) surface.withCosmicDecor();') -or
    !$surfaceSource.Contains('if (cosmicPlus) CosmicPlusDecor.paint(pixels, rtl, aqua);') -or
    !$surfaceSource.Contains('else if (cosmic) CosmicDecor.paint(pixels, rtl, aqua);')) { throw 'Cosmic Plus opt-in/legacy renderer routing changed' }
$motionMethods = @('dispatchKeyEvent', 'onTileFocused', 'onTileVerticalKey', 'focusTile', 'refitGlass', 'showHero',
    'fillHero', 'setUpCinematicShelf', 'showHeroArt', 'fadeHeroArt', 'updateHeroMeta', 'updateHeroDots',
    'setAmbient', 'transitionAmbient', 'showGlowOnly', 'hideHero', 'hideGlass', 'reloadApps', 'reloadTiles')
foreach ($name in $motionMethods) {
    if ((Method-Text $originalMain $name) -cne (Method-Text $aquaMain $name)) { throw "Cinematic motion/navigation changed: $name" }
}
# Only scrim selection may differ in MainActivity's cinematic renderer. Everything after the new guard is original.
$aquaScrim = Method-Text $aquaMain 'scrimDrawable'
$scrimGuard = '        if (aquaCinematic) return aquaDesign.scrim(getResources(), getResources().getBoolean(R.bool.rtl_menu_bar));' + "`n"
if (!$aquaScrim.Contains($scrimGuard) -or $aquaScrim.Replace($scrimGuard, '') -cne (Method-Text $originalMain 'scrimDrawable')) {
    throw 'Unexpected change outside Aqua scrim selection'
}
$aquaGlass = [IO.File]::ReadAllText("$target/launcher/app/src/main/java/com/gathin/tvhome/GlassView.java")
foreach ($name in @('onDraw', 'onSizeChanged', 'setStyle', 'setRadius')) {
    if ((Method-Text $originalGlass $name) -cne (Method-Text $aquaGlass $name)) { throw "Glass geometry/rendering changed: $name" }
}
$rimOverride = '        rim.setColor(AquaSurface.rimColor(rimAccent, rim.getColor()));' + "`n"
if ((Method-Text $aquaGlass 'setAccent').Replace($rimOverride, '') -cne (Method-Text $originalGlass 'setAccent')) {
    throw 'Glass accent changed beyond the fixed rim colour'
}
foreach ($layout in @('main/res/layout/activity_main.xml', 'main/res/layout-iw/activity_main.xml')) {
    [xml]$xml = Get-Content -Raw (Join-Path "$target/launcher/app/src" $layout)
    $ids = @($xml.DocumentElement.ChildNodes | Where-Object { $_.NodeType -eq 'Element' } | ForEach-Object { $_.GetAttribute('id', 'http://schemas.android.com/apk/res/android') })
    if ($ids.IndexOf('@+id/heroArt') -ge $ids.IndexOf('@+id/heroScrim') -or
        $ids.IndexOf('@+id/ambient') -ge $ids.IndexOf('@+id/heroScrim') -or
        $ids.IndexOf('@+id/heroScrim') -ge $ids.IndexOf('@+id/hero')) { throw "Wrong Aqua composition order: $layout" }
}
$expectedRoot = Join-Path $target 'launcher/app/src'
if (!$LauncherSource) {
    $receipt = Join-Path $root 'app/src/aqua/assets/aqua-launcher.properties'
    $sourceLine = Get-Content -LiteralPath $receipt | Where-Object { $_ -like 'buildSourcePath=*' } | Select-Object -First 1
    if (!$sourceLine) { throw 'Build Aqua first, or pass -LauncherSource pointing to the isolated launcher source.' }
    $LauncherSource = $sourceLine.Substring('buildSourcePath='.Length)
}
$actualRoot = Join-Path $LauncherSource 'app/src'
if ([IO.File]::ReadAllText("$target/launcher/app/build.gradle").Replace("`r`n", "`n") -cne
    [IO.File]::ReadAllText("$LauncherSource/app/build.gradle").Replace("`r`n", "`n")) { throw 'Launcher Gradle source differs from patch' }
$count = 0
foreach ($file in Get-ChildItem -LiteralPath $expectedRoot -Recurse -File) {
    $relative = $file.FullName.Substring($expectedRoot.Length + 1)
    $actual = Join-Path $actualRoot $relative
    if (!(Test-Path -LiteralPath $actual)) { throw "Missing patched source: $relative" }
    if ($file.Extension -in @('.java', '.xml', '.txt', '.json')) {
        $a = [IO.File]::ReadAllText($file.FullName).Replace("`r`n", "`n")
        $b = [IO.File]::ReadAllText($actual).Replace("`r`n", "`n")
        if ($relative.Replace('\', '/') -eq 'main/AndroidManifest.xml') {
            $branded = $a.Replace('android:label="@string/app_name"', 'android:label="&#258;ng H&#242;n Aqua Lite"').Replace('android:icon="@mipmap/ic_launcher"', 'android:icon="@drawable/aqua_icon"').Replace('android:banner="@drawable/banner"', 'android:banner="@drawable/aqua_banner"')
            if ($b -ceq $branded) { $a = $branded }
        }
        if ($a -cne $b) { throw "Unexpected source change: $relative" }
    } elseif ((Get-FileHash -LiteralPath $file.FullName).Hash -ne (Get-FileHash -LiteralPath $actual).Hash) {
        throw "Unexpected binary source change: $relative"
    }
    $count++
}
# Only these files are generated later by the Aqua build script.
$generated = @('main/assets/aqua-defaults.json', 'main/res/drawable/aqua_icon.xml', 'main/res/drawable/aqua_banner.xml',
    'main/assets/licenses/cleantv-MIT.txt', 'main/assets/licenses/cleanyourtv-MIT.txt', 'main/assets/licenses/lexend-OFL.txt')
foreach ($file in Get-ChildItem -LiteralPath $actualRoot -Recurse -File) {
    $relative = $file.FullName.Substring($actualRoot.Length + 1).Replace('\', '/')
    if (!(Test-Path -LiteralPath (Join-Path $expectedRoot $relative)) -and $relative -notin $generated) {
        throw "Unexpected extra source file: $relative"
    }
}
Write-Output "PASS: both patches apply to clean commit $pin; $count source/resource files match isolated launcher $LauncherSource."
Write-Output "PASS: $($motionFiles.Count) cinematic engine/layout files and $($motionMethods.Count) MainActivity motion/navigation methods remain identical to CleanTV after VGS patch."
Write-Output 'PASS: Glass drawing/geometry unchanged; only rim colour overridden; Aqua uses the existing scrim above artwork and below content.'
Write-Output 'PASS: Cosmic Plus requires the new opt-in flag and preserves the legacy cosmic renderer branch.'
