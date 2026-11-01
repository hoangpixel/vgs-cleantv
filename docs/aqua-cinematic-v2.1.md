# Aqua Cinematic v2.1 — Visible Aqua Design Fix

01/11/2026, Asia/Bangkok. **READY FOR TESTING**: sửa render đã build và kiểm tra pixel/source/APK; chưa kiểm chứng thị giác hoặc frame time v2.1 trên TCL. Phản hồi người dùng xác nhận bố cục và độ mượt **v2** tốt; không dùng phản hồi đó làm kết quả đo cho **v2.1**.

Không chạy ADB, cài APK, đổi Home, debloat, sửa thiết bị, commit, push hoặc reset Git. Giữ package và key ký Aqua. Không sửa logic đồng hồ/lời chào, font, ADB, sao lưu/khôi phục hoặc quy tắc hệ thống.

## 1. Nguyên nhân v2 chìm màu: đường render thực tế

Đối chiếu source thực sự build v2 tại `.gradle/aqua-launcher/275e0a25ac8c4946bc36f945a133443e/launcher/`, patch và defaults trong APK v2:

- `MainActivity.onCreate` gắn `AquaCinematic.background(...)` vào **background của root**, chỉ khi `aqua_cinematic=true` và wallpaper tắt. Texture 480×270 không phải lớp trên artwork.
- XML vẽ theo thứ tự: **root background → SkyView → heroArt → ambient → heroScrim → hero và nội dung/toolbar/grid → focus glass**. Hai bản layout thường/Hebrew cùng thứ tự này. Không thấy elevation/translationZ đổi thứ tự các lớp nền.
- Preset v2 có `sky_intensity=35`, `cinematic_art=50`, `cinematic_scrim=100`, `ambient_glow=true`. Sky alpha 0,35; artwork alpha đích 0,50; ambient là mask đổi tint theo app. Scrim còn phủ tối phía chữ và phần dưới, kết thúc bằng alpha 255 ở đáy. Root vẫn được vẽ, nhưng đóng góp màu của nó bị giảm qua các lớp phía trên, có vùng bị che hoàn toàn.
- V2 chỉ pha deep aqua ở root với alpha tối đa `0x59` (~35%), chưa dùng aqua sáng trong nền. Cách đặt và cường độ này không bảo đảm nhận diện Aqua khi artwork/ambient xuất hiện. Lỗi này thuộc thiết kế render v2, không cần giả định TV đã làm mất texture.
- `onTileFocused` chọn accent theo app ở dock hoặc trắng trong grid; `GlassView.setAccent` còn nâng accent về trắng 2/3. Vì vậy `focus_accent_color=#80DEDA` ở đầu Activity không giữ được một rim Aqua rõ ràng.
- Đổi app gọi `showHeroArt` và `setAmbient`, không thay root gradient trực tiếp. WallpaperPlayer có thể clear/khôi phục background root khi dùng ảnh; nó không ghi lên `heroScrim`. Đây là lý do gradient ở root cũng không phù hợp khi chọn wallpaper riêng.

Đã mở ZIP APK v2 và xác nhận các defaults trên, cùng hash APK v2 khớp báo cáo trước. Preset Manager/JSON/provider và đường lưu `theme_revision` vẫn nhất quán. **Không đọc được config thực đang lưu trên TCL trong task này**, vì không dùng ADB; không khẳng định TV đang dùng defaults nếu người dùng có override riêng.

## 2. Sửa render và màu nhận diện

Gradient nay là **background của chính View `heroScrim`**, nằm **trên cả artwork và ambient, dưới text/dock/toolbar/focus**. Không thêm View hoặc đổi thứ tự/kích thước/vị trí thành phần. `scrimDrawable()` chỉ thêm guard cho Aqua; nhánh scrim CleanTV cũ giữ nguyên. `setUpCinematicShelf`, fade artwork và điều hướng không đổi.

`AquaSurface` tạo pixel ARGB của một scrim tĩnh:

| Thành phần | Màu / cách dùng |
| --- | --- |
| Vùng chữ phía trái | Navy `#102637`, shadow tối giữ tương phản. |
| Vùng giữa/phải | Deep Teal `#126A76`, pha mềm với artwork còn nhìn xuyên qua. |
| Vùng sáng gần góc phải trên | Aqua `#80DEDA`, glow định vị tĩnh, không phủ cyan toàn màn hình. |
| Mép trên và phía dưới | Shadow Dark Navy `#091923` bảo vệ toolbar và dock. |
| Focus rim | Đúng Aqua `#80DEDA`, không nâng thành gần trắng; giữ độ dày/hình học cũ. |
| Vạch cạnh đồng hồ | Aqua nhẹ; giữ kích thước/vị trí cũ. |
| Chữ, tên app, lời chào, lịch âm | Giữ trắng, font và bố cục hiện có. |

Art opacity 50%, scrim strength 100%, ambient và các tùy chọn motion giữ như v2. Artwork nằm **dưới scrim Aqua** nên không thể che nó theo thứ tự vẽ; nội dung app vẫn đi qua alpha của scrim. Các fade artwork/ambient vẫn tạo chuyển động nền gốc, không có animated gradient mới.

`GlassView` chỉ thêm rim override: glow vẫn theo app (hoặc vàng khi reorder), rim Aqua giữ nguyên trên dock/grid, kể cả reorder. Khi chủ động bật festival, không áp rim/divider Aqua để giữ màu lễ hội. `setStyle`, `setRadius`, `onSizeChanged`, `onDraw` và hardware-layer cache của GlassView không đổi.

Nâng cao vẫn đổi màu qua `aqua_gradient_color`, `aqua_night_color`, `background_color`, `focus_accent_color`. Trong v2.1, focus accent cũng làm màu sáng của gradient và vạch đồng hồ. Wallpaper riêng nằm dưới scrim nên vẫn phối Aqua; không còn phủ mất nhận diện như v2. Nếu tự đặt `cinematic_scrim=0`, tắt cinematic/aqua hoặc chọn màu khác, kết quả sẽ theo override đó.

## 3. Bảo vệ animation và hiệu năng

- Không sửa spring stiffness/damping, duration, interpolator, scale/translation, D-pad/OK/Back, RecyclerView scrolling/focus hoặc dock navigation.
- Script review xuất archive commit khóa `fe9ad3204b945d3aadfb6876ee6ea78c20a1fe73`, áp VGS/Aqua, so **249 file source/resource** với cây build thực tế và cả `app/build.gradle`.
- **9 file engine/layout** giữ hash so với CleanTV sau VGS: AppAdapter, Motion, HeroArt, DockDecoration, GridNavigation, WallpaperPlayer, hai activity_main XML và tile_enter.xml.
- **19 phương thức MainActivity** điều hướng/chuyển động giữ nguyên nội dung: dispatch, focus tile, di chuyển dọc, refit glass, shelf/artwork/meta/dots/ambient/fade/hide/reload. Phương thức `scrimDrawable` được kiểm riêng: bỏ đúng guard Aqua mới thì toàn thân còn lại khớp baseline.
- GlassView được so 4 phương thức vẽ/hình học; `setAccent` chỉ được thêm phép gán màu rim. Không đổi constructor/hardware layer hoặc shader sheen cũ. Kiểm tra XML xác nhận scrim ở trên art/ambient và dưới nội dung.
- `AquaCinematic.scrim` cache một BitmapDrawable **480×270 ARGB_8888 = 518.400 byte (~0,49 MiB)**, tạo khi dựng Activity. Tính pixel và mảng tạm chỉ xảy ra lần đầu, không trên focus, onDraw hay minuteTick. Không có thread/service/timer mới.
- V2 có một texture root Aqua **và** scrim gốc; v2.1 bỏ texture root và thay scrim gốc bằng scrim màu, nên số texture nền thường trú giảm một so với thiết kế v2. Đây là tính từ source, **không phải số đo RAM PSS trên TV**. Mảng tạm 0,49 MiB được bỏ tham chiếu sau tạo bitmap.
- Không thêm lớp phủ toàn màn hình, invalidate loop, blur GPU, video, shader động hoặc cấp phát bitmap theo app/focus. Chi phí composite scrim dùng vị trí/lượt vẽ có sẵn. Pixel chuẩn bị một lần trên CPU vẫn có thể ảnh hưởng thời gian mở Activity; phải kiểm tra cold start trên TCL.

