# Ăng Hòn Aqua — build và xác minh APK

**Mới nhất: Aqua Galaxy v2.3e — Cosmic Plus (01/11/2026), READY FOR TESTING.** Đúng preview được duyệt: hành tinh 58 px tại (413,104), 28 sao tĩnh; giữ Galaxy/header/animation/navigation. Hai APK BUILD SUCCESSFUL, **391 test PASS** (338 + 46 + 7), không failure/error/skipped. Package/chứng chỉ giữ nguyên, Manager nhúng đúng Launcher; giữ preset v2.3c để rollback. Xem [báo cáo v2.3e, preview và APK](aqua-galaxy-v2.3e.md). Chưa thử v2.3e trên TCL; cần chọn preset Cosmic Plus để chuyển từ cấu hình v2.3c đang lưu.

```text
Launcher: app/src/aqua/assets/tvhome.apk — 2.3e / code 16
SHA-256: 4a5327a6968fe4a31f7ee5fba58727871812e5ad313d72541e2336c59703f867
Manager: app/build/outputs/apk/debug/app-debug.apk — 2.3e / code 115
SHA-256: f2a80fc608509d6cced3b1f07afc45eecefb9253c028c380971d9bc3b2420c4c
```

**Các phần v2.2/v2.1/v2/v1 bên dưới là lịch sử, không phải hash hiện tại.**

**Bản ghi lịch sử: Aqua Galaxy — Cinematic v2.2 (01/11/2026), READY FOR TESTING.** Hai APK BUILD SUCCESSFUL, 374 test PASS; giữ preset v2.1. Xem [báo cáo Galaxy, preview và xác minh cập nhật](aqua-galaxy-v2.2.md). Đây là ghi nhận trước phản hồi TCL và bản tinh chỉnh v2.3.

```text
Launcher: app/src/aqua/assets/tvhome.apk — 2.2 / code 13
SHA-256: f7ba98dde9f310ad329c35574164c5794e657659bd1df1fcc6a94687f2536922
Manager: app/build/outputs/apk/debug/app-debug.apk — 2.2 / code 112
SHA-256: fd9b8c8e16b5a26eb5fdce2c1acc1afde2653e74c6b71e72b04e82a05f1d9702
```

**Các phần v2.1/v2/v1 bên dưới là lịch sử, không phải hash hiện tại.**

**Bản ghi lịch sử 01/11/2026: Aqua Cinematic v2.1 — READY FOR TESTING.** Đã sửa lớp gradient bị artwork/scrim che, giữ animation/điều hướng. Launcher và Manager BUILD SUCCESSFUL; 365 test PASS; launcher nhúng đúng hash, chữ ký giữ nguyên. Xem [báo cáo v2.1 và kiểm tra render](aqua-cinematic-v2.1.md). Đây là ghi nhận trước phản hồi thử v2.1 và bản Galaxy v2.2.

```text
Launcher: app/src/aqua/assets/tvhome.apk — version 2.1 / code 12
SHA-256: bc5c23d3b3b595d096dfbed7d4e95098014589c74dec7751f3844e2aa0b30691
Manager: app/build/outputs/apk/debug/app-debug.apk — version 2.1 / code 111
SHA-256: 4ae2e2236fcf72c85cb0565f57544818530a6d7fe6e24e74518107030c474252
```

**Các phần v2/v1 bên dưới là lịch sử, không phải hash hiện tại.**

**Bản ghi lịch sử 01/11/2026: Aqua Cinematic v2 — READY FOR TESTING.** Launcher và Manager debug BUILD SUCCESSFUL; 355 unit test PASS; launcher nhúng khớp SHA-256. Xem [báo cáo Cinematic v2](aqua-cinematic-v2.md) cho phân tích animation, cấu hình chuyển từ v1, toàn bộ xác minh và lệnh đo chỉ đọc trên TCL. Đây là ghi nhận trước phản hồi thử v2 và bản sửa visual v2.1.

```text
Launcher: app/src/aqua/assets/tvhome.apk
SHA-256: 4dbe2084de6570cb594775152a21117c3b84f7ef4dc416c6bc3cb455de04bb34
Manager: app/build/outputs/apk/debug/app-debug.apk
SHA-256: 380cde68d459460c75f6193e575f78c9622b9946677fa605687b48ac35c1ea43
```

**Phần bên dưới là báo cáo lịch sử Aqua Lite v1 (31/10), không phải hash/preset hiện tại.**

Ngày 31/10/2026, Asia/Bangkok. Trạng thái: **READY FOR TESTING**.

Hai APK debug đã build và xác minh thành công. Đây là kết quả kiểm tra artifact và unit test, chưa phải xác nhận giao diện mượt/ổn định trên TCL. Không dùng ADB, cài/gỡ APK, đổi Home mặc định hoặc thao tác lên TV; không commit, push hay reset Git.

## Artifact cuối cùng

