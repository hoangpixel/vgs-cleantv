# Final Code Review & Verification — Ăng Hòn Aqua Lite

Đây là bản ghi trước khi môi trường SDK được khắc phục. Kết quả hiện tại đã được cập nhật trong [báo cáo build và xác minh APK](aqua-lite-build-verification.md): hai APK debug đã build, 349 unit test đạt, trạng thái READY FOR TESTING. Giữ phần bên dưới làm lịch sử audit, không dùng trạng thái BLOCKED cũ để xác định kết quả hiện tại.

Ngày: 31/10/2026, Asia/Bangkok. Kết luận: **BLOCKED**.

Đã hoàn tất đợt review source và sửa các lỗi an toàn phát hiện được trong workspace. Chưa tạo APK Aqua; chưa có build Android thành công. Không chạy lệnh ADB lên thiết bị, cài/gỡ APK, đổi Home, tắt gói, xóa dữ liệu TV, reset Git, commit hoặc push. Không cài SDK/JDK hay chỉnh cấu hình ngoài workspace. Có đọc thư viện Gradle/JUnit/apksig đã có, với quyền thực thi được cấp; output và cây nguồn kiểm tra sinh trong `.gradle/aqua-review` của workspace.

## 1. Danh sách file

Toàn bộ thay đổi giai đoạn 1 và Final Review, gồm file mới chưa commit:

| Nhóm | File |
| --- | --- |
| Git / đóng gói | `.gitattributes`, `.gitignore`, `app/build.gradle.kts`, `app/src/main/AndroidManifest.xml`, `gradle/wrapper/gradle-wrapper.properties` |
| Cấu hình / theme | `app/src/main/java/zone/vgs/tvsach/dulieu/Theme.kt`, `app/src/main/assets/nang-cao.json`, `themes/ang-hon-aqua-lite/config.json` |
| UI / phiên làm việc | `app/src/main/java/zone/vgs/tvsach/ui/UngDung.kt`, `app/src/main/java/zone/vgs/tvsach/ui/PhienLamViec.kt` |
| OTA | `app/src/main/java/zone/vgs/tvsach/CapNhat.kt`, `app/src/main/java/zone/vgs/tvsach/ThongBaoCapNhat.kt` |
| Tách đích launcher / khôi phục | `app/src/main/java/zone/vgs/tvsach/adb/ThaoTacTv.kt`, `app/src/main/java/zone/vgs/tvsach/dulieu/KichBanKhoiPhuc.kt` |
| Biểu tượng riêng | `app/src/aqua/res/drawable/aqua_icon.xml`, `app/src/aqua/res/drawable/aqua_banner.xml` |
| Patch launcher | `patches/launcher-aqua-lite.patch` |
| Build / kiểm tra | `scripts/dung-aqua-lite.ps1`, `scripts/kiem-tra-aqua-lite.ps1`, `scripts/review/kiem-tra-patch.ps1`, `scripts/review/kiem-tra-jvm.ps1`, `scripts/review/SyntaxCheck.java`, `scripts/review/VerifyApk.java` |
| Test Kotlin | `app/src/test/java/zone/vgs/tvsach/dulieu/AquaLiteTest.kt`, `app/src/test/java/zone/vgs/tvsach/dulieu/KichBanKhoiPhucTest.kt` |
| Tài liệu | `docs/ang-hon-aqua-lite.md`, `docs/aqua-lite-final-review.md` |

`patches/launcher-vgs.patch` chỉ được chuẩn hóa xuống dòng LF; `git diff --numstat` không có thay đổi nội dung nguồn so với Git. Giữ cả thông báo bản quyền và giấy phép font. Hai APK `app/src/main/assets/tvhome*.apk` không thay đổi.

Thư mục `upstream/` bị Git chính bỏ qua. Trong đó Aqua thực sự sửa `launcher/app/src/main/java/com/gathin/tvhome/Config.java`, `MainActivity.java`, `Weather.java`, bên trên toàn bộ patch VGS đã áp. Tất cả thay đổi Aqua được giữ trong patch có context đầy đủ, không phụ thuộc duy nhất vào cây upstream bị ignore. Script build sẽ sinh tên/icon/banner/mặc định riêng sau khi qua kiểm tra môi trường.

`.vscode/settings.json` xuất hiện trong trạng thái Git khi review; không phải file do đợt review này tạo/sửa. Không động tới file đó.