Lệnh render host đo khoảng 11,52ms cho lần chuẩn bị texture trên máy tính này; **không ngoại suy thành thời gian trên ARMv7, FPS hoặc cam kết không có regression**.

## 4. Lời chào

`AquaGreeting.java` có hash nguyên vẹn so với v2, cùng 4 unit test cũ PASS. Không sửa `updateGreeting`, minuteTick, receiver giờ/timezone, resume, font Lexend hoặc autosize hiện có. Vẫn “Xin chào, Ăng Hòn!” cùng lời chúc sáng/chiều/tối/đêm, dùng giờ/múi giờ TV và tên lưu trong config. Điểm nhấn Aqua nằm ở vạch đồng hồ, không tô cả câu cyan.

## 5. Kiểm tra render thật từ pixel dùng trong APK

`AquaSurfaceTest` có **9 test PASS**: SRC_OVER/alpha/thứ tự phối, Aqua trên artwork đỏ/trung tính/trắng/tối, artwork còn nhìn qua, độ tương phản vùng chữ/dock/toolbar, rim chính xác và fallback theme thường, RTL mirror, texture hữu hạn/ổn định và palette chỉnh được.

Test mở rộng từng phát hiện toolbar trắng trên artwork trắng chỉ đạt **~2,85:1**. Đã thêm top shadow tĩnh trong cùng texture; test cuối yêu cầu **≥4,5:1** tại các điểm toolbar, **≥7:1** tại các điểm chữ trái/dock trên nền trắng. Đây là kiểm tra các điểm mẫu với palette mặc định, không chứng nhận mọi vị trí/màu tùy chỉnh hoặc cách panel TCL hiển thị.

Đã tạo và xem [ảnh kiểm tra phối màu](../.gradle/aqua-v21-review/composition-check.png): ba đầu vào artwork mô phỏng đỏ/xanh/trắng, hàng dưới được compositing bằng **đúng pixel production** của AquaSurface. Aqua nhìn rõ với các đầu vào đó. Ảnh ghi rõ **NOT A TV SCREENSHOT**; không dùng nó như bằng chứng render Android hay đo hiệu năng thiết bị.

Tái tạo ảnh (chạy từ root, sau khi có source launcher theo receipt):

```powershell
$receipt = Get-Content app/src/aqua/assets/aqua-launcher.properties
$launcherSource = ($receipt | Where-Object { $_ -like 'buildSourcePath=*' }).Substring(16)
New-Item -ItemType Directory -Force .gradle/aqua-v21-review/classes | Out-Null
& "$env:JAVA_HOME/bin/javac.exe" -encoding UTF-8 --release 17 -d .gradle/aqua-v21-review/classes "$launcherSource/app/src/main/java/com/gathin/tvhome/AquaSurface.java" scripts/review/RenderAquaSurface.java
& "$env:JAVA_HOME/bin/java.exe" '-Djava.awt.headless=true' -cp .gradle/aqua-v21-review/classes com.gathin.tvhome.RenderAquaSurface .gradle/aqua-v21-review/composition-check.png "$launcherSource/app/src/main/res/font/lexend.ttf"
```

## 6. Build, test, APK

Giữ quy trình archive cô lập của `scripts/dung-aqua-lite.ps1`; không sửa `upstream/`. Gradle 8.9, runtime JDK 21.0.12, bytecode 17; SDK từ `local.properties`, launcher API/Build-Tools 34 và Manager API/Build-Tools 35. AGP launcher 8.5.2, Manager 8.7.2. Chế độ online, không `--offline`, không thêm dependency.

```powershell
$gradleBuild = "$env:USERPROFILE/.gradle/wrapper/dists/gradle-8.9-bin/90cnw93cvbtalezasaz0blq0a/gradle-8.9/bin/gradle.bat"
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/dung-aqua-lite.ps1 -GradleExecutable $gradleBuild
& $gradleBuild --no-daemon --console=plain -g .gradle/aqua-cache -PaquaLite=true testDebugUnitTest :app:assembleDebug
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/review/kiem-tra-patch.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/kiem-tra-aqua-lite.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/xac-minh-aqua-lite.ps1
git diff --check
```

