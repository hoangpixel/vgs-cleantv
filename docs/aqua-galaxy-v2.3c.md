# Aqua Galaxy v2.3c — final verification

**READY FOR TESTING — 01/11/2026.** Hai APK debug build thành công, tổng 383 unit test PASS; xác minh package/chữ ký API 28, cập nhật từ v2.3 và Launcher nhúng trong Manager đều PASS. Chưa cài hoặc đo frame time trên TCL. Không chạy ADB, đổi Home, gỡ ứng dụng, commit hoặc push.

## Thiết kế được duyệt và source chính thức

Tích hợp renderer từ `scripts/review/candidates/v23c/CosmicDecor.java` vào `patches/launcher-aqua-lite.patch`; chỉ đổi comment mô tả khi chuyển sang production. Pixel của source chính thức khớp thiết kế người dùng đã duyệt:

- Một hành tinh **48 px** trên texture 480×270, tâm `(413,64)`.
- Mặt sáng champagne `#E8C8A0`, mặt tối tím khói `#77627F`, viền `#F3D9B7`, bóng `#392C46`. Pháp tuyến hình cầu, ánh sáng xiên trên-phải và chuyển mềm giữa sáng/tối.
- Halo tĩnh 8 px, alpha tối đa 14/255; thân cách mép trên 40 px và mép phải 42 px.
- **12 sao** giữ nguyên vị trí/cường độ so với v2.3b. Galaxy nền không đổi; lời chào vẫn dưới đồng hồ/ngày/lịch âm.
- Decor bake vào mảng pixel của **heroScrim hiện có**, nằm trên artwork/ambient và dưới nội dung. Không thêm View, bitmap riêng, blur, particle, timer hoặc animation.

Bitmap vẫn ARGB_8888 480×270 (~0,49 MiB), cache theo Activity như trước. Phép tính hình cầu chỉ trong bước chuẩn bị decor, không gọi theo focus hoặc đổi app. Không suy ra FPS/RAM trên TCL từ kiểm tra source này.

Hash texture ARGB:

```text
Galaxy v2.2 không decor:
46e44756d7cf4c920c88f5558042f2be818dda077de58e66c9d9f545aa509b84
Champagne Moon v2.3c, đúng preview đã duyệt:
61b836ba35389065601e687afa875b84e43d4556d34437179cb6fff418201885
```

Giữ ID preset `ang-hon-aqua-cosmic`, cập nhật nhãn thành **Ăng Hòn Aqua Galaxy — Cinematic v2.3c**. Không đổi khóa cấu hình hoặc cơ chế cập nhật/áp theme. Preset v2.1/v2.2 và hai file defaults cũ giữ nguyên byte. Config cosmic đang lưu tiếp tục chọn renderer mới khi Activity của APK mới được tạo; người đang dùng v2.1/v2.2 có thể chọn preset v2.3c nếu muốn decor mới. Không tự ghi đè cấu hình của người dùng.

## Preview cuối

[Mở so sánh v2.3b / v2.3c, ba trạng thái Idle / Artwork / Focus](../.gradle/aqua-v23c-final/champagne-moon-comparison.png).

```text
C:\Users\mhoang\OneDrive\Desktop\vgs-cleantv\.gradle\aqua-v23c-final\champagne-moon-comparison.png
PNG SHA-256 f83da03e83536742d413b18bc52ad56abb33d8fb5b1522c2c8760693adb77616
```

Render bằng source production đã áp patch; PNG khớp **từng byte** với ảnh đã duyệt tại `.gradle/aqua-v23c-review/champagne-moon-comparison.png`. Trái v2.3b aqua 36 px, phải v2.3c champagne 48 px. Galaxy, sao và pixel ngoài vùng hành tinh không đổi; 2.950 pixel khác candidate v2.3b, đều trong vùng hành tinh phải. Kết quả render lặp lại xác định, RTL mirror khớp.

