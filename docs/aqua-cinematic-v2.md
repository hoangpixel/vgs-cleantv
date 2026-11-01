# Ăng Hòn Aqua Cinematic v2 — triển khai và xác minh

Ngày 01/11/2026, Asia/Bangkok. **READY FOR TESTING** trên thiết bị thật, chưa xác nhận FPS hoặc độ mượt.

Hai APK debug đã build thành công và qua xác minh nội dung/chữ ký. Không kết nối ADB, cài/gỡ APK, đổi Home, debloat, reset, commit hoặc push. Hướng Aqua Minimal được thay bằng yêu cầu Cinematic; không triển khai hệ animation mới.

## Trạng thái công việc khi tiếp tục

Đã đọc `git status --short`, `git diff`, `git diff --stat`, `git diff --check`, danh sách untracked, ba tài liệu Aqua cũ và hai patch. Workspace vốn có 14 đường dẫn tracked báo sửa (13 file có diff nội dung; patch VGS chỉ khác trạng thái xuống dòng), cùng 17 file untracked. `.vscode/settings.json` là cấu hình có sẵn, không động tới.

V1 đã có package/chữ ký riêng, defaults, cấu hình qua điện thoại, đường áp theme giới hạn Aqua, khôi phục giới hạn package, chống nhúng APK cũ và hai APK đã build. Phần Minimal bị ngắt chỉ có bản sao source làm việc và bản lưu APK trong `.gradle`; chưa có thay đổi animation/layout Minimal trong patch đang dùng. Giữ các bản sao đó, không reset/checkout/clean/xóa công việc cũ. Cây `upstream/` vốn có patch từ các phiên trước; lượt này chỉ đọc, không áp patch vào đó.

## Baseline “Điện ảnh” thực tế

Không suy ra chế độ từ tên biến: preset có nhãn tiếng Việt **Điện ảnh**, ID `dien-anh`, nằm trong `app/src/main/java/zone/vgs/tvsach/dulieu/Theme.kt`, ghi đúng hai khóa `cinematic_home=true`, `sky_intensity=35`. Các giá trị còn lại lấy mặc định launcher hoặc lựa chọn riêng của người dùng. Đây là preset khác với **Kiểu Apple TV**, vốn đổi uniform tiles, fade rows, scrim và vị trí hàng.

Nguồn launcher khóa ở `fe9ad3204b945d3aadfb6876ee6ea78c20a1fe73`; áp `launcher-vgs.patch` rồi mới `launcher-aqua-lite.patch` trên archive mới. Mã Cinematic và các lớp sau đã tồn tại ở commit khóa, không phải implementation Aqua tạo lại:

| File trong `launcher/app/src/` | Vai trò và hành vi giữ nguyên |
| --- | --- |
| `main/res/values/config.xml`, `main/java/com/gathin/tvhome/Config.java` | Công tắc, mặc định và precedence cấu hình; animations bật, reduced motion tắt, advanced effects tắt. |
| `main/res/layout/activity_main.xml`, bản `layout-iw` | Đồng hồ/lời chào/lịch ở góc trái trên, cụm nút phải; tên app và meta/dots ở shelf phía trên dock, grid bên dưới. Không thay XML bố cục. |
| `main/java/com/gathin/tvhome/MainActivity.java` | `setUpCinematicShelf`: tên app 68sp, letter spacing −0.025, Last opened, dots, artwork và scrim. `onTileFocused`, `onTileVerticalKey`, `focusTile`, dispatch D-pad/OK/Back và cuộn RecyclerView giữ nguyên. |
| `main/java/com/gathin/tvhome/AppAdapter.java` | Tile/banner, nhãn, focus listeners và interpolator gốc; không đổi adapter hoặc quy trình nạp icon. |
| `main/java/com/gathin/tvhome/HeroArt.java` | Artwork từ app được thu nhỏ 48×27, saturation/box passes một lần rồi cache. Không phải blur hậu cảnh thời gian thực. |
| `main/java/com/gathin/tvhome/DockDecoration.java`, `GlassView.java` | Dock Cinematic trong tối (`0x8C121014`), edge trắng mờ; viền focus theo màu app trên dock, trắng ở grid. GlassView cache phần vẽ hiện có. |
| `main/java/com/gathin/tvhome/SkyView.java`, `Motion.java`, `GridNavigation.java`, `main/res/anim/tile_enter.xml` | Nền theo giờ, reduced-motion policy, điều hướng cạnh lưới và entrance gốc. |

