# Ăng Hòn Aqua Galaxy — Cinematic v2.2

01/11/2026, Asia/Bangkok. **READY FOR TESTING**. Hoàn tất thiết kế texture, xem preview từ renderer, build hai APK và xác minh đóng gói/cập nhật. Chưa chạy v2.2 trên TCL, chưa xác nhận FPS hoặc cold start không đổi.

Không dùng ADB, cài APK, đổi Home, gỡ CleanTV/SmartTube, debloat, reset/clean Git, commit hoặc push. Không thêm dependency, quyền, service hoặc timer. Giữ các thay đổi Git có sẵn từ các phiên trước.

## 1. Visual và cách tạo tinh vân

V2.1 là quầng aqua rộng ở góc phải. V2.2 có nền không gian tối, dải teal–aqua cong chạy chéo lên phía phải, lõi sáng mảnh và một nếp mây mềm đi cùng; mây indigo phía phải dưới dải sáng tạo chiều sâu. Không phủ màn hình bằng cyan hoặc tạo một LinearGradient đơn sắc.

Palette: Deep Space `#07121D`, Midnight Navy `#0B2030`, Dark Teal `#123F50`, Ocean Teal `#1A6875`, Aqua Glow `#52C6C6`, Aqua Highlight `#80DEDA`, Galaxy Indigo `#4256A8`, chữ trắng `#FFFFFF`.

`AquaSurface.galaxy()` bake procedural texture ARGB: đường cong sin nhẹ, nhiều falloff mềm `(1-d²)³`, các vùng mây cục bộ, ribbon rộng và lõi sáng hẹp. Indigo chỉ tập trung phía phải dưới dải aqua. Giữ shadow navy phía chữ, trên toolbar và dưới dock. Thêm dither luminance cố định **−1/0/+1 LSB**, vẽ sẵn vào texture để giảm vệt lượng tử màu. Không random theo thời gian, sao bay, shimmer, blur runtime hoặc shader animation.

Preview đầu còn giống một tia sáng đơn; đã nới cloud, thêm nếp mây và giảm lõi trước khi chốt. PNG cuối không có banding lớn thấy rõ; vẫn phải xem banding trên panel TCL khi upscale.

## 2. Layer và hiệu năng

Giữ pipeline v2.1:

```text
root / wallpaper → Sky → artwork app → ambient
→ heroScrim chứa Galaxy → chữ / hero / toolbar / dock / grid → focus glass
```

Galaxy không quay về dưới artwork. Art opacity 50%, scrim strength 100% giữ nguyên; artwork/ambient vẫn đổi theo app qua animation cũ, nhìn qua alpha của scrim. Gradient ở trên nên không bị artwork che hết. Không đổi View, vị trí, kích thước hay thứ tự nội dung.

Bitmap vẫn **480×270 ARGB_8888 = 518.400 byte (~0,49 MiB)**. Một BitmapDrawable cache theo Activity, cùng số texture/lượt vẽ như v2.1; mảng pixel tạm cùng kích thước chỉ dùng lúc tạo texture. Không tạo bitmap khi focus hoặc chuyển app. Sin tính hai lần mỗi cột (960 lần cho cả texture), không ở mỗi frame.

Lượt host tham khảo: Galaxy chuẩn bị ~25,59ms, v2.1 ~11,50ms. Đây **không phải benchmark ARMv7 hoặc frame time**. Phần bake có thêm chi phí một lần; không có phép tính tinh vân trong onDraw/D-pad/spring/minuteTick. Cần đo cold start/recreate, RAM PSS và frame time thực trên TCL để kết luận không regression.

## 3. Giữ chuyển động, preset cũ và lời chào

So với v2.1, **251 file source/resource kế thừa không thay đổi**, gồm toàn bộ `MainActivity`, `GlassView`, lời chào, font và layout. Chỉ ba file Java production launcher thay đổi: AquaSurface, AquaCinematic, Config. Không đổi spring, duration, interpolator, focus scale/translation, RecyclerView scrolling, dock hoặc remote handling.