## 2. Chức năng đã có trong source

- Nền `#102637`, focus `#80DEDA`, chữ trắng, dock 5 cột; dùng cơ chế View/RecyclerView của launcher hiện có.
- Tên mặc định `Ăng Hòn`, sửa/lưu qua `greeting_name`; lời chào dùng đồng hồ và bốn buổi sẵn có. Preset giữ locale tiếng Việt sau khi điện thoại áp lại cấu hình.
- Ảnh tĩnh, màu nền, màu focus và tên đi qua JSON/provider hiện có; không cần build lại khi thay các giá trị này. File cấu hình mặc định có định dạng nhập được để đặt lại màu/tên/ảnh theo preset.
- `reduced_motion=true`; animation, advanced effects, ambient/app glow, dock glass, cinematic và fade rows tắt mặc định. Viền `rim` không vẽ các vòng glow của GlassView. Không thêm video/blur/timer hay dịch vụ nền. OTA CleanTV và lịch kiểm tra OTA tắt trong bản Aqua.
- Cơ chế focus D-pad, OK, Back, dock và thông tin ứng dụng gần đây của upstream được giữ; chưa xác nhận bằng remote TCL thật. Các nút đặc thù cần helper/quyền riêng vẫn phụ thuộc cài đặt sẵn có, bản áp theme không tự cấp quyền.
- Package quản lý `vn.anghon.aqua.manager`; launcher `vn.anghon.aqua.launcher`; provider `${applicationId}.theme` riêng. Namespace Java/Kotlin giữ tên nguồn để tránh đổi toàn bộ các lớp.

## 3. Lỗi phát hiện và sửa

| Mức độ | Phát hiện | Sửa trong Final Review |
| --- | --- | --- |
| Cao | Nhánh “chỉ áp dụng theme” gọi `dayCauHinh`, có `batLaiGoiLoi`, sửa quyền/appops, canh gác và khởi động lại kèm viết settings trợ năng | Thêm `dayGiaoDienAqua`, chỉ ghi file của launcher Aqua và gọi action riêng để Activity `recreate()`. Chặn luồng tự cài/thay Home trong Aqua trước mọi lệnh TV. |
| Cao | Chỉ kiểm tra `tvhome.apk` tồn tại; có thể đóng gói APK cũ hoặc APK không đúng patch | Thêm receipt SHA-256 và task `verifyAquaLauncher`: đối chiếu APK, commit, hai patch, mặc định thực sự trong ZIP và chuỗi mã Aqua trong DEX trước merge assets. Script build xác minh chữ ký/package trước khi xuất receipt. Chưa chạy được task Gradle này do plugin/SDK. |
| Cao | Một số nhánh nhả Home/khôi phục không sao lưu và kịch bản xuất vẫn nhắm launcher CleanTV gốc | Giới hạn tập launcher theo bản Aqua; kịch bản xuất nhận tập package riêng. Hành vi mặc định của bản CleanTV vẫn giữ tập cũ. Thêm test khôi phục riêng cho Aqua, chưa chạy được test Kotlin này. |
| Trung bình | Chỉ `am start` một Activity đang chạy không đảm bảo đọc lại theme | Thêm action `<package>.action.APPLY_THEME`, xử lý tại `onNewIntent`; không force-stop hay viết lại trợ năng. |
| Trung bình | Trình đọc màu/header VGS không đọc asset mặc định Aqua; hai nơi có thể dùng mặc định khác nhau | Dùng chung `Config.aquaDefaults` có cache nhỏ một lần mỗi process, không lưu Context/Activity. |
| Trung bình | Patch kiểm tra bằng các hunk thiếu context; kiểm tra VGS ngược hỏng khi Aqua sửa tiếp Weather | Sinh lại patch Aqua có context đầy đủ. Kiểm tra trên cây nguồn sạch tại commit khóa, áp lần lượt VGS/Aqua và so toàn bộ file nguồn/resource. Không reset upstream. |
| Trung bình | Kết nối Aqua có thể ghi lại bản sao lưu CleanTV trên TV dù chỉ muốn đổi theme | Bản Aqua chỉ ghi bản đã đọc vào kho cục bộ lúc kết nối. Các thao tác sao lưu/khôi phục chủ động vẫn được giữ. |
| Trung bình | Logo CleanTV trên màn đầu và theme hình nền VGS/macOS vẫn được chào mời trong bản riêng | Dùng biểu tượng giọt nước trên màn Aqua; lọc theme có thương hiệu; loại hai ảnh và APK Xiaomi khỏi assets của build Aqua. Giữ copyright/giấy phép và nội dung ghi nhận nguồn. |
| Build | URL Gradle 9.7.1 đi với SHA của 8.9 | Sửa về cặp URL/SHA 8.9 của repo, giữ xác minh checksum. |
| Cấu hình | File mặc định không có marker nhập cấu hình/theme; locale có thể mất sau lần áp từ điện thoại | Thêm `vgs_file`, `vgs_theme`, và `locale=vi` trong preset điện thoại. Cập nhật preview lệnh Aqua đúng với hành vi gửi cấu hình. |