Nền gốc tối; scrim có lớp ngang `0xD1060406 → transparent`, lớp dọc kết thúc `0xFF060406` để giữ chữ trắng và dock rõ. Artwork opacity 50%, scrim strength 100%, dots bật. Ambient lấy màu app, không phủ một màu xanh cố định lên nội dung.

Các thông số chuyển động thực tế, giữ nguyên với `advanced_effects=false`, `reduced_motion=false`:

| Chuyển động | Implementation gốc được tái sử dụng |
| --- | --- |
| Tile focus | Scale 1 → 1.10; vào 260ms, ra 180ms; OvershootInterpolator tension 1.6 khi vào, 0 khi ra. Nhãn alpha/translation 220ms, −6dp → +4dp. |
| Focus glass | Hai SpringAnimation theo tọa độ nội dung grid, stiffness 260, damping 0.58; scroll shift giữ glass đi cùng tile. Không có ValueAnimator thay thế. |
| Tên app | Lần đầu fade 260ms; đổi app fade-out/−12dp trong 120ms, rồi fade-in/+12dp → 0 trong 200ms. |
| Artwork | Fade-out 140ms, thay bitmap cache, fade-in 420ms; nhánh đầu vào 600ms. |
| Ambient | Fade đầu 600ms; đổi màu theo nhánh gốc 200ms/500ms, không có animated gradient mới. |
| Entrance | 420ms, decelerate_cubic, alpha/translation/scale, stagger 0.06. |
| Cuộn/remote | GridLayoutManager, smoothScrollBy/smoothScrollToPosition và focus search gốc; không áp duration tự tạo hoặc chặn phím mới. |

Script `scripts/review/kiem-tra-patch.ps1` xuất lại commit sạch, kiểm tra 10 file engine/layout giữ nguyên hash và 20 phương thức MainActivity giữ nguyên nội dung so với sau patch VGS. Sau đó so toàn bộ **247 file source/resource** với cây thực sự build. PASS. Script nay lấy cây build từ receipt, không so nhầm với cây `upstream/` v1.

## Nguyên nhân khựng: bằng chứng và giới hạn

**Đã xác định trong source:** Aqua v1 đặt `cinematic_home=false`, `animations=false`, `reduced_motion=true`, ambient tắt, glass rim và sky 0. Điều đó loại shelf Điện ảnh, bỏ scale/fade/entrance; focus glass nhảy ngay và cuộn dùng `scrollBy` thay cho nội suy. Đây là khác biệt trực tiếp giải thích cảm giác chuyển trạng thái tức thời; chưa chứng minh CPU/GPU bị rớt khung hình.

Patch Aqua v1 chỉ thêm màu, cache defaults và recreate khi người dùng áp theme; không thêm vòng lặp theo focus hoặc decode wallpaper ở mỗi nút remote. RecyclerView đã có `setHasFixedSize(true)` và `setItemAnimator(null)` từ baseline. Rebind toàn adapter khi reload/reorder không phải hành vi mới của Aqua; `TileRefresh` tránh rebuild không cần thiết khi resume. `requestLayout` đổi kích thước glass chỉ khi cần; dots chỉ đổi kích thước khi trạng thái đổi. Không sửa các đường này để “tối ưu” thiếu số liệu.

**Còn là giả thuyết cần đo:** lần focus đầu tạo HeroArt/lấy màu icon trên UI thread; PackageManager nạp icon/banner khi reload; decode ảnh nền đầu tiên chạy UI thread. Các việc này đều có trong baseline. HeroArt cache tránh làm lại cho mỗi lần quay về cùng app. Wallpaper mặc định v2 tắt; ảnh tĩnh riêng vẫn dùng decoder/lifecycle gốc, không decode theo focus. Stats/weather chỉ theo điều kiện và lịch hiện có; không thêm dịch vụ nền.

