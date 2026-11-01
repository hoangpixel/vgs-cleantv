# Ăng Hòn Aqua Galaxy v2.3 — Header & Cosmic Decor

01/11/2026, Asia/Bangkok. **READY FOR VISUAL REVIEW**. Hai APK đã build/xác minh, 382 kết quả test PASS hiện có được kiểm tra lại trực tiếp từ XML. Ưu tiên người dùng xem preview trước khi quyết định cập nhật TV. Chưa cài hoặc đo v2.3 trên TCL. Không dùng ADB, đổi Home, debloat, gỡ CleanTV/SmartTube, xóa dữ liệu, reset Git, commit hoặc push.

## Xác minh khi tiếp tục phiên bị ngắt

Không triển khai lại, sửa source, chạy lại Gradle/test/build hoặc tạo lại preview. Chỉ đọc workspace, XML và APK; chạy verifier metadata/chữ ký/ZIP hiện có (chỉ cập nhật JSON báo cáo trong `.gradle`). Artifact đang có hợp lệ và được giữ nguyên.

**382 là số đúng, không phải 379.** Các file `TEST-*.xml` hiện tại cho kết quả:

| Nguồn XML tương đối workspace | Suite | Test | Failure / error / skipped |
| --- | --- | --- | --- |
| `.gradle/aqua-launcher/f1dbb0542b7d4a398c5152b869969130/launcher/app/build/test-results/testDebugUnitTest/` | 48 | 332 | 0 / 0 / 0 |
| `app/build/test-results/testDebugUnitTest/` | 11 | 43 | 0 / 0 / 0 |
| `khoa/build/test-results/testDebugUnitTest/` | 1 | 7 | 0 / 0 / 0 |
| Tổng | 60 | **382** | **0 / 0 / 0** |

Đã đối chiếu thuộc tính `tests` của từng XML với số node `testcase`, không cộng test từ các cây build cũ. Tất cả suite Manager cuối có timestamp XML từ `2026-11-01T04:50:08` đến `2026-11-01T04:50:10`; launcher từ `04:47:08` đến `04:47:09`. Khoa giữ kết quả cache ngày 31/10, source module không đổi (`git diff --exit-code -- khoa` PASS); không coi 7 test đó là vừa chạy lại trong phiên resume.

Thống kê **332 + 40 + 7 = 379** trước đó được đọc khi Manager v2.3 vẫn đang build, nên thư mục Manager còn kết quả 40 test cũ. Kết quả cuối thêm đúng **3 test của `AquaCosmicTest`**, file [TEST-zone.vgs.tvsach.dulieu.AquaCosmicTest.xml](../app/build/test-results/testDebugUnitTest/TEST-zone.vgs.tvsach.dulieu.AquaCosmicTest.xml), timestamp XML `2026-11-01T04:50:08`:

- `defaultsMatchManagerAndStackedHeaderIsEnabled`
- `nameDockAndRemoteSurviveCosmicPresetAndRoundTrip`
- `galaxyPaletteAndMotionAreInheritedWithoutChangingOldPreset`

Không sửa số liệu để tăng tổng, không cộng lặp các lượt build. Dữ liệu cuối đủ tin cậy, không cần chạy lại test.

Kiểm tra mới cũng xác nhận:

- `git diff --check` PASS; giữ nguyên toàn bộ dirty changes/source của phiên trước.
- Hai APK tồn tại, versionName 2.3, versionCode thực tế 14/113; SHA-256 và certificate khớp phần artifact bên dưới.
- Manager nhúng đúng byte launcher độc lập: verifier đọc `assets/tvhome.apk` và đối chiếu SHA-256. Defaults/Dex/receipt/authority/giấy phép hợp lệ.
- So hai APK v2.2 đã lưu: cùng package/certificate, versionCode tăng; chữ ký xác minh API 28 PASS. Chưa thử cài trên TV.
- Receipt hash khớp APK, cả hai patch và defaults hiện tại, không có dấu hiệu launcher cũ hoặc source patch đã đổi sau build.
- 11 file engine/layout/greeting giống byte với source build v2.2. Toàn bộ `MainActivity` giống v2.2 sau khi loại đúng hai dòng thay đổi thuộc hook header; không có thay đổi khác trong navigation/animation/time flow.
- Snapshot bảo vệ xác nhận upstream, VGS patch, APK CleanTV gốc và defaults v2.1/v2.2 giữ nguyên.
- Đã mở xem preview hiện có: đủ Idle/artwork/focus, lời chào dưới giờ/ngày/lịch âm, bán nguyệt và sao thưa bên phải, không đè vùng chữ trái trong ảnh. Không tạo lại PNG.