| Thành phần | Đường dẫn trong workspace | Package | versionCode | minSdk / targetSdk | Kích thước |
| --- | --- | --- | --- | --- | --- |
| Launcher | `app/src/aqua/assets/tvhome.apk` | `vn.anghon.aqua.launcher` | 11 | 26 / 34 | 2,274,066 byte |
| Quản lý | `app/build/outputs/apk/debug/app-debug.apk` | `vn.anghon.aqua.manager` | 110 | 26 / 35 | 29,799,046 byte |

Đường dẫn tuyệt đối:

```text
C:\Users\mhoang\OneDrive\Desktop\vgs-cleantv\app\src\aqua\assets\tvhome.apk
C:\Users\mhoang\OneDrive\Desktop\vgs-cleantv\app\build\outputs\apk\debug\app-debug.apk
```

SHA-256 APK cuối cùng:

```text
launcher c88465bd4928cd063d902b8c0c4eb2a8c16896968d375a061f510d5df1e5a552
manager  873369566d673d5265bb2924819109220211ed0b39e5eb1d16b59fafddb7fac5
```

Cả hai được `apksigner` xác minh hợp lệ với `--min-sdk-version 28 --max-sdk-version 28`, dùng cùng chứng chỉ debug cục bộ:

```text
certificate SHA-256
5b6ffaad8776e93270563667d2b0c28349de8e528d1fdb5576977c158388f486
```

Chứng chỉ này khác fingerprint launcher gốc ghi trong đợt audit trước. Package cũng khác `com.gathin.tvhome`, `com.ts.launcher` và `zone.vgs.cleantv`; các APK Aqua không phải bản cập nhật ghi đè những package đó. Trước khi cài thử vẫn cần kiểm tra TV không có một app không liên quan trùng package Aqua. Giữ keystore debug hiện tại nếu muốn cập nhật các bản thử sau, không chia sẻ khóa riêng.

Launcher không có thư viện native, không bị giới hạn vào ARM64. Quản lý có native ABI `armeabi-v7a` và `arm64-v8a`. minSdk 26 cho phép cài trên Android 9/API 28; điều này không thay thế kiểm thử chạy thực tế.

## Môi trường và nguồn build

- Gradle 8.9, xác minh checksum wrapper giữ nguyên.
- JDK chạy Gradle: `C:\Program Files\Java\jdk-21.0.12`; source/bytecode target vẫn là Java/Kotlin 17. Build thực tế đã qua với JDK 21, không cần cài JDK 17 cho lượt build này.
- SDK: `C:\Users\mhoang\AppData\Local\Android\Sdk`, đọc từ `local.properties`.
- API 35 / Build-Tools 35.0.0 đã có cho quản lý. Đã bổ sung API 34 / Build-Tools 34.0.0 bằng SDK Manager với quyền được cấp để giữ nguyên cấu hình launcher.
- AGP 8.7.2 cho quản lý, AGP 8.5.2 cho launcher. Gradle dùng kết nối mạng, không dùng `--offline`.
- Cache Gradle trong `.gradle/aqua-cache`.

Launcher lấy từ commit `fe9ad3204b945d3aadfb6876ee6ea78c20a1fe73` của CleanYourTV. Script xuất archive vào thư mục mới dưới `.gradle/aqua-launcher/`, áp lần lượt `launcher-vgs.patch` và `launcher-aqua-lite.patch` bằng kiểm tra có context. Manifest, icon, banner, defaults và giấy phép chỉ được bổ sung vào bản sao tách biệt đó. Không checkout/reset hay áp lại patch lên cây `upstream/` hiện có.

Cây source của artifact cuối và kết quả test launcher:

```text
.gradle/aqua-launcher/6215cd1818324c3d9436e60af9be74bc/launcher/
```

## Kiểm tra đóng gói

Các kiểm tra sau đều PASS:

- `assets/tvhome.apk` trong APK quản lý có SHA-256 **đúng bằng** APK launcher độc lập: `c88465bd4928cd063d902b8c0c4eb2a8c16896968d375a061f510d5df1e5a552`.
- `assets/aqua-defaults.json` trong launcher khớp byte/hash với `themes/ang-hon-aqua-lite/config.json`.
- Task `verifyAquaLauncher` đối chiếu hash APK, commit, hai patch và defaults; kiểm tra mã `focus_accent_color` / `.action.APPLY_THEME` trong DEX trước merge assets.
- Provider launcher trong binary manifest: `vn.anghon.aqua.launcher.theme`.
- APK quản lý không chứa `assets/theme/vgs.jpg`, `assets/theme/macos.jpg`, hoặc `assets/tvhome-xiaomi.apk`.
- Launcher có thông báo MIT của CleanTV/CleanYourTV và OFL của Lexend trong `assets/licenses/`; app quản lý vẫn giữ `giay-phep.txt`.
- Hai APK launcher gốc trong `app/src/main/assets` không bị ghi đè; `git diff --exit-code` trên chúng PASS.

Một lỗi đã được phát hiện bằng kiểm tra APK thật: `sourceSets.main.assets.exclude(...)` không loại ảnh VGS khỏi gói cuối như mong muốn. Đã sửa bằng task `prepareAquaMainAssets` tạo bản assets lọc trong `app/build/generated/aquaMainAssets`, không xóa hoặc đổi assets gốc. Sau sửa đã build lại và xác minh ZIP cuối thành công.

