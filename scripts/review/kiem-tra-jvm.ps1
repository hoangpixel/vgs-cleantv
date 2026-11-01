param([Parameter(Mandatory=$true)][string]$GradleLib)
$ErrorActionPreference = 'Stop'
Set-Location (Split-Path (Split-Path $PSScriptRoot -Parent) -Parent)
$out = Join-Path $PWD '.gradle/aqua-review'
New-Item -ItemType Directory -Force "$out/classes", "$out/tmp" | Out-Null
& "$env:JAVA_HOME/bin/javac.exe" -proc:none -encoding UTF-8 --release 17 -cp "$GradleLib/*" -d "$out/classes" scripts/review/SyntaxCheck.java
if ($LASTEXITCODE -ne 0) { throw 'Could not compile syntax checker' }
& "$env:JAVA_HOME/bin/java.exe" "-Djava.io.tmpdir=$out/tmp" -cp "$out/classes;$GradleLib/*" SyntaxCheck app/src/main/java app/src/test/java upstream/launcher/app/src/main/java upstream/launcher/app/src/test/java app/build.gradle.kts
if ($LASTEXITCODE -ne 0) { throw 'Source syntax check failed' }
$names = @('WallpaperSize', 'WallpaperRotation', 'TileRefresh', 'GridNavigation', 'InputFold', 'InputNames', 'TileFit', 'TopFade', 'GlowStyle', 'GlassStyle', 'DockSlots', 'FocusScale')
$sources = @(); $tests = @(); $classes = @()
foreach ($name in $names) {
    $source = "upstream/launcher/app/src/main/java/com/gathin/tvhome/$name.java"
    $test = "upstream/launcher/app/src/test/java/com/gathin/tvhome/${name}Test.java"
    if ((Test-Path $source) -and (Test-Path $test)) {
        $text = [IO.File]::ReadAllText((Join-Path $PWD $source)) + [IO.File]::ReadAllText((Join-Path $PWD $test))
        if ($text -match 'import (android\.|androidx\.|org\.json\.)|\bConfig\.') { continue }
        $sources += $source; $tests += $test; $classes += "com.gathin.tvhome.${name}Test"
    }
}
$junit = (Get-ChildItem -LiteralPath $GradleLib -Recurse -Filter 'junit-4.13.2.jar' | Select-Object -First 1).FullName
$hamcrest = (Get-ChildItem -LiteralPath $GradleLib -Recurse -Filter 'hamcrest-core-1.3.jar' | Select-Object -First 1).FullName
if (!$junit -or !$hamcrest) { throw 'JUnit 4.13.2 / Hamcrest not found in cached Gradle distribution' }
if ($classes.Count -eq 0) { throw 'No SDK-independent tests selected' }
Write-Output ("SDK-independent test classes: " + ($classes -join ', '))
& "$env:JAVA_HOME/bin/javac.exe" -proc:none -encoding UTF-8 --release 17 -cp "$junit;$hamcrest" -d "$out/classes" @sources @tests
if ($LASTEXITCODE -ne 0) { throw 'Pure JVM test compilation failed (no Android stubs are used)' }
& "$env:JAVA_HOME/bin/java.exe" "-Djava.io.tmpdir=$out/tmp" -cp "$out/classes;$junit;$hamcrest" org.junit.runner.JUnitCore @classes
if ($LASTEXITCODE -ne 0) { throw 'Pure JVM tests failed' }
$apksig = "$out/lib/apksig.jar"
if (Test-Path -LiteralPath $apksig) {
    & "$env:JAVA_HOME/bin/javac.exe" -proc:none -encoding UTF-8 --release 17 -cp $apksig -d "$out/classes" scripts/review/VerifyApk.java
    if ($LASTEXITCODE -ne 0) { throw 'APK verifier compilation failed' }
    & "$env:JAVA_HOME/bin/java.exe" "-Djava.io.tmpdir=$out/tmp" -cp "$out/classes;$apksig" VerifyApk app/src/main/assets/tvhome.apk app/src/main/assets/tvhome-xiaomi.apk
    if ($LASTEXITCODE -ne 0) { throw 'Original APK verification failed' }
}