Preview tuyệt đối:

```text
C:\Users\mhoang\OneDrive\Desktop\vgs-cleantv\.gradle\aqua-v23-review\cosmic-comparison.png
SHA-256 a9d1d527ba5281ef3b63eab5f1a041355e0e0c1804102f0d0bcfa5e898e636d5
```

Ảnh dùng texture production nhưng header/card là mô phỏng bố trí; không chứng nhận layout Android, contrast ở mọi điều kiện hoặc frame time trên TCL. Không có animation trang trí liên tục trong source. Cần người dùng đánh giá ảnh, sau đó mới quyết định thử thiết bị.

**Không cần build lại. Giữ nguyên hai APK và preview hiện tại.** Lượt resume chỉ bổ sung phần xác minh trong tài liệu này; danh sách 14 file triển khai v2.3 bên dưới là công việc đã có từ phiên trước.

## Header mới

Thêm preset riêng **Ăng Hòn Aqua Galaxy — Cinematic v2.3**, ID `ang-hon-aqua-cosmic`. Giữ nguyên preset/file cấu hình v2.1 và v2.2 để quay lại.

Trong preset mới, `aqua_cosmic=true` cùng `date_below_clock=true`. Launcher dùng nguyên hàm `moveDateBelowClock()` sẵn có để chuyển cột ngày/lịch âm xuống dưới hàng đồng hồ. Sau đó `AquaHeader.apply()` chuyển chính TextView lời chào xuống cuối LinearLayout, không tạo TextView mới:

1. Hàng đồng hồ và AM/PM.
2. Cột thứ/ngày, lịch âm và weather nếu đã bật.
3. Lời chào theo giờ TV.

Lời chào có margin trên 6dp, autosize 11–12sp, alpha 0,82 để là thông tin phụ. Giữ font Lexend, single-line/ellipsis và giới hạn chiều rộng theo header; không đổi chuỗi chào hoặc logic thời gian. Đồng hồ vẫn kích thước gốc 52sp trước các tùy chỉnh sẵn có. Hàm date-below-clock gốc ẩn divider dọc khi date chuyển xuống dưới.

Preset đặt `top_buttons_top=true` bằng tùy chọn hiện có để cụm nút vẫn ở hàng trên. Manager phân biệt giá trị do theme sở hữu với override của người dùng khi đọc config, tránh mang lựa chọn vị trí nút của v2.3 sang preset cũ ngoài ý muốn.

Chỉ reparent lời chào một lần khi Activity dựng giao diện. Không reflow theo focus, không thêm timer. Bố trí Android thực tế, tên dài, weather và header scale vẫn cần xem trên TCL; kiểm tra source/preview không thay thế kiểm thử layout thiết bị.

## Trang trí cosmic

`CosmicDecor.paint()` vẽ sẵn vào **mảng pixel của scrim Galaxy hiện có**:

- Một bán nguyệt cách điệu nhỏ, đường kính khoảng 20 pixel trong texture 480×270; vị trí tâm `(413,49)`, phía phải trên và ngoài vùng chữ trái.
- Năm điểm sao thưa tại các tọa độ cố định; hai điểm có cross-sparkle rất nhỏ, alpha thấp.
- Halo/crescent được tính từ distance falloff trong vùng nhỏ, không dùng runtime blur hoặc ảnh bên thứ ba.