Review từ commit sạch sau VGS xác nhận **9 file engine/layout và 19 phương thức MainActivity** không đổi. Hai patch áp thành công trên archive commit `fe9ad3204b945d3aadfb6876ee6ea78c20a1fe73`; **250 file source/resource** và Gradle launcher khớp cây build. Không áp patch lên upstream hiện có.

Lời chào giữ giờ/múi giờ TV, bốn buổi, receiver đổi giờ/timezone, resume, tên chỉnh từ Manager, font Lexend và dấu tiếng Việt. Rim focus/vạch đồng hồ vẫn aqua, glow theo app như v2.1. Không thêm animation chữ.

Thêm preset riêng **Ăng Hòn Aqua Galaxy — Cinematic v2.2**, ID `ang-hon-aqua-galaxy`. Giữ nguyên preset `ang-hon-aqua-lite` v2.1 và file defaults cũ. Galaxy chỉ bật khi cờ `aqua_galaxy` cùng các cờ Cinematic cho phép; thiếu cờ thì dùng renderer v2.1.

Sáu khóa màu riêng: `galaxy_space_color`, `galaxy_navy_color`, `galaxy_dark_teal_color`, `galaxy_ocean_color`, `galaxy_glow_color`, `galaxy_indigo_color`. Hai màu gradient v2.1 không quyết định palette Galaxy; không xóa các override đã lưu. Focus vẫn theo `focus_accent_color`. Màu Galaxy có trong Nâng cao và dùng JSON/provider/export/import hiện có, không cần build lại khi đổi màu/tên/ảnh tĩnh.

**Upgrade không tự ghi đè config TV.** Sau khi người dùng chủ động cập nhật hai APK, chọn/áp preset Galaxy để bật nền mới. Kết nối lại vẫn nhận preset cũ đã lưu; chọn v2.1 để quay về look cũ. Các override motion/scrim vẫn có quyền ưu tiên nếu người dùng tự đặt. Không bật reduced motion hoặc tắt animation trong defaults mới.

## 4. Preview đúng pixel renderer

Ảnh: [galaxy-comparison.png](../.gradle/aqua-v22-review/galaxy-comparison.png). Đã tạo, xem/điều chỉnh trước build, rồi render lại từ source launcher thực sự build. Cột trái v2.1, phải v2.2:

- **A — Idle:** underlay navy, không artwork; dải tinh vân khác rõ quầng aqua cũ.
- **B — Artwork:** underlay đỏ→xanh mô phỏng artwork/ambient đã phối; Galaxy vẫn rõ và còn phản ứng với màu phía dưới.
- **C — Focus:** cùng underlay, thêm chữ/mẫu rim aqua; tên app trên vùng tối và rim rõ.

Texture/compositing dùng pixel production. Underlay, vị trí chữ và mẫu card là dữ liệu/geometry tham chiếu, không phải ảnh UI Android. Ảnh ghi **NOT A TV SCREENSHOT**. Không dùng preview để chứng minh FPS hoặc remote. Các điểm mẫu chữ trái/dock trên nền trắng đạt **≥7:1**, toolbar **≥4,5:1** với palette mặc định; không bảo đảm mọi màu tùy chỉnh.

Render v2.1 từ code mới khớp hash bản cũ:

```text
ARGB texture: 2e7c7a6dfc1694216a2be507312ed20dabd8e8644bf8737f20b73893b6d981c8
PNG v2.1:    1779a1113bc19003c27b59edb10409d20a640834816820360f4eb19ab9d0d074
```

Tái tạo preview từ root:

```powershell
$receipt = Get-Content app/src/aqua/assets/aqua-launcher.properties
$launcherSource = ($receipt | Where-Object { $_ -like 'buildSourcePath=*' }).Substring(16)
New-Item -ItemType Directory -Force .gradle/aqua-v22-review/classes | Out-Null
& "$env:JAVA_HOME/bin/javac.exe" -encoding UTF-8 --release 17 -d .gradle/aqua-v22-review/classes "$launcherSource/app/src/main/java/com/gathin/tvhome/AquaSurface.java" scripts/review/RenderAquaGalaxy.java
& "$env:JAVA_HOME/bin/java.exe" '-Djava.awt.headless=true' -cp .gradle/aqua-v22-review/classes com.gathin.tvhome.RenderAquaGalaxy .gradle/aqua-v22-review/galaxy-comparison.png "$launcherSource/app/src/main/res/font/lexend.ttf"
```