Debug Aqua không có R8/shrink như build release của launcher gốc. Đây là biến gây nhiễu tiềm năng cho startup/JIT/bộ nhớ, **không được xác nhận là nguyên nhân lag**. Chưa chạy trace, benchmark, emulator hoặc remote trên TCL cho v2. V2 khôi phục chuyển động được người dùng thích; không khẳng định FPS tăng, ít RAM hơn v1, hoặc mượt bằng/hơn bản gốc.

## Giao diện và lời chào v2

Giữ chính bố cục, dock, typography shelf, artwork theo app, scrim, ambient và animation Điện ảnh. Preset v2 bật lại các mặc định gốc bằng giá trị rõ ràng để không mang công tắc v1 sang: `animations=true`, `reduced_motion=false`, `focus_overshoot=true`, `cinematic_home=true`, `ambient_glow=true`, `glass_style=glass`, art 50, scrim 100, dots bật. Không bật advanced effects, TopFade/hardware layer toàn grid, uniform tiles hoặc dock glass kiểu Apple TV. `sky_intensity=35` giữ như preset Điện ảnh.

`AquaCinematic.background` tạo **một BitmapDrawable 480×270 ARGB_8888** khi Activity dựng nền: gradient navy `#102637 → #091923`, thêm radial aqua đậm `#168F9C` alpha tối đa 35% ở góc phải trên. Canvas/LinearGradient/RadialGradient chỉ dùng lúc tạo texture; khi focus chỉ draw texture tĩnh theo cách nền sky/scrim gốc. Thêm khoảng **518.400 byte (~0,49 MiB)** pixel buffer, chưa gồm overhead/texture GPU. Không có timer, shader động, invalidate loop, View overlay mới hoặc cache giữ Activity. Palette aqua sáng `#80DEDA` dành cho focus thường; trên dock Cinematic vẫn ưu tiên màu app để giữ phong cách gốc, grid dùng viền trắng như baseline. Không tải ảnh/logo bên thứ ba mới.

`aqua_cinematic` mặc định false khi thiếu, chỉ có tác dụng khi `cinematic_home=true`; preset khác giữ hành vi cũ. Khi chọn ảnh nền riêng, ảnh phủ thay gradient; artwork/scrim/ambient Cinematic vẫn hoạt động như bản gốc. Nền gradient không decode ảnh ngoài hoặc thay bitmap trong mỗi lần focus.

`AquaGreeting.text` đọc `Calendar.getInstance()` theo **giờ/múi giờ TV**, không dùng giờ điện thoại hay offset lịch âm. Nội dung:

| Giờ TV | Ví dụ mặc định |
| --- | --- |
| 05:00–11:59 | Xin chào, Ăng Hòn! Chúc buổi sáng tốt lành. |
| 12:00–16:59 | Xin chào, Ăng Hòn! Chúc buổi chiều vui vẻ. |
| 17:00–21:59 | Xin chào, Ăng Hòn! Chúc buổi tối thư giãn. |
| 22:00–04:59 | Xin chào, Ăng Hòn! Chúc ngủ ngon. |

Tái sử dụng `minuteTick`, canh đầu phút cho Aqua; chạy lại khi resume và receiver TIME_SET/TIMEZONE_CHANGED hiện có. Handler được gỡ khi pause theo lifecycle cũ, không thêm timer render. Giữ vị trí và font Lexend của patch VGS, bỏ all-caps/letter spacing quá rộng cho câu dài, autosize 11–14sp, ellipsize cuối vẫn giữ để tên rất dài không đè nút. Chỉ gọi setText khi câu đổi ở nhánh Aqua. Font tiếng Việt/bố cục hiển thị thực tế vẫn cần xem trên TCL.

## Cấu hình từ Manager và chuyển từ v1

Giữ ID theme `ang-hon-aqua-lite` để nhập cấu hình cũ, đổi nhãn preset thành **Ăng Hòn Aqua Cinematic v2**. Giữ package, icon ứng dụng và signing key hiện có; tên hiển thị APK vẫn là Aqua Lite. Không đổi identity để tạo một vùng dữ liệu mới ngoài ý muốn.