Không thêm dependency Android. Không thay transport ADB, danh sách/rules debloat, kế hoạch tắt gói, schema sao lưu hay APK gốc. Các sửa trong `ThaoTacTv` là tách đích và bỏ tác động hệ thống khỏi đường theme; các nhánh quản lý TV tổng quát vẫn là chức năng CleanTV hiện có và không dùng để cài thử giao diện.

## 4. Test và build thực tế

| Kiểm tra | Kết quả thực tế |
| --- | --- |
| `git status`, toàn bộ diff và file mới | Đã rà soát; không commit/push. |
| `git diff --check` | PASS. |
| `scripts/kiem-tra-aqua-lite.ps1` | PASS: palette, preset nhẹ, cấu hình nhập lại được, đường theme không chứa các thao tác sửa hệ thống bị cấm, XML, catalog không trùng và hai APK gốc không đổi. Đây là kiểm tra tĩnh. |
| `scripts/dung-aqua-lite.ps1 -PrepareOnly` | PASS. Áp hai patch lên commit `fe9ad3204b945d3aadfb6876ee6ea78c20a1fe73`; **244** file nguồn/resource khớp cây launcher hiện có. |
| Parser Java/Kotlin | **123 Java + 31 Kotlin/script Gradle, 0 lỗi cú pháp**. Java parse với `--release 17`; Kotlin parser 1.9.23 có sẵn trong Gradle. Không phải type-check, Android lint hay biên dịch Compose. |
| JUnit JVM không cần SDK | **41 test PASS**: `WallpaperSizeTest`, `TileRefreshTest`, `GridNavigationTest`, `InputNamesTest`, `DockSlotsTest`. Biên dịch source thật với Java target 17, chạy trên JDK 21; không thay Android API bằng stub. |
| Test cần Android / Kotlin app | Chưa chạy được: `AquaLiteTest`, `KichBanKhoiPhucTest` và toàn bộ suite Gradle. Không tính vào 41 test đã qua. |
| Build quản lý Aqua | Thử Gradle 8.9 có sẵn với `--offline --no-daemon -g .gradle/aqua-cache -PaquaLite=true :app:testDebugUnitTest :app:assembleDebug`; **FAILED** tại resolve `com.android.application:8.7.2`, vì cache riêng chưa có plugin. Chưa vào biên dịch/merge/package. |
| Script build launcher | **BLOCKED** tại preflight yêu cầu JDK 17, thực tế `JAVA_HOME` là JDK 21.0.12. Không chạy tới Gradle launcher. |
| SDK | `ANDROID_HOME`/`ANDROID_SDK_ROOT` trống; không thấy SDK ở `%LOCALAPPDATA%\Android\Sdk`, `C:\Android` hoặc đường dẫn Studio đã kiểm tra. Không có `local.properties` chỉ SDK. Cần API 34/35 và build-tools. |
| Chữ ký/package APK gốc | PASS bằng thư viện apksig 8.7.2 đã có; chi tiết bên dưới. |
| UI, RAM/FPS, remote và reboot TV | Chưa thực hiện. Không có thiết bị/emulator được sử dụng. |

Kiểm tra Java từng gặp lỗi đọc JAR trong sandbox; đã chạy lại với quyền đọc thư viện được cấp và có kết quả sạch `OK (41 tests)`, exit code 0. Lần thử chọn test cần Android đã được loại khỏi tập độc lập SDK; không coi các lần kiểm tra ban đầu thất bại là test ứng dụng đã qua.

## 5. APK và chữ ký

**Không có APK Aqua mới để cung cấp.** Hai đường dẫn dưới đây chưa tồn tại và chỉ là output dự kiến:

- Launcher: `app/src/aqua/assets/tvhome.apk`.
- Quản lý: `app/build/outputs/apk/debug/app-debug.apk`.

APK thực tế trong `app/src/main/assets` là bản gốc, không phải Aqua:

| APK | Package đọc từ binary manifest | versionCode | min/target SDK | Kết quả chữ ký cho API 28 |
| --- | --- | --- | --- | --- |
| `tvhome.apk` | `com.gathin.tvhome` | 11 | 26 / 34 | Hợp lệ, v2 |
| `tvhome-xiaomi.apk` | `com.ts.launcher` | 11 | 26 / 34 | Hợp lệ, v2 |

Cả hai không chứa `assets/aqua-defaults.json`; không có entry native `lib/`. Fingerprint SHA-256 chứng chỉ signer chung: `ed11469b82cf0df4b4880c3df82c5cfe6a79518da292a14cac4e2c8c2532c208`.

Hash SHA-256 file gốc giữ nguyên:

```text
tvhome.apk        e65ad1fc7faa88403bda24b7c1044585b92cf1c33f63bcccadf1e063f54bca21
tvhome-xiaomi.apk 5c2181500069e9dd3c364d49b54d484516ebda5547dfc8e0a92eec102dd96bc5
```

Source bản Aqua giữ versionCode launcher 11 và quản lý 110, nhưng applicationId khác bản gốc nên không phải bản cập nhật của CleanTV. Bản preview dùng debug signing cục bộ, không dùng signingConfig VGS; cần kiểm tra fingerprint thực tế sau build và giữ cùng keystore cho các bản cập nhật preview sau. Nếu TV đã có app không liên quan trùng package Aqua, phải dừng để kiểm tra; không dùng `install -r` mù quáng.

Build Aqua chỉ được phép debug. Guard receipt/DEX giúp ngăn nhầm bản cũ, không thay thế việc xác minh chữ ký, package và so hash APK lồng bên trong **APK quản lý cuối cùng**. Việc kiểm tra artifact cuối này còn chờ build thành công; không đưa ra xác nhận đóng gói hoàn chỉnh lúc chưa có artifact.

## 6. Chưa xác minh được

- Type-check/link/resource merge của app, dependency resolution, R8/lint, test Android/Kotlin và chữ ký của APK Aqua.
- APK quản lý cuối cùng chứa đúng byte APK launcher đã xác minh; không có ABI `armeabi-v7a` bị loại khi đóng gói dependency native của quản lý. Source đã có bộ lọc ARMv7; launcher dự kiến thuần Java nhưng phải kiểm tra artifact mới.
- Tên có dấu và lời chào tại mốc 05:00, 12:00, 17:00, 22:00 trên TCL; lịch âm/giờ TV thực tế.
- Thay ảnh/tên/màu, đóng mở process, Activity `recreate()`, điều hướng D-pad/OK/Back và các phím TCL riêng.
- Lưu và nhập lại cấu hình trên điện thoại; file mặc định khôi phục preset nhưng không tự xóa lựa chọn đã lưu trong Launcher settings trên TV. Nếu từng đổi Wallpaper/Animations tại TV, cần kiểm tra lựa chọn đó riêng; không tự xóa dữ liệu để reset.
- RAM thực tế, FPS, độ trễ focus, dữ liệu cache và `/data` còn trống. Bản debug chưa tối ưu R8, không phù hợp để suy ra hiệu năng bản phát hành.

## 7. Rủi ro trên TCL Android 9 / ARMv7 / RAM 1.37 GiB

Chưa đủ điều kiện đánh giá là an toàn để cài ngay. Source mới không thấy API Android vượt API 28; launcher minSdk 26, dùng các API View/Intent/InputStream tương thích và tái sử dụng xử lý remote của upstream. Chữ ký APK gốc hợp lệ cho API 28 chỉ chứng minh khả năng xác minh chữ ký, không chứng minh launcher Aqua hoạt động.

Preset không có bitmap wallpaper mặc định; không bật blur/video/cinematic, không có vòng animation mới. Wallpaper tĩnh được decode theo kích thước màn hình qua pipeline sẵn có, không decode mỗi frame. Executor wallpaper được đóng trong vòng đời có sẵn; cache mặc định Aqua chỉ giữ JSON nhỏ, không giữ Activity/Context. Không phát hiện leak mới từ diff, nhưng chưa profiling.

