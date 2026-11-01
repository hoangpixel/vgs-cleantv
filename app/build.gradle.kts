import java.util.Properties
import java.security.MessageDigest
import java.util.zip.ZipFile

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

// Khoá ký nằm ngoài git (tv-sach.keystore + ky.properties). Thiếu thì bản release ký bằng khoá debug.
val kyFile = rootProject.file("ky.properties")
val ky = Properties().apply { if (kyFile.exists()) kyFile.inputStream().use { load(it) } }
val aquaLite = providers.gradleProperty("aquaLite").orNull == "true"

android {
    // Bản dựng tái tạo được (24/10/2026): khối "dependency info" AGP nhét vào khối ký được mã hoá bằng khoá của Google,
    // mỗi lần dựng ra khác nhau. App không lên Play nên bỏ (F-Droid cũng khuyên vậy).
    dependenciesInfo { includeInApk = false; includeInBundle = false }
    testOptions { unitTests.isReturnDefaultValues = true }
    namespace = "zone.vgs.tvsach"
    compileSdk = 35

    defaultConfig {
        applicationId = if (aquaLite) "vn.anghon.aqua.manager" else "zone.vgs.cleantv"
        buildConfigField("boolean", "AQUA_LITE", aquaLite.toString())
        manifestPlaceholders["appLabel"] = if (aquaLite) "Ăng Hòn Aqua Lite" else "CleanTV"
        manifestPlaceholders["appIcon"] = if (aquaLite) "@drawable/aqua_icon" else "@mipmap/ic_launcher"
        manifestPlaceholders["appBanner"] = if (aquaLite) "@drawable/aqua_banner" else "@drawable/banner_tv"
        minSdk = 26
        targetSdk = 35
        versionCode = if (aquaLite) 115 else 110
        versionName = if (aquaLite) "2.3e" else "4.0"
        ndk { abiFilters += listOf("arm64-v8a", "armeabi-v7a") }
    }

    signingConfigs {
        if (kyFile.exists() && !aquaLite) {
            create("vgs") {
                storeFile = rootProject.file(ky.getProperty("storeFile"))
                storePassword = ky.getProperty("storePassword")
                keyAlias = ky.getProperty("keyAlias")
                keyPassword = ky.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            // Không ghi mã commit git vào APK: kho công khai có lịch sử khác kho nội bộ, dựng lại từ đó sẽ lệch.
            vcsInfo.include = false
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.findByName("vgs") ?: signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures { compose = true; buildConfig = true }
    // APK launcher đóng sẵn: không nén lại để đọc nhanh.
    androidResources { noCompress += "apk" }
    packaging {
        resources { excludes += setOf("META-INF/versions/9/OSGI-INF/MANIFEST.MF", "META-INF/DEPENDENCIES") }
    }
}

// Separate asset overlay: never replace the checked-in CleanTV launcher APKs.
if (aquaLite) {
    android.sourceSets.getByName("debug").assets.srcDir("src/aqua/assets")
    android.sourceSets.getByName("debug").res.srcDir("src/aqua/res")
    // AGP's asset merger does not reliably honour source-set exclude patterns. Feed it a filtered copy.
    val aquaMainAssets = layout.buildDirectory.dir("generated/aquaMainAssets")
    val prepareAquaMainAssets = tasks.register<Sync>("prepareAquaMainAssets") {
        from("src/main/assets")
        exclude("tvhome.apk", "tvhome-xiaomi.apk", "theme/vgs.jpg", "theme/macos.jpg")
        into(aquaMainAssets)
    }
    android.sourceSets.getByName("main").assets.setSrcDirs(listOf(aquaMainAssets))
    val verifyAquaLauncher = tasks.register("verifyAquaLauncher") {
        val apk = file("src/aqua/assets/tvhome.apk")
        val receipt = file("src/aqua/assets/aqua-launcher.properties")
        val sources = mapOf(
            "vgsPatchSha256" to rootProject.file("patches/launcher-vgs.patch"),
            "aquaPatchSha256" to rootProject.file("patches/launcher-aqua-lite.patch"),
            "defaultsSha256" to rootProject.file("themes/ang-hon-aqua-cosmic-plus/config.json"),
        )
        inputs.files(apk, receipt, *sources.values.toTypedArray())
        doLast {
            fun sha(bytes: ByteArray) = MessageDigest.getInstance("SHA-256").digest(bytes)
                .joinToString("") { "%02x".format(it.toInt() and 0xff) }
            check(apk.isFile && receipt.isFile) { "Run scripts/dung-aqua-lite.ps1: a verified Aqua launcher and receipt are required." }
            val meta = Properties().apply { receipt.inputStream().use { load(it) } }
            check(meta.getProperty("applicationId") == "vn.anghon.aqua.launcher") { "Wrong launcher identity." }
            check(meta.getProperty("upstreamCommit") == rootProject.file("app/src/main/assets/upstream-commit.txt").readText().trim()) { "Stale upstream receipt." }
            check(meta.getProperty("launcherSha256") == sha(apk.readBytes())) { "Launcher APK changed since verification." }
            sources.forEach { (key, source) ->
                check(meta.getProperty(key) == sha(source.readBytes())) { "Stale launcher: $key changed. Rebuild it." }
            }
            ZipFile(apk).use { zip ->
                val defaults = zip.getEntry("assets/aqua-defaults.json") ?: error("Old launcher: no Aqua defaults in APK.")
                check(sha(zip.getInputStream(defaults).use { it.readBytes() }) == meta.getProperty("defaultsSha256")) { "APK defaults do not match source." }
                val hasAquaCode = zip.entries().asSequence().filter { it.name.matches(Regex("classes[0-9]*\\.dex")) }.any { dex ->
                    val strings = zip.getInputStream(dex).use { String(it.readBytes(), Charsets.ISO_8859_1) }
                    strings.contains("focus_accent_color") && strings.contains(".action.APPLY_THEME") &&
                        strings.contains("aqua_cinematic") && strings.contains("AquaGreeting") &&
                        strings.contains("AquaCinematic") && strings.contains("AquaSurface") && strings.contains("setRimAccent") &&
                        strings.contains("aqua_galaxy") && strings.contains("galaxy_indigo_color") &&
                        strings.contains("aqua_cosmic") && strings.contains("CosmicDecor") && strings.contains("AquaHeader") &&
                        strings.contains("aqua_cosmic_plus") && strings.contains("CosmicPlusDecor")
                }
                check(hasAquaCode) { "Launcher DEX does not contain the Aqua patch." }
            }
        }
    }
    tasks.configureEach {
        if (name.startsWith("merge") && name.contains("Debug") && name.endsWith("Assets")) {
            dependsOn(verifyAquaLauncher, prepareAquaMainAssets)
        }
        if (name.startsWith("merge") && name.endsWith("Assets")) doFirst {
            check(name.contains("Debug")) { "Aqua Lite phase 1 is a debug preview only." }
        }
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.10.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
    implementation("androidx.work:work-runtime-ktx:2.9.1")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.json:json:20240303")

    // ADB qua Wi-Fi (TCP 5555 và ghép đôi Wireless debugging).
    implementation("com.github.MuntashirAkon:libadb-android:3.1.1")
    implementation("org.conscrypt:conscrypt-android:2.5.3")
    // Tạo chứng chỉ X.509 cho khoá ADB (cùng dòng jdk15to18 mà libadb đã kéo về).
    implementation("org.bouncycastle:bcpkix-jdk15to18:1.81")
}