Sau khi người dùng chủ động cập nhật hai APK, **chọn lại preset v2 và Áp dụng**. Chọn preset sẽ bỏ công tắc “TV yếu” còn giữ từ v1; defaults trong APK không tự ghi đè config đã lưu trên TV. Nâng cao có quyền ưu tiên: kiểm tra các override `animations=false`, `reduced_motion=true`, `cinematic_home=false` hoặc công tắc TV yếu nếu trước đây đã tự đặt. Không tự xóa lựa chọn người dùng. File xuất v1 nhập lại vẫn là cấu hình v1 cho đến khi chọn/áp preset mới.

Tên ở mục Đồng hồ & lời chào ghi vào `greeting_name`, giữ qua JSON/provider và xuất/nhập hiện có. Nâng cao có thêm `aqua_gradient_color`, `aqua_night_color`; dùng `background_color` cho màu đầu gradient, `focus_accent_color` cho focus thường, `header_color` cho chữ. Không cần build lại khi đổi tên/màu/ảnh. Ảnh riêng vẫn qua bộ chọn ảnh tĩnh có sẵn; muốn trở về gradient, bỏ ảnh riêng/bộ ảnh xoay rồi chọn lại preset. Các màu quá sáng có thể làm giảm tương phản; preset mặc định dùng các màu tối ở trên.

Áp theme vẫn dùng `dayGiaoDienAqua`: ghi ảnh/config/theme_revision trong package Aqua rồi action APPLY_THEME để recreate. Không gọi nhánh sửa quyền/Home/debloat. Luồng tự cài/thay Home vẫn chặn. Không sửa ADB transport, danh sách debloat hoặc logic sao lưu/khôi phục trong đợt này. Wi-Fi/ADB, xuất/nhập, lịch âm và chức năng CleanTV khác dùng implementation có sẵn; chưa kiểm thử end-to-end trên TV trong lượt này.

## Build, test và artifact thực tế

Môi trường: Gradle 8.9; runtime JDK 21.0.12, target bytecode 17; SDK `C:\Users\mhoang\AppData\Local\Android\Sdk`; launcher compile/target 34, Build-Tools 34.0.0, AGP 8.5.2; Manager compile/target 35, Build-Tools 35.0.0, AGP 8.7.2. Dùng cache `.gradle/aqua-cache` và chế độ online, không `--offline`. Lượt này không cần cài thêm SDK/dependency.

```powershell
$gradleBuild = "$env:USERPROFILE/.gradle/wrapper/dists/gradle-8.9-bin/90cnw93cvbtalezasaz0blq0a/gradle-8.9/bin/gradle.bat"
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/dung-aqua-lite.ps1 -PrepareOnly
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/dung-aqua-lite.ps1 -GradleExecutable $gradleBuild
& $gradleBuild --no-daemon --console=plain -g .gradle/aqua-cache -PaquaLite=true testDebugUnitTest :app:assembleDebug
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/review/kiem-tra-patch.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/kiem-tra-aqua-lite.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/xac-minh-aqua-lite.ps1
git diff --check
```

Kết quả cuối: launcher **BUILD SUCCESSFUL (35s)**, Manager **BUILD SUCCESSFUL (32s)**, các script/whitespace PASS. Trước đó một lần gõ sai đường dẫn Gradle dừng trước khi build; một lượt Manager bị guard chặn `Stale launcher: aquaPatchSha256 changed` vì chuẩn hóa header patch sau build đầu. Đã chạy lại toàn bộ script launcher từ patch cuối, rồi build Manager và verifier thành công, không sửa receipt thủ công hoặc bỏ guard. Cảnh báo cũ SDK XML v4/v3, deprecated API và trailing space trong giấy phép patch VGS không chặn build; không sửa giấy phép chỉ để bỏ warning. Không chạy lint toàn bộ hoặc instrumentation.

| Phạm vi | Suite | Test | Fail / error / skip |
| --- | --- | --- | --- |
| Launcher | 45 | 313 | 0 / 0 / 0 |
| Manager `app` | 9 | 35 | 0 / 0 / 0 |
| `khoa` | 1 | 7 | 0 / 0 / 0 |
| Tổng | 55 | **355** | **0 / 0 / 0** |