Rủi ro lớn còn lại là artifact/build chưa xác minh và bộ nhớ TV hạn chế. Dung lượng `/data` tổng 4.2 GiB không cho biết dung lượng còn trống. Cần đo trước khi sideload, tránh ảnh lớn/bộ ảnh xoay và không bật helper tự mở Home trong thử nghiệm đầu. Việc có cùng lúc hai launcher không làm cả hai có hiệu năng như nhau hoặc cùng hoạt động nhẹ; phải đo khi mở/thoát app.

Ứng dụng quản lý vẫn giữ các chức năng debloat/khôi phục tổng quát theo yêu cầu. Chúng có thể thay đổi hệ thống khi người dùng chủ động gọi; **không sử dụng các chức năng đó làm quy trình rollback cho lần thử theme**. Chỉ dùng luồng cấu hình Aqua riêng và mở Activity rõ package.

## 8. Quy trình cài thử / lui lại sau khi được phép

Các bước sau là hướng dẫn cho tương lai, chưa được thực hiện. Chỉ bắt đầu khi đã có APK mới được xác minh và người dùng cho phép thử trên TV.

1. Hoàn thiện JDK 17, SDK API 34/35, build-tools và dependency cache theo [hướng dẫn build](ang-hon-aqua-lite.md); chạy lại suite Gradle, build và các bước xác minh artifact. Giữ checksum Gradle. Không sao chép APK gốc vào chỗ output Aqua để vượt guard.
2. Với kết nối được người dùng cho phép, ghi lại Home hiện tại, danh sách package Aqua nếu có, fingerprint bản sẽ cài, dung lượng `/data` còn trống và xuất cấu hình CleanTV gốc từ app gốc. Các lệnh thu thập chỉ đọc; không thay Home hay helper.
3. Xác nhận launcher APK là `vn.anghon.aqua.launcher`, quản lý là `vn.anghon.aqua.manager`, minSdk phù hợp và chữ ký hợp lệ. Kiểm tra APK launcher trích từ APK quản lý có cùng SHA-256 với launcher đã ký. Cài quản lý lên điện thoại, chỉ cài launcher riêng lên TV sau khi được phép; không cài APK quản lý lên TV trong lần thử này.
4. Mở Aqua từ danh sách ứng dụng, hoặc mở component `vn.anghon.aqua.launcher/com.gathin.tvhome.MainActivity`. Không chọn “Luôn luôn” trong hộp chọn Home, không chạy `set-home-activity`, không vô hiệu hóa Home gốc, không cấp dịch vụ bắt phím/overlay/tự khởi động. Nếu có hộp chọn Home, giữ lựa chọn đang dùng ổn định.
5. Thử D-pad/OK/Back, dock, mở/thoát app, đồng hồ/lịch âm; đổi từng mục tên/màu/ảnh tĩnh từ quản lý Aqua. Kiểm tra Home mặc định không đổi và launcher cũ vẫn mở được. Đo RAM/độ trễ sau khi để yên và sau nhiều lần mở app; không coi build thành công là bằng chứng mượt.
6. Muốn về giao diện Aqua mặc định: nhập `themes/ang-hon-aqua-lite/config.json` rồi áp dụng trong app Aqua, hoặc nhập bản cấu hình Aqua đã xuất trước đó. File mặc định giữ dock hiện có; kiểm tra riêng lựa chọn wallpaper/animation từng đổi trên TV. Không clear data để “sửa nhanh”.
7. Muốn dừng thử: nhấn Home để về launcher cũ vì mặc định vẫn giữ nguyên, hoặc mở launcher cũ từ danh sách ứng dụng. Chỉ khi được phép mới gỡ đúng `vn.anghon.aqua.launcher`; trên điện thoại chỉ gỡ `vn.anghon.aqua.manager` nếu muốn. Không dùng “Khôi phục không sao lưu”, không gỡ `com.gathin.tvhome`/`com.ts.launcher`/`zone.vgs.cleantv`, không reset TV.

**BLOCKED** cho tới khi có môi trường Android đầy đủ, build/test Gradle thành công và APK Aqua được xác minh. Ngay cả sau đó chỉ có thể chuyển sang READY FOR TESTING; độ mượt và ổn định cần kiểm thử có kiểm soát trên TCL.
