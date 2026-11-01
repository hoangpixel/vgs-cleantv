# Ăng Hòn Aqua Lite — giai đoạn 1

**Mới nhất: Galaxy v2.3 (01/11/2026)** — lời chào dưới giờ/ngày, decor cosmic tĩnh; giữ v2.1/v2.2. Hai APK đã build, 382 test PASS. Xem [báo cáo v2.3 và preview](aqua-galaxy-v2.3.md). Các mục phiên bản dưới đây là lịch sử.

**Cập nhật Galaxy v2.2 (01/11/2026):** thêm preset tinh vân riêng, giữ v2.1 và chuyển động Cinematic. Hai APK build thành công, 374 test PASS. Xem [báo cáo Galaxy, preview và APK mới](aqua-galaxy-v2.2.md). Các mục phiên bản bên dưới là lịch sử.

**Mới nhất: Aqua Cinematic v2.1 (01/11/2026).** Gradient Aqua nay nằm trên artwork, focus rim/vạch đồng hồ Aqua; giữ chuyển động v2 được người dùng thử tốt. Hai APK đã build, 365 test PASS. Xem [báo cáo, APK và giới hạn kiểm chứng v2.1](aqua-cinematic-v2.1.md). Các đoạn v2/v1 dưới đây là lịch sử.

**Cập nhật 01/11/2026:** hướng Aqua Minimal được thay bằng **Aqua Cinematic v2**, tái sử dụng bố cục và animation Điện ảnh gốc. Hai APK v2 đã build, 355 test PASS; chưa đo độ mượt trên TCL. Xem [báo cáo v2 và APK mới](aqua-cinematic-v2.md). Nội dung giai đoạn 1 dưới đây mô tả preset v1 lịch sử (ít chuyển động); khi cập nhật cần chọn/áp lại preset v2 từ Manager.

Đây là mã nguồn bản thử; **hai APK Aqua debug đã build và xác minh thành công**. Không có lệnh nào cài APK, đổi Home, vô hiệu hóa gói hoặc push GitHub được thực hiện trong phiên triển khai.

Kết quả mới nhất ngày 31/10/2026: **READY FOR TESTING**. Xem [báo cáo build và xác minh APK](aqua-lite-build-verification.md) để lấy đường dẫn hai APK, SHA-256, chữ ký và kết quả **349 unit test PASS**. [Final Review cũ](aqua-lite-final-review.md) là bản ghi trước khi môi trường SDK được khắc phục.

## Kiến trúc và phạm vi thay đổi

- `app/`: ứng dụng quản lý trên điện thoại, Kotlin/Compose. `Theme.kt` chứa preset, `UngDung.kt` chứa màn chọn giao diện/ảnh/tên/màu, `PhienLamViec.kt` tạo JSON và gửi qua ADB.
- `upstream/launcher/`: launcher Java/View/RecyclerView độc lập, không phải giao diện Compose của ứng dụng quản lý. Mã nguồn lấy từ CleanYourTV, khóa tại `fe9ad3204b945d3aadfb6876ee6ea78c20a1fe73`, trùng `app/src/main/assets/upstream-commit.txt`.
- `patches/launcher-vgs.patch`: các cải tiến CleanTV sẵn có, gồm thời tiết, font Lexend tĩnh cho dấu tiếng Việt trên Android 9, phím remote và provider cấu hình. Không thay đổi nội dung patch này; `.gitattributes` buộc patch dùng LF để Git đọc được binary patch trên Windows.
- `scripts/dung-launcher.sh`: quy trình gốc cho Bash, mặc định đường dẫn Homebrew/macOS. Build release hai lần với package Xiaomi và package thường, chép vào `app/src/main/assets/tvhome-xiaomi.apk` và `tvhome.apk`, rồi tạo lại dữ liệu bằng Python. **Không dùng script này để build Aqua**: nó ghi vào assets gốc.
- `scripts/dung-aqua-lite.ps1`: quy trình Windows riêng, không cài TV. Xuất archive commit khóa vào `.gradle/aqua-launcher/`, áp patch gốc và patch Aqua trên bản sao, không sửa source upstream. Build/test launcher debug, xác minh package/chữ ký rồi mới chép sang `app/src/aqua/assets/tvhome.apk`. APK gốc giữ nguyên.

Preset Aqua dùng navy `#102637`, focus aqua `#80DEDA`, chữ trắng, 5 cột, dock nền đặc và viền focus đơn giản (`glass_style=rim`). Giữ đồng hồ, lịch âm, lời chào, hành vi ứng dụng gần đây và điều hướng D-pad/OK/Back sẵn có. Không thêm hàng ứng dụng hoặc dịch vụ theo dõi mới. Không thêm timer lời chào, video wallpaper, blur động hoặc tác vụ nền mới.