`AquaGreetingTest`: 4 test, gồm toàn bộ 1.440 phút/ngày, ranh giới các buổi, đổi timezone cùng một instant, tên tiếng Việt/fallback và lịch tick. `AquaLiteTest`: 6 test, preset/defaults khớp, giữ cấu hình dock/remote, giữ baseline Điện ảnh, màu đi qua catalog/serialization, nhập preset và lọc thương hiệu. `KichBanKhoiPhucTest`: 4 test PASS. `khoa` không đổi nên Gradle báo UP-TO-DATE, không tính lặp test của nhiều lượt build.

Source build cuối: `.gradle/aqua-launcher/275e0a25ac8c4946bc36f945a133443e/launcher/`. Báo cáo launcher ở `app/build/reports/tests/testDebugUnitTest/` bên trong cây này; Manager và khoa có cùng đường dẫn báo cáo dưới module tương ứng của project chính.

| APK | Đường dẫn tương đối workspace | Package | versionCode | minSdk / target | Dung lượng |
| --- | --- | --- | --- | --- | --- |
| Launcher | `app/src/aqua/assets/tvhome.apk` | `vn.anghon.aqua.launcher` | 11 | 26 / 34 | 2.275.538 byte |
| Manager | `app/build/outputs/apk/debug/app-debug.apk` | `vn.anghon.aqua.manager` | 110 | 26 / 35 | 29.799.046 byte |

Workspace: `C:\Users\mhoang\OneDrive\Desktop\vgs-cleantv`.

```text
launcher SHA-256 4dbe2084de6570cb594775152a21117c3b84f7ef4dc416c6bc3cb455de04bb34
manager  SHA-256 380cde68d459460c75f6193e575f78c9622b9946677fa605687b48ac35c1ea43
certificate     5b6ffaad8776e93270563667d2b0c28349de8e528d1fdb5576977c158388f486
```

Chữ ký v2 scheme hợp lệ cho **API 28**; cùng certificate Aqua v1. VersionCode giữ như bản thử trước, dùng hash nhận diện artifact v2. Hai package riêng không cập nhật đè `zone.vgs.cleantv`/`com.gathin.tvhome`; cập nhật Aqua giữ dữ liệu Aqua. Launcher không có native library; Manager có `armeabi-v7a` và `arm64-v8a`. minSdk 26 phù hợp Android 9, chưa phải kiểm chứng runtime.

Verifier kiểm tra SHA launcher nhúng trong Manager **khớp tuyệt đối** APK độc lập, defaults khớp source, marker code AquaCinematic/AquaGreeting trong DEX, authority riêng, receipt, giấy phép MIT/OFL và loại wallpaper VGS/macOS/APK Xiaomi. Task Gradle còn đối chiếu SHA hai patch/commit/defaults trước merge assets. Kết quả máy đọc: `.gradle/aqua-build-verification.json`.

Hai APK gốc trong main assets không thay đổi:

```text
tvhome.apk        e65ad1fc7faa88403bda24b7c1044585b92cf1c33f63bcccadf1e063f54bca21
tvhome-xiaomi.apk 5c2181500069e9dd3c364d49b54d484516ebda5547dfc8e0a92eec102dd96bc5
launcher-vgs.patch 542693f8b40f600f085a12747b30f74c4625af7b2e436ffbe468111ebee7816b
```

## File thay đổi trong đợt Cinematic

- `patches/launcher-aqua-lite.patch`: giữ thay đổi v1, thêm AquaCinematic/AquaGreeting/test và hook opt-in trong Config/MainActivity. Các file Java thực sự biên dịch được tạo trong archive build, không sửa upstream tại chỗ.
- `themes/ang-hon-aqua-lite/config.json`, `app/src/main/java/zone/vgs/tvsach/dulieu/Theme.kt`: preset Cinematic mới, palette/gradient và motion baseline.
- `app/src/main/java/zone/vgs/tvsach/ui/PhienLamViec.kt`: khi chọn preset v2, bỏ reduced-motion của lựa chọn v1.
- `app/src/main/assets/nang-cao.json`: hai màu gradient và mô tả precedence màu focus.
- `app/src/test/java/zone/vgs/tvsach/dulieu/AquaLiteTest.kt`: kiểm tra Cinematic và cấu hình màu.
- `app/build.gradle.kts`: guard DEX yêu cầu cả code Cinematic v2.
- `scripts/kiem-tra-aqua-lite.ps1`, `scripts/xac-minh-aqua-lite.ps1`, `scripts/review/kiem-tra-patch.ps1`: xác minh preset mới, APK và tái sử dụng engine.
- Tài liệu này và liên kết/cảnh báo lịch sử trong `ang-hon-aqua-lite.md`, `aqua-lite-build-verification.md`.