## 5. Build và test thực tế

Gradle 8.9, runtime JDK 21.0.12, bytecode 17. SDK `C:\Users\mhoang\AppData\Local\Android\Sdk`; API/Build-Tools 34 launcher, 35 Manager; AGP 8.5.2/8.7.2. Online, không `--offline`, không thêm dependency.

```powershell
$gradleBuild = "$env:USERPROFILE/.gradle/wrapper/dists/gradle-8.9-bin/90cnw93cvbtalezasaz0blq0a/gradle-8.9/bin/gradle.bat"
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/dung-aqua-lite.ps1 -GradleExecutable $gradleBuild
& $gradleBuild --no-daemon --console=plain -g .gradle/aqua-cache -PaquaLite=true testDebugUnitTest :app:assembleDebug
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/review/kiem-tra-patch.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/kiem-tra-aqua-lite.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/xac-minh-aqua-lite.ps1 -PreviousLauncherApk .gradle/aqua-v22-review/launcher-v21.apk -PreviousManagerApk .gradle/aqua-v22-review/manager-v21.apk
git diff --check
```

Launcher **BUILD SUCCESSFUL (48s)**, Manager **BUILD SUCCESSFUL (59s)**. Source/patch/config/whitespace/APK verification PASS.

| Module | Suite | Test | Fail/error/skip |
| --- | --- | --- | --- |
| Launcher | 47 | 327 | 0/0/0 |
| Manager | 10 | 40 | 0/0/0 |
| khoa | 1 | 7 | 0/0/0 |
| Tổng | 58 | **374** | **0/0/0** |

GalaxySurfaceTest 5 test: hash v2.1, idle/artwork, indigo cục bộ, contrast, dither/alpha/RTL/determinism. AquaGalaxyTest 4 test: defaults/Manager/import, preset/motion cũ, màu cũ tách khỏi Galaxy, màu qua catalog/export/import. AquaSurfaceTest 9, AquaGreetingTest 4, AquaLiteTest 7, KichBanKhoiPhucTest 4 vẫn PASS. Khoa không đổi, Gradle UP-TO-DATE. Không cộng lặp nhiều lượt test.

Hai thử nghiệm javac/JUnit phụ không nạp được classpath `org.junit`; không sửa dependency để né lỗi. Gradle đầy đủ đã biên dịch/chạy tất cả test thành công, là kết quả chính thức. Render Java desktop compile/chạy được. Các warning SDK XML v4/v3, deprecated API và trailing whitespace giấy phép VGS không chặn build; không sửa giấy phép chỉ để bỏ warning. Không chạy lint toàn bộ, instrumentation hoặc benchmark TV.

Source launcher: `.gradle/aqua-launcher/12c27e6473074be0b62720f92945417d/launcher/`; test HTML trong `app/build/reports/tests/testDebugUnitTest/` của cây này. Manager/khoa có report tương ứng dưới module project chính.

## 6. APK và cập nhật từ v2.1

Root: `C:\Users\mhoang\OneDrive\Desktop\vgs-cleantv`.

| APK | Đường dẫn | Package | Version/code | minSdk/target | Byte |
| --- | --- | --- | --- | --- | --- |
| Launcher | `app/src/aqua/assets/tvhome.apk` | `vn.anghon.aqua.launcher` | 2.2 / 13 | 26 / 34 | 2.278.602 |
| Manager | `app/build/outputs/apk/debug/app-debug.apk` | `vn.anghon.aqua.manager` | 2.2 / 112 | 26 / 35 | 28.860.602 |