**Tất cả tĩnh.** Không blink, pulse, particle system, shader animation, dịch vụ hoặc tác vụ định kỳ. Không tạo bitmap/View riêng cho hành tinh hay sao. Cùng bitmap ARGB_8888 **480×270 = 518.400 byte (~0,49 MiB)** và cache theo Activity như v2.2. Chi phí trang trí chỉ lúc bake; không tính lại khi focus/đổi app. Không tăng số lớp toàn màn hình hoặc lượt vẽ nền.

Layer giữ nguyên:

```text
root / wallpaper → Sky → artwork → ambient
→ heroScrim (Galaxy + decor tĩnh) → nội dung/toolbar/dock → focus glass
```

Không đổi thuật toán/màu Galaxy v2.2. Khi không bật cosmic, texture Galaxy có đúng hash v2.2. Khi bật, test xác nhận chỉ dưới **1% pixel** thay đổi, nằm trong các vùng hành tinh/sao được khai báo ở nửa phải; mọi pixel ngoài vùng đó giữ nguyên. Decor mirror cùng texture khi RTL. Không thêm trang trí vào renderer v2.1.

```text
Texture v2.2 ARGB SHA-256
46e44756d7cf4c920c88f5558042f2be818dda077de58e66c9d9f545aa509b84

Texture v2.3 ARGB SHA-256
fc37adc2cc58ceaac569ca4a3afd96e7b02bc1ce3bb7a8f3e9fba5a7dc428471
```

Spring, interpolator, duration, D-pad, RecyclerView scrolling/focus, dock navigation và GlassView không thay đổi. Giữ API/package/signing và cơ chế apply theme hiện có. `MainActivity` chỉ thêm điều kiện/hook header trong onCreate; các phương thức chuyển động không sửa. Logic `AquaGreeting`, minuteTick, receiver giờ/timezone, resume và font giữ nguyên.

Không khẳng định v2.3 đã giữ FPS/cold start/RAM PSS trên TCL chỉ từ source. Header cao hơn có thể ảnh hưởng không gian còn lại trên độ phân giải/font scale khác; đây là mục cần kiểm tra thực tế. Lượt timing host cũng không phải phép đo TV.

## Preview

[cosmic-comparison.png](../.gradle/aqua-v23-review/cosmic-comparison.png): cột trái v2.2, phải v2.3; ba hàng Idle, artwork và focused app. Đã tạo và xem trước build, rồi render lại từ source launcher thực sự build.

Pixel Galaxy/decor/compositing dùng đúng code production. Header/card là bố trí tham chiếu, thời gian/ngày là fixture; ảnh ghi **SCHEMATIC LAYOUT / FIXTURE TIME — NOT A TV SCREENSHOT**. Không hardcode giờ trong launcher.

Preview cho thấy lời chào dưới cụm ngày, nhỏ và dịu hơn đồng hồ; bán nguyệt/sao thưa, không lấn vùng tên app. Không thấy banding lớn ở PNG. Test các điểm lời chào với alpha 0,82 trên underlay trắng đạt contrast ≥7:1; đây là kiểm tra điểm mẫu, không chứng nhận mọi ảnh/màu tùy chỉnh hoặc panel TCL.

Tái tạo preview từ root:

```powershell
$receipt = Get-Content app/src/aqua/assets/aqua-launcher.properties
$launcherSource = ($receipt | Where-Object { $_ -like 'buildSourcePath=*' }).Substring(16)
New-Item -ItemType Directory -Force .gradle/aqua-v23-review/classes | Out-Null
& "$env:JAVA_HOME/bin/javac.exe" -encoding UTF-8 --release 17 -d .gradle/aqua-v23-review/classes "$launcherSource/app/src/main/java/com/gathin/tvhome/AquaSurface.java" "$launcherSource/app/src/main/java/com/gathin/tvhome/CosmicDecor.java" scripts/review/RenderAquaCosmic.java
& "$env:JAVA_HOME/bin/java.exe" '-Djava.awt.headless=true' -cp .gradle/aqua-v23-review/classes com.gathin.tvhome.RenderAquaCosmic .gradle/aqua-v23-review/cosmic-comparison.png "$launcherSource/app/src/main/res/font/lexend.ttf"
```