Launcher đã có bốn lời chào theo giờ TV: đêm 00:00–04:59, sáng 05:00–11:59, chiều 12:00–16:59, tối 17:00–21:59 và đêm từ 22:00 (nhánh tiếng Việt). Tên mặc định `Ăng Hòn`; có thể sửa từ điện thoại. Cấu hình mặc định launcher đặt `locale=vi`, lịch âm UTC+7. Dùng lịch/đồng hồ và lịch cập nhật có sẵn.

`focus_accent_color` là khóa bổ sung duy nhất cho màu focus thường: khóa `accent_color` của upstream chỉ dùng khi bật skin lễ hội. Thiếu/sai màu thì dùng hành vi màu upstream. Khi di chuyển dock rồi thoát chế độ sắp xếp, màu Aqua được khôi phục. Skin lễ hội và màu theo app vẫn có quyền ưu tiên nếu người dùng chủ động bật.

## Hai ứng dụng chạy song song

| Thành phần | CleanTV gốc | Bản thử Aqua |
| --- | --- | --- |
| Quản lý trên điện thoại | `zone.vgs.cleantv` | `vn.anghon.aqua.manager` |
| Launcher thường | `com.gathin.tvhome` | `vn.anghon.aqua.launcher` |
| Provider cấu hình launcher | `com.gathin.tvhome.theme` | `vn.anghon.aqua.launcher.theme` |

Namespace/lớp launcher vẫn là `com.gathin.tvhome`; applicationId mới tạo vùng dữ liệu/provider riêng. Home mặc định của Android chỉ có một, nhưng hai launcher có thể cùng được cài và mở từ danh sách ứng dụng. Chưa thử song song trên TV thực tế.

Build quản lý với `-PaquaLite=true` để dùng package, tên và biểu tượng giọt nước riêng; không có cờ này thì vẫn build CleanTV gốc. Bản thử Aqua bị chặn build release để tránh phát hành nhầm khi chưa kiểm thử. Mã nguồn giữ bản quyền VGS/AlexVGS và Gathin cùng giấy phép MIT, các giấy phép font OFL trong upstream. Biểu tượng/tên ứng dụng Aqua không dùng logo VGS. Các nội dung trợ giúp cũ còn nhắc nguồn CleanTV; chưa coi đây là bản phát hành thương mại hoàn chỉnh.

Trong bản Aqua, OTA CleanTV bị tắt để không tải APK launcher gốc vào bản tùy biến; không lập lịch kiểm tra cập nhật nền. Chọn “Áp dụng giao diện” dùng đường gửi riêng, chỉ sửa cấu hình/ảnh trong package Aqua rồi yêu cầu Activity đọc lại, không cài đè launcher, sửa quyền hay đổi Home mặc định. Luồng tự cài/thay Home của bản Aqua đã bị chặn. Khôi phục giới hạn phần xử lý launcher vào package Aqua; nhánh khôi phục CleanTV gốc giữ hành vi mặc định. Không sửa danh sách/quy tắc vô hiệu hóa ứng dụng hệ thống hay ADB transport. Khi chỉ kết nối bằng Aqua, bản sao lưu đọc từ TV được lưu cục bộ, không ghi đè bản trên TV.

## Đổi cấu hình mà không build lại

Sau khi người dùng tự cài bản launcher Aqua và bản quản lý Aqua, kết nối qua Wi-Fi/ADB như cũ, mở **Đổi giao diện Home sạch**, chọn **Ăng Hòn Aqua Lite**:

1. Đổi tên tại **Đồng hồ & lời chào**; để trống trong preset Aqua sẽ dùng `Ăng Hòn`.
2. Chọn một ảnh nền tĩnh từ điện thoại bằng bộ chọn ảnh hiện có. Không chọn thêm bộ ảnh xoay nếu muốn nhẹ nhất.
3. Trong **Nâng cao → Hình nền & màu**, đổi `background_color` và `focus_accent_color` (ví dụ `#102637`, `#80DEDA`). Màu chữ dùng tùy chọn màu chữ sẵn có. Lựa chọn Nâng cao ghi sau preset nên có thể đè preset.
4. Áp dụng giao diện. Có thể lưu/mở file cấu hình bằng chức năng sẵn có; tên và khóa màu mới đi cùng JSON.

Không bật lại hiệu ứng kính, cinematic, ambient glow, fade rows hoặc animated screensaver trên TV RAM 1.37 GiB nếu ưu tiên nhẹ. Đây là khuyến nghị cho preset, chưa phải kết quả đo RAM/FPS. `themes/ang-hon-aqua-lite/config.json` là mặc định khi chưa có cấu hình ngoài; cấu hình ghi từ điện thoại được ưu tiên ở lần đọc sau. Không build lại khi đổi tên/màu/ảnh.

## Trạng thái môi trường Windows đã kiểm tra