Kết quả cuối: **launcher BUILD SUCCESSFUL (22s)**; **Manager BUILD SUCCESSFUL (12s)**; patch/source/render guard, configuration-only Aqua path, whitespace và APK verification PASS. Manager test đã chạy trong lượt compile trước đó của cùng source v2.1; lượt cuối chỉ đổi launcher nhúng nên Gradle tái dùng kết quả hợp lệ. Không cộng lặp số test giữa nhiều lượt build.

| Phạm vi | Suite | Test | Fail / error / skip |
| --- | --- | --- | --- |
| Launcher | 46 | 322 | 0 / 0 / 0 |
| Manager | 9 | 36 | 0 / 0 / 0 |
| khoa | 1 | 7 | 0 / 0 / 0 |
| Tổng | 56 | **365** | **0 / 0 / 0** |

Trong đó AquaSurfaceTest 9, AquaGreetingTest 4, AquaLiteTest 7, KichBanKhoiPhucTest 4 PASS. Khoa không đổi, Gradle UP-TO-DATE. Test HTML nằm ở `app/build/reports/tests/testDebugUnitTest/` trong project/module tương ứng; source launcher cuối:

```text
.gradle/aqua-launcher/db1f0ec4ad4e4b5faff7b5c06287f5d4/launcher/
```

Trong kiểm tra phụ, script compare ban đầu vướng CRLF/LF ở dòng guard mới; đã xác nhận thân phương thức giống nhau sau normalize và sửa checker normalize xuống dòng. Lệnh javac/JUnit thủ công có in AccessDeniedException lúc đóng JAR của JDK zipfs, dù vẫn sinh class/chạy test; không lấy đó làm bằng chứng build. Kết quả Gradle đầy đủ cuối cùng ở trên đã biên dịch/test/package thành công. Render host cũng từng cần quote `-Djava.awt.headless=true` trong PowerShell; lệnh đúng được ghi ở trên. Còn các warning SDK XML v4/v3, deprecated API và trailing whitespace giấy phép VGS có sẵn; không sửa giấy phép hoặc source ngoài phạm vi. Không chạy instrumentation, emulator, lint toàn bộ hoặc benchmark TCL.

| APK | Đường dẫn tương đối | Package | versionName / Code | minSdk / target | Byte |
| --- | --- | --- | --- | --- | --- |
| Launcher | `app/src/aqua/assets/tvhome.apk` | `vn.anghon.aqua.launcher` | 2.1 / 12 | 26 / 34 | 2.276.786 |
| Manager | `app/build/outputs/apk/debug/app-debug.apk` | `vn.anghon.aqua.manager` | 2.1 / 111 | 26 / 35 | 31.135.027 |

Root: `C:\Users\mhoang\OneDrive\Desktop\vgs-cleantv`.

```text
Launcher SHA-256 bc5c23d3b3b595d096dfbed7d4e95098014589c74dec7751f3844e2aa0b30691
Manager  SHA-256 4ae2e2236fcf72c85cb0565f57544818530a6d7fe6e24e74518107030c474252
Certificate     5b6ffaad8776e93270563667d2b0c28349de8e528d1fdb5576977c158388f486
```

Đã so certificate với **cả hai APK v2 đã lưu trước task**: giống nhau. apksigner xác minh API 28 hợp lệ. Launcher không có native libraries; Manager chứa ARMv7 và ARM64. Hai applicationId không đổi. VersionCode chỉ tăng ở build Aqua; giá trị version của CleanTV gốc giữ nguyên.

Manager nhúng `assets/tvhome.apk` có SHA **bc5c…0691**, khớp hoàn toàn launcher độc lập. Defaults trong launcher khớp config source; DEX có AquaSurface/setRimAccent; receipt khớp APK, commit/hai patch/defaults. Giấy phép MIT/OFL giữ lại, không thêm ảnh/logo bên thứ ba. APK Manager không có wallpaper thương hiệu VGS/macOS hoặc APK Xiaomi. Kết quả máy đọc: `.gradle/aqua-build-verification.json`.