## Build, test và hồi quy

Giữ build archive cô lập tại commit `fe9ad3204b945d3aadfb6876ee6ea78c20a1fe73`, áp VGS rồi Aqua. Không sửa upstream hiện có. Gradle 8.9, runtime JDK 21.0.12, bytecode 17; SDK từ local.properties, API/Build-Tools 34 launcher, 35 Manager; AGP 8.5.2/8.7.2. Online, không `--offline`, không thêm dependency.

```powershell
$gradleBuild = "$env:USERPROFILE/.gradle/wrapper/dists/gradle-8.9-bin/90cnw93cvbtalezasaz0blq0a/gradle-8.9/bin/gradle.bat"
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/dung-aqua-lite.ps1 -GradleExecutable $gradleBuild
& $gradleBuild --no-daemon --console=plain -g .gradle/aqua-cache -PaquaLite=true testDebugUnitTest :app:assembleDebug
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/review/kiem-tra-patch.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/kiem-tra-aqua-lite.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/xac-minh-aqua-lite.ps1 -PreviousLauncherApk .gradle/aqua-v23-review/launcher-v22.apk -PreviousManagerApk .gradle/aqua-v23-review/manager-v22.apk
git diff --check
```

Launcher **BUILD SUCCESSFUL (1m19s)**, Manager **BUILD SUCCESSFUL (2m20s)**. Không có lỗi build/test cần sửa trong lượt này. Các warning SDK XML/deprecated API/trailing space giấy phép VGS có sẵn không chặn build; không sửa source ngoài phạm vi chỉ để loại warning.

| Module | Suite | Test | Fail/error/skip |
| --- | --- | --- | --- |
| Launcher | 48 | 332 | 0/0/0 |
| Manager | 11 | 43 | 0/0/0 |
| khoa | 1 | 7 | 0/0/0 |
| Tổng | 60 | **382** | **0/0/0** |

CosmicDecorTest 5 test: hash Galaxy v2.2, vùng thay đổi <1%, tĩnh/RTL, v2.1 không bị thêm decor, contrast lời chào. AquaCosmicTest 3 test: defaults/header flags, kế thừa palette/motion/preset cũ, tên/dock/remote qua preset/export/import. Tất cả test greeting/Galaxy/AquaLite/khôi phục cũ tiếp tục PASS. Khoa không đổi, Gradle UP-TO-DATE; không cộng trùng lượt chạy.

Script review xác nhận **253 file source/resource** khớp cây build, hai patch áp đúng, **9 file engine/layout và 19 phương thức chuyển động/điều hướng** giống baseline sau VGS. Kiểm thêm hook header chỉ bật cosmic và chạy sau hàm chuyển date; GlassView geometry/render giữ nguyên. Đây là kiểm tra source/structural; không phải instrumentation xác nhận tọa độ View trên TV.

Source build và report launcher:

```text
.gradle/aqua-launcher/f1dbb0542b7d4a398c5152b869969130/launcher/
  app/build/reports/tests/testDebugUnitTest/index.html
```

Report Manager/khoa ở `app/build/reports/tests/testDebugUnitTest/` và `khoa/build/reports/tests/testDebugUnitTest/` của project chính. Không chạy emulator, instrumentation, lint toàn bộ hoặc benchmark TCL.

## Hai APK cuối

Root: `C:\Users\mhoang\OneDrive\Desktop\vgs-cleantv`.

| APK | Đường dẫn | Package | Version/code | minSdk/target | Byte |
| --- | --- | --- | --- | --- | --- |
| Launcher | `app/src/aqua/assets/tvhome.apk` | `vn.anghon.aqua.launcher` | 2.3 / 14 | 26 / 34 | 2.280.090 |
| Manager | `app/build/outputs/apk/debug/app-debug.apk` | `vn.anghon.aqua.manager` | 2.3 / 113 | 26 / 35 | 28.862.094 |