```text
Launcher SHA-256 f7ba98dde9f310ad329c35574164c5794e657659bd1df1fcc6a94687f2536922
Manager  SHA-256 fd9b8c8e16b5a26eb5fdce2c1acc1afde2653e74c6b71e72b04e82a05f1d9702
Certificate     5b6ffaad8776e93270563667d2b0c28349de8e528d1fdb5576977c158388f486
```

Chữ ký hợp lệ API 28. So với hai APK v2.1 giữ trước task: applicationId/certificate giống nhau, versionCode tăng **12→13**, **111→112**. Đáp ứng điều kiện metadata cho cập nhật `adb install -r` giữ dữ liệu; **không chạy lệnh này và chưa kiểm chứng cài trên TCL**. Launcher không native libs; Manager có ARMv7/ARM64. Không đổi key, minSdk hoặc quyền để ép cập nhật.

Manager nhúng launcher SHA **f7ba…6922**, khớp APK độc lập. Defaults Galaxy khớp source, DEX có code/khóa Galaxy, receipt khớp commit/hai patch/defaults. Không nhúng APK gốc, wallpaper VGS/macOS hoặc APK Xiaomi. Giữ MIT/OFL. `.gradle/aqua-build-verification.json` ghi kết quả, bao gồm `upgradeFrom21MetadataCompatible=true` khi truyền hai APK trước.

Snapshot trước/sau xác nhận upstream source, APK CleanTV gốc, patch VGS và defaults v2.1 không đổi. Không thao tác SmartTube hoặc thiết bị. Bản v2.1 giữ tại `.gradle/aqua-v22-review/`.

## 7. File thay đổi

- `patches/launcher-aqua-lite.patch`: renderer Galaxy/Config/factory/test, version launcher; giữ MainActivity/GlassView/greeting.
- `themes/ang-hon-aqua-galaxy/config.json`: defaults riêng mới, file v2.1 không sửa.
- `app/src/main/java/zone/vgs/tvsach/dulieu/Theme.kt`: preset và nhận diện hai Aqua.
- `app/src/main/java/zone/vgs/tvsach/ui/PhienLamViec.kt`: default Galaxy cho cài mới, tên/chọn preset cho cả hai Aqua; không đổi ADB/system flow.
- `app/src/main/assets/nang-cao.json`: sáu màu Galaxy.
- `app/src/test/java/zone/vgs/tvsach/dulieu/AquaGalaxyTest.kt`: bốn test mới.
- `app/build.gradle.kts`: version 2.2/112, defaults và guard Galaxy.
- `scripts/dung-aqua-lite.ps1`: chép/hash defaults mới, giữ build cô lập.
- `scripts/kiem-tra-aqua-lite.ps1`, `scripts/xac-minh-aqua-lite.ps1`: palette/version/upgrade/nội dung Galaxy.
- `scripts/review/RenderAquaGalaxy.java`: preview host, không vào APK.
- Báo cáo này, `docs/aqua-lite-build-verification.md`, `docs/ang-hon-aqua-lite.md`.

Tổng 14 file source/config/script/test/tài liệu, ngoài output build/receipt/preview/source copy trong `.gradle`. Không sửa `.vscode/settings.json` hoặc source hệ thống. Không đánh đồng toàn bộ dirty state từ các giai đoạn trước với riêng v2.2.

## 8. Cần kiểm chứng trên TCL

- Cảm giác tinh vân, độ sáng/indigo từ khoảng cách xem và các chế độ màu TV.
- Banding sau upscale, rim/chữ trên artwork sáng và wallpaper riêng; palette custom/scrim thấp có thể giảm tương phản.
- D-pad nhanh/giữ phím, dock/grid, quay lại app: đo frame time so v2.1 đã thử tốt.
- Cold start/recreate, RAM PSS trên ARMv7: bake nhiều hơn nhưng chỉ một lần; chưa có số đo TCL.
- Cập nhật giữ config, chọn Galaxy, xuất/nhập tên/màu/ảnh và quay lại v2.1. Không cần đổi Home hay gỡ launcher gốc.

Xuất config trước khi thử. Việc cài/cập nhật thiết bị là bước người dùng chủ động sau báo cáo này.