Header/card là bố trí tham chiếu, artwork là underlay tổng hợp; đây không phải screenshot Android TV. Không đè vùng chữ/card trong ba preview. Layout thật, overscan, tên app dài và màu/độ rõ từ xa vẫn cần kiểm tra trên TCL.

## Build và unit test thực tế

Gradle **8.9**, JDK runtime **21.0.12**, source/bytecode target **17**; SDK `C:\Users\mhoang\AppData\Local\Android\Sdk`. Launcher: AGP 8.5.2, SDK/Build-Tools 34; Manager: AGP 8.7.2, SDK/Build-Tools 35. Không thêm dependency, không dùng `--offline`.

Source launcher lấy từ archive commit `fe9ad3204b945d3aadfb6876ee6ea78c20a1fe73`, áp VGS rồi Aqua vào thư mục cô lập. Không sửa/reset source `upstream/` đang có thay đổi từ trước.

```text
Source Launcher cuối:
C:\Users\mhoang\OneDrive\Desktop\vgs-cleantv\.gradle\aqua-launcher\47efa797cabc4a53b52183415537f504\launcher
```

Các lệnh chính từ root (dùng Gradle 8.9 đã có trong wrapper cache):

```powershell
$gradleBuild = "$env:USERPROFILE/.gradle/wrapper/dists/gradle-8.9-bin/90cnw93cvbtalezasaz0blq0a/gradle-8.9/bin/gradle.bat"
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/dung-aqua-lite.ps1 -GradleExecutable $gradleBuild
& $gradleBuild --no-daemon --console=plain -g .gradle/aqua-cache -PaquaLite=true testDebugUnitTest :app:assembleDebug
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/review/kiem-tra-patch.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/kiem-tra-aqua-lite.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/xac-minh-aqua-lite.ps1 -PreviousLauncherApk .gradle/aqua-v23c-final/launcher-v23.apk -PreviousManagerApk .gradle/aqua-v23c-final/manager-v23.apk
git diff --check
```

**Launcher BUILD SUCCESSFUL in 22s; Manager BUILD SUCCESSFUL in 1m 1s.** Log cuối: `.gradle/aqua-v23c-final/launcher-build-verified.log` và `manager-build.log`.

Các lần thử trước gặp vấn đề môi trường, đã giữ log để đối chiếu:

1. Java của phiên sandbox nhận `user.home=C:\`, Android plugin dừng vì `C:\.android`.
2. Đặt Android user-home trong workspace, nhưng sandbox chặn thao tác khóa `debug.keystore.lock`.
3. Truyền rõ cùng khóa ký đã có qua signing properties, vượt bước ký; compiler bị chặn đọc `androidx.collection:collection:1.0.0` trong cache.
4. Chạy lại build/test ngoài sandbox qua cơ chế cấp quyền của công cụ: hai build thành công. Không sửa source để né các lỗi quyền truy cập này.

Build cuối dùng `ANDROID_USER_HOME=.gradle/aqua-android-home` và bản sao **cùng khóa** từ `%USERPROFILE%\.android\debug.keystore` ở `retained-aqua-signing.jks`, qua `ORG_GRADLE_PROJECT_android.injected.signing.*` (store.file, store.password, key.alias, key.password). Đây là thiết lập của tiến trình build, không thay key/package/update path trong source. Key và cache nằm trong thư mục bị Git ignore. Chứng chỉ được xác minh với APK v2.3 cũ, không dựa vào tên file key. Không tạo khóa ký mới.

Warning giấy phép có trailing space trong patch VGS và deprecated API/Kotlin có sẵn không chặn build. Không sửa các phần đó. Một lần gọi helper regression bằng đường dẫn tương đối thất bại do helper tính substring từ đường dẫn tuyệt đối; chạy lại qua receipt chứa đường dẫn tuyệt đối đã PASS, không đổi helper/source launcher vì lỗi gọi lệnh này.

Thống kê **trực tiếp từng XML**, đối chiếu số testcase với thuộc tính `tests`, không cộng trùng lượt build:

| Module | Suite XML | Test | Failure | Error | Skipped |
| --- | --- | --- | --- | --- | --- |
| Launcher | 48 | 333 | 0 | 0 | 0 |
| Manager | 11 | 43 | 0 | 0 | 0 |
| khoa | 1 | 7 | 0 | 0 | 0 |
| Tổng | 60 | **383** | **0** | **0** | **0** |

`khoa:testDebugUnitTest` UP-TO-DATE, giữ 7 kết quả hợp lệ của module không đổi; Launcher và Manager chạy test trong lượt build này. So với 382 test của v2.3, thêm đúng **1 golden test** `approvedChampagnePreviewRemainsPixelIdentical` (CosmicDecorTest: 5 → 6 test). Test vùng decor cũ được đổi từ budget <1% sang <3% để phản ánh hình 48 px và 12 sao đã duyệt; vẫn kiểm tra chỉ thay vùng được khai báo ở nửa phải. Test hash nền Galaxy, v2.1 không decor, tính tĩnh/RTL và contrast lời chào tiếp tục PASS.

Các suite Manager được yêu cầu: AquaLiteTest **7**, AquaGalaxyTest **4**, AquaCosmicTest **3**, KichBanKhoiPhucTest **4**, đều không failure/error/skipped. Không chạy instrumentation/emulator hoặc benchmark trên TV.

Nguồn thống kê:

- Launcher: thư mục source cuối ở trên, `app/build/test-results/testDebugUnitTest/TEST-*.xml`.
- Manager: `app/build/test-results/testDebugUnitTest/TEST-*.xml`.
- khoa: `khoa/build/test-results/testDebugUnitTest/TEST-*.xml`.
- Tổng hợp máy đọc: `.gradle/aqua-v23c-final/unit-test-summary.json`.

## Hồi quy và an toàn

- Patch Aqua chỉ đổi ba khối so với v2.3: version launcher, CosmicDecor, CosmicDecorTest.
- Helper xác nhận hai patch áp đúng vào commit sạch; **253 file source/resource** khớp cây launcher cuối.
- **9 file engine/layout và 19 phương thức motion/navigation** giữ nguyên baseline Cinematic sau VGS; Glass drawing/geometry giữ nguyên.
- So trực tiếp cây `app/src` v2.3 và v2.3c: chỉ CosmicDecor.java và CosmicDecorTest.java khác byte. MainActivity, AquaSurface, AquaCinematic, AquaHeader, AquaGreeting, D-pad, spring, duration/interpolator, RecyclerView, dock và thời gian giữ nguyên.
- Hash snapshot xác nhận defaults cũ, VGS patch, code ADB/debloat/khôi phục, cấu hình/theme transport và candidate/preview cũ giữ nguyên. Không đổi source CleanTV chính thức hoặc hai APK launcher gốc trong `app/src/main/assets`.
- `scripts/kiem-tra-aqua-lite.ps1` PASS: palette/motion, cấu hình có thể khôi phục, đường apply theme chỉ đổi cấu hình, XML/catalogue và whitespace.

## APK cuối và chữ ký

| APK | Package | Version / code | minSdk / targetSdk | Byte |
| --- | --- | --- | --- | --- |
| Launcher | `vn.anghon.aqua.launcher` | **2.3c / 15** | 26 / 34 | 2.280.650 |
| Manager | `vn.anghon.aqua.manager` | **2.3c / 114** | 26 / 35 | 28.862.658 |

Đường dẫn tuyệt đối:

```text
Launcher:
C:\Users\mhoang\OneDrive\Desktop\vgs-cleantv\app\src\aqua\assets\tvhome.apk
Manager:
C:\Users\mhoang\OneDrive\Desktop\vgs-cleantv\app\build\outputs\apk\debug\app-debug.apk
```

```text
Launcher SHA-256
658208e428c5ac972b5c09ca6764ddc3c83b27443cf6e5fe009bc5d3251d7d0e
Manager SHA-256
e2ceb39ecc2ab9cb37edd249893b867502592cf2f5d24318507bca134972bc8e
Signer certificate SHA-256 (cả hai, cùng v2.3)
5b6ffaad8776e93270563667d2b0c28349de8e528d1fdb5576977c158388f486
```

apksigner xác minh chữ ký hợp lệ trên **API 28**. Package/chứng chỉ giống hai APK v2.3 đã lưu trước task; versionCode tăng **14→15 / 113→114**, tương thích metadata để cập nhật giữ dữ liệu. Launcher không có native libs; Manager có **armeabi-v7a** và arm64-v8a. Chưa xác minh cài/update thực tế trên TCL.

Verifier đọc **byte `assets/tvhome.apk` bên trong Manager**, hash ra `658208e4…51d7d0e`, trùng đầy đủ SHA-256 Launcher độc lập. Defaults cosmic khớp source, DEX có renderer/header, authority đúng, receipt nhúng khớp, giấy phép MIT/OFL còn nguyên. Không đóng gói nhầm launcher gốc hoặc wallpaper/logo VGS/macOS. JSON chứng cứ: `.gradle/aqua-v23c-final/apk-verification.json` (bản sao của `.gradle/aqua-build-verification.json`).

Hai APK v2.3 cũ được giữ trong `.gradle/aqua-v23c-final/launcher-v23.apk` và `manager-v23.apk`. Android thường không cho hạ versionCode bằng cập nhật thông thường: ưu tiên quay về preset v2.2/v2.1 trong Manager nếu cần bỏ decor mới; không coi APK cũ là phương án downgrade giữ dữ liệu đã được thử nghiệm.

## File thay đổi trong lượt finalize này

**8 file source/config/script/tài liệu**, ngoài 2 file APK/receipt ở assets và output build:

1. `patches/launcher-aqua-lite.patch`: renderer v2.3c, version launcher và test golden/budget decor.
2. `app/build.gradle.kts`: Aqua Manager 2.3c / 114; nhánh CleanTV gốc giữ nguyên.
3. `app/src/main/java/zone/vgs/tvsach/dulieu/Theme.kt`: nhãn/mô tả preset cosmic; không đổi map cấu hình.
4. `scripts/xac-minh-aqua-lite.ps1`: metadata mới, cho phép đối chiếu cập nhật từ v2.3.
5. `scripts/review/candidates/v23c/README.md`: ghi trạng thái thiết kế đã được duyệt/tích hợp.
6. `docs/aqua-v2.3c-planet-review.md`: liên kết kết quả finalize, giữ nội dung review gốc.
7. `docs/aqua-lite-build-verification.md`: cập nhật kết quả/APK mới nhất.
8. `docs/aqua-galaxy-v2.3c.md`: báo cáo này.

Asset được tạo lại: `app/src/aqua/assets/tvhome.apk`, `app/src/aqua/assets/aqua-launcher.properties`. Manager output: `app/build/outputs/apk/debug/app-debug.apk`. Output/cache/preview/test report nằm trong `.gradle/` và `build/`, không tính là source. Giữ nguyên các dirty changes khác của workspace từ trước task; không reset/checkout/clean Git hoặc sửa `.vscode/settings.json`.

## Cần kiểm chứng tiếp trên TCL

Thiết kế đã hoàn tất tích hợp; APK đủ điều kiện bắt đầu thử nghiệm thủ công. Chưa khẳng định mượt hơn hoặc bằng v2.2/v2.3 từ test JVM/build. Cần xem:

- Độ rõ hành tinh champagne và 12 sao từ xa; tương phản, banding/upscale, overscan.
- Vùng header/hero với tên dài, weather, font scale; lời chào theo giờ TV.
- Chuyển app nhanh/giữ D-pad, focus/dock/grid, frame time, cold start/recreate và RAM trên TCL.
- Giữ cấu hình cá nhân sau cập nhật; xuất cấu hình trước khi người dùng tự thử, chọn lại v2.2/v2.1 để khôi phục nền/bố cục trước nếu cần.

Không có bước triển khai thiết bị trong đợt finalize này.