Hash snapshot trước/sau xác nhận source `upstream/launcher/app/src`, patch VGS và hai APK main assets không đổi. Đã lưu bản v2 trong `.gradle/aqua-v21-review/`, không xóa/reset công việc cũ.

## 7. File cập nhật trong đợt này

| File | Thay đổi |
| --- | --- |
| `patches/launcher-aqua-lite.patch` | Chứa AquaSurface mới/test; sửa AquaCinematic, nhánh chọn scrim trong MainActivity, màu rim trong GlassView, fallback teal trong Config; version riêng trong launcher/app/build.gradle. AquaGreeting giữ nguyên. |
| `themes/ang-hon-aqua-lite/config.json` | Deep teal `#126A76`; motion/layout/alpha artwork giữ nguyên. |
| `app/src/main/java/zone/vgs/tvsach/dulieu/Theme.kt` | Nhãn v2.1 và palette mới, không đổi preset Điện ảnh gốc. |
| `app/src/main/assets/nang-cao.json` | Mô tả đúng scrim trên artwork và rim Aqua. |
| `app/src/test/java/zone/vgs/tvsach/dulieu/AquaLiteTest.kt` | Một test palette v2.1/artwork/scrim. |
| `app/build.gradle.kts` | Version riêng Aqua 2.1/111 và guard DEX v2.1. |
| `scripts/review/kiem-tra-patch.ps1` | Kiểm giới hạn render, giữ engine/layout/geometry và thứ tự XML; normalize CRLF/LF. |
| `scripts/kiem-tra-aqua-lite.ps1` | Kiểm palette và motion baseline. |
| `scripts/xac-minh-aqua-lite.ps1` | Kiểm versionName/versionCode, marker v2.1 và certificate hai APK khớp nhau. |
| `scripts/review/RenderAquaSurface.java` | Công cụ PNG host từ pixel production; không đóng vào APK. |
| `docs/aqua-cinematic-v2.1.md` | Báo cáo này. |
| `docs/aqua-lite-build-verification.md`, `docs/ang-hon-aqua-lite.md` | Trỏ kết quả mới, giữ báo cáo v2/v1 lịch sử. |

Tổng 13 file source/config/script/tài liệu ở project chính thêm/cập nhật trong task, ngoài output build/APK/receipt/ảnh kiểm tra và bản sao source cô lập. Git vốn có nhiều thay đổi từ các phiên trước; không đánh đồng toàn bộ git diff với riêng v2.1. `.vscode/settings.json` không sửa.

## 8. Cần kiểm chứng trên TCL

- Màu Aqua đủ rõ trên panel TCL và chế độ hình ảnh hiện dùng, đặc biệt YouTube/app đỏ, app xanh, ảnh nền sáng; banding của texture 480×270 khi phóng lên màn hình.
- Đọc chữ/toolbar từ xa, viền Aqua trên banner sáng và khi reorder. Màu tùy chỉnh quá sáng hoặc `cinematic_scrim` thấp có thể làm giảm tương phản.
- Remote trái/phải/lên/xuống nhanh, giữ phím, đổi dock/grid và quay về từ app: đo frame time/CPU/GPU để xác nhận không regression so với v2 đã thử tốt. Source giữ motion chưa thay thế kiểm thử này.
- Cold start/recreate khi áp theme: thời gian chuẩn bị texture và bộ nhớ trên ARMv7/RAM 1,37 GiB. Không có số đo TCL trong task này.
- Lời chào theo thời gian, dấu tiếng Việt và tên lưu sau restart; logic giữ nguyên nhưng vẫn cần xem hiển thị thực tế.

Cập nhật hai APK Aqua do người dùng chủ động thực hiện; không cần thay Home mặc định hay gỡ CleanTV/SmartTube. Với config v2 đang bật Aqua Cinematic, sửa render mới có hiệu lực sau khi launcher nạp lại; muốn đúng palette mới, chọn/áp preset **Ăng Hòn Aqua Cinematic v2.1** từ Manager. Các override màu/scrim người dùng vẫn được giữ theo cơ chế cũ. Xuất config trước khi thử; nếu cần quay về v2, dùng bản APK/config đã giữ và giao diện launcher gốc, không xóa dữ liệu TV.