```text
Launcher SHA-256 a4cdac16e884f03d90bb3e64cb4397370d02bba431472dc858f9f69b8cf3a84c
Manager  SHA-256 036f6eda1dc383755b13d95f8a705030be0c79f2aca305fc259c2fe36ad381eb
Certificate     5b6ffaad8776e93270563667d2b0c28349de8e528d1fdb5576977c158388f486
```

apksigner xác minh API 28 hợp lệ. Package/certificate khớp hai APK v2.2 giữ trước task; versionCode tăng 13→14 và 112→113, đủ điều kiện metadata cập nhật giữ dữ liệu. Chưa cài thử trên TCL. Launcher không có native libs, Manager có ARMv7/ARM64.

Launcher nhúng trong Manager có SHA **a4cd…a84c**, khớp APK độc lập. Defaults cosmic khớp source; DEX có `aqua_cosmic`, AquaHeader và CosmicDecor; receipt khớp commit/hai patch/defaults. Giữ giấy phép MIT/OFL; không nhúng nhầm launcher gốc hoặc tài sản VGS/macOS. Kết quả máy đọc: `.gradle/aqua-build-verification.json`.

Snapshot trước/sau xác nhận upstream source, patch VGS, hai APK gốc và defaults v2.1/v2.2 không đổi. Không sửa ADB transport, debloat, sao lưu/khôi phục hoặc SmartTube.

## File cập nhật trong đợt v2.3

- `patches/launcher-aqua-lite.patch`: thêm AquaHeader/CosmicDecor/CosmicDecorTest; hook header, cờ cosmic, factory và version launcher; giữ thuật toán Galaxy.
- `themes/ang-hon-aqua-cosmic/config.json`: preset mới; không sửa hai defaults cũ.
- `app/src/main/java/zone/vgs/tvsach/dulieu/Theme.kt`: thêm preset v2.3 từ bản sao map v2.2.
- `app/src/main/java/zone/vgs/tvsach/ui/PhienLamViec.kt`: default preset mới; không coi vị trí nút do theme sở hữu là override người dùng.
- `app/src/test/java/zone/vgs/tvsach/dulieu/AquaCosmicTest.kt`: ba test mới.
- `app/build.gradle.kts`: version 2.3/113, defaults/guard cosmic.
- `scripts/dung-aqua-lite.ps1`, `scripts/kiem-tra-aqua-lite.ps1`, `scripts/xac-minh-aqua-lite.ps1`: defaults/version/code/upgrade phù hợp.
- `scripts/review/kiem-tra-patch.ps1`: structural check cho header opt-in.
- `scripts/review/RenderAquaCosmic.java`: preview ba trạng thái, không vào APK.
- Báo cáo này, `docs/aqua-lite-build-verification.md`, `docs/ang-hon-aqua-lite.md`.

Tổng 14 file source/config/test/script/tài liệu, ngoài APK/receipt/build outputs/preview và source copy cô lập. Không động `.vscode/settings.json`; không reset các dirty changes trước đó.

## Cần kiểm chứng trên TCL

- Header thật: thứ tự giờ/ngày/lịch âm/lời chào, khoảng cách với hero/dock, cụm nút trên, weather và tên dài/font scale.
- Độ rõ bán nguyệt/năm sao từ xa, tương phản trên artwork sáng, banding sau upscale.
- D-pad nhanh/giữ phím, focus/dock/grid và frame time so với v2.2 đã thử tốt; không suy ra FPS từ test source.
- Cold start/recreate, RAM và việc xuất/nhập tên/màu/ảnh sau cập nhật.

Khi người dùng tự cập nhật hai APK, **chọn và Áp dụng preset “Ăng Hòn Aqua Galaxy — Cinematic v2.3”**. APK không tự ghi đè config cũ. Có thể chọn lại v2.2/v2.1 để quay về bố cục/nền cũ; xuất config trước thử. Không cần thay Home mặc định hoặc gỡ launcher gốc.