Tổng **13 file nguồn/cấu hình/script/tài liệu** cập nhật hoặc thêm trong đợt này; không nhầm với toàn bộ dirty state từ các giai đoạn trước. APK/receipt/build outputs là file sinh ra, được ignore. Bản v1 đã lưu ở `.gradle/aqua-before-minimal/` để tham chiếu, không xóa.

## Đo chỉ đọc và kiểm thử trên TCL

Các lệnh sau **chỉ đề xuất**, chưa thực thi. Dùng phiên ADB do người dùng đã chủ động thiết lập; nếu có nhiều thiết bị, thêm `-s SERIAL` cho từng lệnh. Không cần `connect`, reset counter hoặc inject phím trong quy trình đo này:

```powershell
adb shell dumpsys display
adb shell dumpsys gfxinfo vn.anghon.aqua.launcher framestats > aqua-before.txt
adb shell dumpsys meminfo vn.anghon.aqua.launcher > aqua-memory-before.txt
# Người dùng tự thao tác remote 20–30 giây với chuỗi trái/phải/lên/xuống cố định.
adb shell dumpsys gfxinfo vn.anghon.aqua.launcher framestats > aqua-after.txt
adb shell dumpsys meminfo vn.anghon.aqua.launcher > aqua-memory-after.txt
adb shell dumpsys cpuinfo > cpu-after.txt
adb shell dumpsys thermalservice > thermal-after.txt
adb shell df -h /data
adb shell content read --uri content://vn.anghon.aqua.launcher.theme/config.json > aqua-config-current.json
```

Đo CleanTV Điện ảnh tương tự bằng package `com.gathin.tvhome`, khi người dùng tự mở bản đó. Lệnh `thermalservice` có thể không tồn tại trên firmware Android 9; ghi lại lỗi, không thay cấu hình để ép chạy. Đọc config có thể chứa tên/vị trí/IP của người dùng: che thông tin trước khi chia sẻ.

Giữ cùng màn hình/resolution/tần số quét, danh sách app/dock, ảnh nền và tải nền; xác nhận config thực dùng animations true/reduced false. Lấy các frame mới giữa hai snapshot theo IntendedVsync, bỏ mẫu trùng/flags không hợp lệ; `framestats` là buffer hữu hạn nên giữ lượt đo ngắn. Tính duration = FrameCompleted − IntendedVsync, p50/p95 và tỷ lệ vượt ngân sách theo refresh thực (60Hz ~16,7ms; 30Hz ~33,3ms), đọc janky frames và RAM PSS. Phân biệt lần đi qua app đầu tiên với lượt lặp sau cache. `cpuinfo`/thermal chỉ là bối cảnh, không đủ tự kết luận nguyên nhân. Muốn quy trách nhiệm chính xác cho UI thread/GPU cần trace thiết bị sau khi người dùng cung cấp số liệu.

Checklist remote thật: trái/phải nhanh và giữ phím; lên/xuống dock/grid; các cạnh lưới; OK mở app, Back/Home trở lại và Last opened; toolbar/Inputs/volume; reorder; greeting khi 04:59→05:00, 11:59→12:00, 16:59→17:00, 21:59→22:00; đổi timezone/resume/reboot; tên dài và dấu tiếng Việt; ảnh riêng/gradient; nhập/xuất màu/tên. Không suy ra các mục này đã PASS từ unit test.

Trước thử, xuất cấu hình Aqua hiện tại; giữ CleanTV/TCL launcher đang chạy và không thay Home mặc định. Cập nhật chỉ hai package Aqua bằng thao tác do người dùng chủ động, rồi mở Aqua từ danh sách ứng dụng. Nếu trải nghiệm không ổn, chuyển về launcher gốc bằng giao diện TV; nhập lại config Aqua đã xuất để quay lại look cũ. Không cần gỡ CleanTV/SmartTube hoặc disable gói hệ thống. Lượt làm việc này chỉ tạo và xác minh APK, chưa triển khai thiết bị.