Receipt build: `app/src/aqua/assets/aqua-launcher.properties`. Kết quả xác minh máy đọc được: `.gradle/aqua-build-verification.json`. Các file APK/receipt sinh ra được bỏ qua bởi Git; không commit binary.

## Unit test và lệnh đã chạy

| Phạm vi | Suite | Test | Fail / error / skipped |
| --- | --- | --- | --- |
| Launcher | 44 | 309 | 0 / 0 / 0 |
| Quản lý `app` | 9 | 33 | 0 / 0 / 0 |
| Module `khoa` | 1 | 7 | 0 / 0 / 0 |
| Tổng | 54 | **349** | **0 / 0 / 0** |

Trong 33 test của quản lý, `AquaLiteTest` có 4 test PASS và `KichBanKhoiPhucTest` có 4 test PASS. Đã chạy toàn bộ tập unit test debug của các module, không lọc chỉ các test này. Các lượt đóng gói lại chỉ đổi assets dùng kết quả unit test còn hợp lệ của Gradle; không cộng trùng số test giữa các lượt build.

Lệnh build chính đã chạy, có mạng:

```powershell
$gradleBuild = "$env:USERPROFILE/.gradle/wrapper/dists/gradle-8.9-bin/90cnw93cvbtalezasaz0blq0a/gradle-8.9/bin/gradle.bat"
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/dung-aqua-lite.ps1 -GradleExecutable $gradleBuild
& $gradleBuild --no-daemon --console=plain -g .gradle/aqua-cache -PaquaLite=true testDebugUnitTest :app:assembleDebug
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/kiem-tra-aqua-lite.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/xac-minh-aqua-lite.ps1
```

Launcher và quản lý đều có kết quả **BUILD SUCCESSFUL**. Kiểm tra cấu hình tĩnh và `git diff --check` PASS. Không chạy instrumentation test, emulator, lint toàn bộ hay benchmark TV.

Báo cáo test HTML:

- Quản lý: `app/build/reports/tests/testDebugUnitTest/index.html`.
- Khôi phục Aqua: `app/build/reports/tests/testDebugUnitTest/classes/zone.vgs.tvsach.dulieu.KichBanKhoiPhucTest.html`.
- Preset Aqua: `app/build/reports/tests/testDebugUnitTest/classes/zone.vgs.tvsach.dulieu.AquaLiteTest.html`.
- Module khoa: `khoa/build/reports/tests/testDebugUnitTest/index.html`.
- Launcher: `<cây source cuối>/app/build/reports/tests/testDebugUnitTest/index.html`.

Các cảnh báo còn lại không làm build thất bại: SDK XML v4 với tooling AGP cũ hiểu v3, API deprecated trong code gốc, và một số thư viện native được đóng gói không strip symbol. Không nâng AGP hoặc sửa các tính năng cũ chỉ để loại các cảnh báo này.

## Đường triển khai, khôi phục và giới hạn

APK quản lý đóng gói đúng launcher mới nhưng bản preview **không tự cài launcher/thay Home**. `caiHomeSach()` của Aqua chặn trước các lệnh TV; để thử sau này cần cài launcher riêng sau khi người dùng cho phép. Khi launcher Aqua đã có, “Áp dụng giao diện” chỉ gọi `dayGiaoDienAqua`: ghi cấu hình/ảnh trong package Aqua rồi mở Activity bằng action riêng, không gọi đường sửa gói, cấp quyền hệ thống hoặc đổi Home mặc định.

Đích launcher/provider riêng; OTA CleanTV tắt trong Aqua để tránh tải nhầm launcher gốc. Phần khôi phục launcher được giới hạn vào package Aqua, kịch bản khôi phục có test chứng minh không đề xuất gỡ launcher gốc và có thể trả lại Home CleanTV trước đó. Các chức năng debloat/khôi phục tổng quát vẫn tồn tại và có thể thay đổi hệ thống nếu người dùng chủ động gọi; không dùng chúng làm rollback cho lần thử theme. Logic debloat và ADB transport không được sửa trong lượt build này.

Đây là APK debug, launcher chưa tối ưu bằng R8 như release. Chưa đo RAM/FPS/CPU/GPU, chưa thử remote TCL hay khởi động lại Android 9. Bộ nhớ `/data` 4.2 GiB là dung lượng tổng, chưa biết phần trống. Khi được cho phép thử, cài quản lý lên điện thoại, launcher lên TV bằng package riêng, mở từ danh sách ứng dụng và giữ nguyên Home cũ. Không bật helper bắt Home/tự khởi động ở lần thử đầu. Trở về launcher cũ hoặc gỡ đúng package Aqua là cách dừng thử; không reset TV, không gỡ CleanTV gốc.

Không còn blocker cho **việc tạo và xác minh hai APK debug này**. READY FOR TESTING nghĩa là có thể chuẩn bị một lượt thử được người dùng cho phép; không có nghĩa đã kiểm chứng độ mượt hoặc an toàn vận hành trên TV thực tế.