- `JAVA_HOME=C:\Program Files\Java\jdk-21.0.12`; Gradle 8.9 đã build cả hai dự án thành công với JDK 21. Source/bytecode target vẫn là 17; script chấp nhận JDK 17 hoặc 21.
- SDK tại `C:\Users\mhoang\AppData\Local\Android\Sdk`, đọc từ `local.properties`. API 35 / Build-Tools 35.0.0 đã có; API 34 / Build-Tools 34.0.0 đã được bổ sung cho launcher với quyền được cấp.
- Wrapper từng trỏ tới Gradle 9.7.1 nhưng giữ checksum của 8.9. Đã sửa URL về 8.9 cho khớp checksum và cấu hình gốc; không bỏ kiểm tra SHA-256. Các blocker dependency/SDK cũ đã được khắc phục và build hiện đã thành công.
- Đã dùng mạng tải dependency và chạy AGP 8.7.2 cho quản lý, 8.5.2 cho launcher; không dùng `--offline`. Cache tại `.gradle/aqua-cache`. Hai build đã thành công.
- Python không có trong PATH; cần cho script build gốc, không cần cho script Aqua Windows.

## Hướng dẫn build sau khi chuẩn bị môi trường

Cần JDK 17 hoặc 21 và SDK API 34/35, Build-Tools 34.0.0/35.0.0. Các gói này hiện đã có. Command-line Tools mới dùng tên gói `platforms/android-34`, `build-tools/34.0.0`. Đặt biến theo đường dẫn thực tế, ví dụ trong PowerShell:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-21.0.12'
$env:ANDROID_HOME = "$env:LOCALAPPDATA\Android\Sdk"
$env:GRADLE_USER_HOME = "$env:USERPROFILE\.gradle"
```

Wrapper hiện đã đồng bộ: `gradle-8.9-bin.zip` / `d725d707bfabd4dfdc958c624003b3c80accc03f7037b5122c4b1d0ef15cecab`. Giữ kiểm tra SHA-256. Tạo `local.properties` (không commit), dùng dấu `/` trong đường dẫn SDK:

```properties
sdk.dir=C:/Users/mhoang/AppData/Local/Android/Sdk
```

Chạy từ thư mục repo:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/kiem-tra-aqua-lite.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/dung-aqua-lite.ps1 -PrepareOnly
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/dung-aqua-lite.ps1
.\gradlew.bat --no-daemon -g .gradle/aqua-cache -PaquaLite=true testDebugUnitTest :app:assembleDebug
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/xac-minh-aqua-lite.ps1
```

Kết quả đã có: launcher `app/src/aqua/assets/tvhome.apk`, quản lý `app/build/outputs/apk/debug/app-debug.apk`. Kiểm tra receipt/hash/Dex khi merge assets và kiểm tra APK cuối bằng script xác minh. Script không gọi ADB hoặc cài đặt.

Launcher và quản lý debug dùng chứng chỉ debug cục bộ, không phải chứng chỉ phát hành CleanTV; giữ keystore debug để cập nhật các bản thử sau. Sau build, kiểm tra chữ ký/package app quản lý tương tự launcher:

```powershell
& "$env:ANDROID_HOME\build-tools\35.0.0\apksigner.bat" verify --verbose --print-certs app/build/outputs/apk/debug/app-debug.apk
& "$env:ANDROID_HOME\build-tools\35.0.0\aapt.exe" dump badging app/build/outputs/apk/debug/app-debug.apk
```

Đã xác minh package quản lý `vn.anghon.aqua.manager`, launcher `vn.anghon.aqua.launcher`; cả hai có chữ ký hợp lệ cho API 28 và cùng chứng chỉ debug cục bộ. Launcher nhúng trong quản lý khớp SHA-256 với bản độc lập. Các APK gốc không đổi. Fingerprint và hash đầy đủ nằm trong báo cáo build mới.

## Kiểm thử đã làm và còn lại

Đã qua: hai build Android; 309 unit test launcher, 33 test quản lý và 7 test khoa (tổng 349, không lỗi/bỏ qua); gồm 4 test `AquaLiteTest` và 4 test `KichBanKhoiPhucTest`. Kiểm tra artifact phát hiện và đã sửa lỗi loại trừ assets: dùng bản assets lọc sinh trong `app/build` thay vì chỉ dùng `assets.exclude`. APK cuối không chứa hai ảnh nền VGS/macOS hoặc APK Xiaomi; launcher có thông báo MIT/OFL.

Còn phải kiểm thử trên TCL Android 9 khi được người dùng cho phép: mở Aqua từ danh sách app, thử D-pad/OK/Back/giữ OK, dock và ứng dụng gần đây; thử tên có dấu và các mốc chuyển buổi; đổi ảnh/màu/tên rồi đóng mở lại; kiểm tra launcher cũ vẫn mở được và không đổi Home mặc định; đo RAM và độ trễ focus. Chưa thực hiện các bước thiết bị này, chưa xác nhận hiệu năng hoặc tương thích remote thực tế.
