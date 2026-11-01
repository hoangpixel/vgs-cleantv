# Aqua Galaxy v2.3e Cosmic Plus — final verification

**READY FOR TESTING — 01/11/2026.** Launcher và Manager build thành công; **391 test PASS**, không failure/error/skipped. Package/chứng chỉ ký khớp APK v2.3c được lưu trước build, versionCode tăng 15→16 và 114→115. Manager nhúng đúng byte Launcher mới. Chưa cài hoặc chạy v2.3e trên TCL.

## Thiết kế và khả năng quay lại preset cũ

Tích hợp nguyên candidate đã duyệt thành `CosmicPlusDecor` trong patch chính thức, chỉ đổi tên class và comment khi đưa vào production:

- Hành tinh **58 px**, tâm **(413,104)** trên texture 480×270.
- Champagne/tím khói, mặt sáng/tối, rim và halo 8 px tĩnh giữ nguyên thiết kế. Không thêm vành, animation hoặc tăng cường độ glow.
- **28 sao**, giữ 22 sao v2.3d và thêm đúng sáu điểm nhỏ; chỉ hai điểm nhấn sáng.
- Khoảng trống với đáy toolbar tham chiếu y=48: **27 px tới thân / 19 px tới halo**, không giảm so với preview.
- Giữ nguyên Galaxy và vùng chữ. Texture production có ARGB SHA-256 `a9bbfd2737c332b2e83f79e52a4c7852df08c7f13b754f0d6703b88cbcbe6d79`.

Preset mới **Ăng Hòn Aqua Galaxy — Cosmic Plus v2.3e**, ID `ang-hon-aqua-cosmic-plus`, bật `aqua_cosmic_plus=true` cùng các cờ cinematic/galaxy/cosmic hiện có. Giữ preset **Cinematic v2.3c**, ID `ang-hon-aqua-cosmic`, renderer `CosmicDecor` và defaults cũ nguyên vẹn. v2.1/v2.2 cũng giữ nguyên.

`Config` chỉ chọn Plus khi cosmic đã bật và có cờ mới; `AquaCinematic`/`AquaSurface` chuyển đúng renderer lúc chuẩn bị texture. Thiếu cờ Plus vẫn dùng v2.3c. Chọn preset cũ sẽ bỏ cờ Plus; test round-trip giữ tên chào, dock và app keys. Defaults cho cài mới dùng Plus, nhưng cấu hình đã lưu trên TV **không tự bị đổi khi cập nhật APK**.

Khi người dùng tự thử: xuất cấu hình đang dùng, cập nhật hai APK đúng package/chữ ký, rồi trong mục **Đổi giao diện** chọn **Cosmic Plus v2.3e** và áp dụng cấu hình. Muốn quay lại diện mạo trước, chọn **Cinematic v2.3c**, v2.2 hoặc v2.1; không cần đổi Home/gỡ ứng dụng. Đây là rollback preset trên APK mới, không phải hạ versionCode. Chưa kiểm thử thao tác đó trên thiết bị.

## Preview cuối khớp source đã build

- [Preview chính LTR — v2.3d trái / v2.3e phải](../.gradle/aqua-v23e-final/cosmic-plus-comparison.png).
- [Preview RTL](../.gradle/aqua-v23e-final/cosmic-plus-comparison-rtl.png).

```text
C:\Users\mhoang\OneDrive\Desktop\vgs-cleantv\.gradle\aqua-v23e-final\cosmic-plus-comparison.png
C:\Users\mhoang\OneDrive\Desktop\vgs-cleantv\.gradle\aqua-v23e-final\cosmic-plus-comparison-rtl.png
```

Ba hàng Idle / Artwork / Focus, có toolbar tham chiếu. Hai PNG đi qua **factory production `withCosmicPlusDecor()`** từ source launcher đã build, khớp từng byte với hai preview được duyệt. Host tool được sao chép vào thư mục output và trỏ sang `CosmicPlusDecor`, không sửa renderer preview/candidate đã duyệt.

```text
PNG LTR SHA-256 a186810f73462cee994011dbb26779e4edb78a38e7f885b613c5959820211a42
PNG RTL SHA-256 8ac6f09e8094a9becf12966e9cf36e72b4f7d955699da46e8d7652bd9bb23d7d
```

Đã kiểm tra ở lượt preview: màu champagne tách khỏi aqua, 22 vùng sao cũ không đổi, sao mới và halo tránh bounding box UI mô phỏng, render xác định/mirror RTL khớp. Source production tái tạo đúng các kiểm tra đó. Header/toolbar/card là fixture, artwork tổng hợp; **không phải screenshot TV hoặc phép đo vị trí UI Android**. Kiểm tra RTL ở đây là pixel/hình học, chưa phải kiểm thử locale thực tế.

## Build và kết quả test

Gradle 8.9, JDK runtime 21.0.12, bytecode 17. Android SDK từ `local.properties`: `C:\Users\mhoang\AppData\Local\Android\Sdk`. Launcher AGP 8.5.2 / SDK và Build-Tools 34; Manager AGP 8.7.2 / SDK và Build-Tools 35. Không thêm dependency và không dùng `--offline`.

Hai patch được kiểm tra/áp vào archive cô lập của commit `fe9ad3204b945d3aadfb6876ee6ea78c20a1fe73`; không reset/chỉnh cây `upstream/` đang có thay đổi từ trước. Source cuối:

```text
C:\Users\mhoang\OneDrive\Desktop\vgs-cleantv\.gradle\aqua-launcher\1e1fc6b055af48a99c39e354cf6ec15a\launcher
```

Lệnh đã chạy từ root, với Gradle 8.9 có sẵn:

```powershell
$gradleBuild = "$env:USERPROFILE/.gradle/wrapper/dists/gradle-8.9-bin/90cnw93cvbtalezasaz0blq0a/gradle-8.9/bin/gradle.bat"
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/dung-aqua-lite.ps1 -PrepareOnly
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/dung-aqua-lite.ps1 -GradleExecutable $gradleBuild
& $gradleBuild --no-daemon --console=plain -g .gradle/aqua-cache -PaquaLite=true testDebugUnitTest :app:assembleDebug
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/review/kiem-tra-patch.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/kiem-tra-aqua-lite.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/xac-minh-aqua-lite.ps1 -PreviousLauncherApk .gradle/aqua-v23e-final/launcher-v23c.apk -PreviousManagerApk .gradle/aqua-v23e-final/manager-v23c.apk
git diff --check
```

Build dùng cùng thiết lập tiến trình đã xác minh ở v2.3c: Android user-home `.gradle/aqua-android-home`, bản sao cùng debug key `retained-aqua-signing.jks` qua `ORG_GRADLE_PROJECT_android.injected.signing.*`. Key hash đối chiếu file gốc trước build; không tạo key mới hoặc đổi signing trong source. Build/test được công cụ cho chạy ngoài sandbox để tránh lỗi ACL JAR/cache/keystore đã xác định ở lượt trước; cache/output vẫn trong workspace. Không chạy ADB.

**Launcher BUILD SUCCESSFUL in 45s; Manager BUILD SUCCESSFUL in 1m 17s.** Không phát hiện lỗi build/source cần sửa ngoài phạm vi triển khai này. Warning whitespace của giấy phép trong VGS patch và deprecated API/Kotlin có sẵn không chặn build; giữ nguyên các phần đó.

Thống kê trực tiếp XML, đối chiếu thuộc tính `tests` với số testcase, không cộng lặp các lần build:

| Module | Suite XML | PASS | FAIL | ERROR | SKIP |
| --- | --- | --- | --- | --- | --- |
| Launcher | 49 | 338 | 0 | 0 | 0 |
| Manager | 12 | 46 | 0 | 0 | 0 |
| khoa | 1 | 7 | 0 | 0 | 0 |
| Tổng | 62 | **391** | **0** | **0** | **0** |

Launcher/Manager chạy test trong lượt build; khoa UP-TO-DATE, giữ bảy kết quả hợp lệ của module không đổi. So với 383 test v2.3c, thêm **5 CosmicPlusDecorTest** và **3 AquaCosmicPlusTest**. Test mới bao phủ golden texture đã duyệt, pixel vùng decor/header, determinism/RTL, factory rollback về hash v2.3c, không bật Plus trên v2.1, defaults/preset mới, round-trip và legacy presets.

AquaLiteTest 7, AquaGalaxyTest 4, AquaCosmicTest 3, AquaCosmicPlusTest 3, KichBanKhoiPhucTest 4 đều PASS. Các test v2.3c cũ vẫn chạy, gồm hash texture `61b836…01885`; Galaxy v2.2 giữ hash `46e447…9b84`.

Nguồn XML: launcher source cuối ở trên + `app/build/test-results/testDebugUnitTest/`; Manager tại `app/build/test-results/testDebugUnitTest/`; khoa tại `khoa/build/test-results/testDebugUnitTest/`. Tổng hợp: `.gradle/aqua-v23e-final/unit-test-summary.json`. Log: `launcher-build.log`, `manager-build.log`, `source-regression.log`, `apk-verification.log` trong cùng thư mục output.

## Hồi quy source/layout/hiệu năng

- Helper xác nhận **255 file source/resource** khớp hai patch và cây launcher cuối; **9 file engine/layout, 19 phương thức motion/navigation** giữ nguyên baseline Cinematic sau VGS.
- So cây `app/src` v2.3c: chỉ thay AquaCinematic/AquaSurface/Config để chọn renderer mới và defaults cho cài mới; thêm CosmicPlusDecor/CosmicPlusDecorTest. MainActivity, layout XML, AquaHeader, AquaGreeting, CosmicDecor và test v2.3c giữ nguyên byte.
- Spring/duration/interpolator, D-pad, RecyclerView, dock navigation, logic đồng hồ/lịch âm/lời chào không đổi. Kiểm tra này là structural/JVM, không phải instrumentation hay benchmark TV.
- Vẫn một scrim bitmap **480×270 ARGB_8888 (~0,49 MiB)** cache theo Activity, trên artwork/ambient và dưới nội dung. Không tạo thêm View/bitmap theo focus, animation nền, particle, blur, render loop hoặc dịch vụ.
- Decor lớn hơn chỉ làm tăng việc bake ban đầu trong vùng nhỏ. Footprint decor 4.117/129.600 pixel (~3,18%) là số pixel khác nền, không phải tỷ lệ CPU/GPU. Chưa đo frame time/cold start/PSS trên TCL.
- Snapshot giữ nguyên VGS patch, defaults v2.1/v2.2/v2.3c, candidate Java/renderer/ảnh đã duyệt, code ADB/debloat/backup/restore và APK launcher gốc. Trong PhienLamViec chỉ đổi preset mặc định, không đổi luồng triển khai/điều khiển thiết bị.

## APK và xác minh đóng gói

| APK | Package | Version/code | minSdk/target | Byte |
| --- | --- | --- | --- | --- |
| Launcher | `vn.anghon.aqua.launcher` | **2.3e / 16** | 26 / 34 | 2.281.706 |
| Manager | `vn.anghon.aqua.manager` | **2.3e / 115** | 26 / 35 | 28.863.714 |

```text
Launcher:
C:\Users\mhoang\OneDrive\Desktop\vgs-cleantv\app\src\aqua\assets\tvhome.apk
SHA-256 4a5327a6968fe4a31f7ee5fba58727871812e5ad313d72541e2336c59703f867

Manager:
C:\Users\mhoang\OneDrive\Desktop\vgs-cleantv\app\build\outputs\apk\debug\app-debug.apk
SHA-256 f2a80fc608509d6cced3b1f07afc45eecefb9253c028c380971d9bc3b2420c4c

Signer certificate SHA-256 (cả hai, cùng APK v2.3c):
5b6ffaad8776e93270563667d2b0c28349de8e528d1fdb5576977c158388f486
```

Chữ ký xác minh hợp lệ với API 28. Launcher không chứa native libs; Manager có armeabi-v7a và arm64-v8a. Hai APK v2.3c trước build được lưu trong `.gradle/aqua-v23e-final/launcher-v23c.apk` / `manager-v23c.apk`; package/cert khớp, versionCode tăng đúng. Không đọc metadata thiết bị qua ADB, nên xác minh tương thích cập nhật dựa trên các APK đã lưu, chưa phải phép thử update thực tế.

Verifier hash byte `assets/tvhome.apk` trong Manager ra đúng SHA-256 đầy đủ của Launcher mới. Defaults/receipt/authority/DEX/giấy phép hợp lệ; Manager DEX có preset Plus lẫn các preset cũ. Không nhúng launcher gốc hoặc tài sản VGS/macOS. Kết quả máy đọc: `.gradle/aqua-v23e-final/apk-verification.json` và `.gradle/aqua-build-verification.json`.

## Danh sách file thay đổi trong lượt finalize

**14 file source/config/test/script/tài liệu**, ngoài APK/receipt và build outputs:

1. `patches/launcher-aqua-lite.patch`: Plus renderer/factory/config flag/test và version Launcher 16.
2. `themes/ang-hon-aqua-cosmic-plus/config.json`: defaults preset mới; defaults cũ giữ nguyên.
3. `app/build.gradle.kts`: version Manager 115, guard defaults/DEX mới.
4. `app/src/main/java/zone/vgs/tvsach/dulieu/Theme.kt`: thêm preset Plus, bỏ cờ Plus khi chọn preset cũ.
5. `app/src/main/java/zone/vgs/tvsach/ui/PhienLamViec.kt`: mặc định Plus cho phiên cấu hình mới; vẫn đọc ID preset đã lưu.
6. `app/src/test/java/zone/vgs/tvsach/dulieu/AquaCosmicPlusTest.kt`: ba test mới.
7. `scripts/dung-aqua-lite.ps1`: build asset/receipt từ defaults Plus.
8. `scripts/kiem-tra-aqua-lite.ps1`: guard defaults Plus.
9. `scripts/xac-minh-aqua-lite.ps1`: metadata mới, so cập nhật từ v2.3c, kiểm DEX preset Manager/Launcher.
10. `scripts/review/kiem-tra-patch.ps1`: guard chọn Plus/legacy renderer.
11. `scripts/review/candidates/v23e/README.md`: trạng thái snapshot đã được duyệt/tích hợp.
12. `docs/aqua-v2.3e-visual-review.md`: liên kết báo cáo finalize, giữ kết quả preview cũ.
13. `docs/aqua-lite-build-verification.md`: kết quả/APK mới nhất.
14. `docs/aqua-galaxy-v2.3e.md`: báo cáo này.

File generated assets cập nhật: `app/src/aqua/assets/tvhome.apk`, `app/src/aqua/assets/aqua-launcher.properties`. Manager output: `app/build/outputs/apk/debug/app-debug.apk`. Preview/cache/receipt source copy/test outputs trong `.gradle/` hoặc build/ không tính là source. Giữ nguyên các dirty changes khác của workspace, không sửa `.vscode/settings.json`.

## Còn phải kiểm thử trên TCL

- Sau khi người dùng tự cập nhật và chọn Plus: hành tinh đúng 58 px/(413,104), 28 sao, khoảng thoáng toolbar thực tế, độ rõ từ xa và banding/upscale.
- Artwork sáng, tên app dài, weather/font scale/overscan và các thành phần hệ thống thật.
- D-pad nhanh/giữ phím, focus/dock/grid; frame time, cold start/recreate và RAM so với v2.3c đã chạy ổn.
- Giữ tên/dock/cấu hình sau update; chọn lại v2.3c/v2.2/v2.1 và export/import để kiểm tra rollback trên thiết bị.

Không suy ra độ mượt TV từ BUILD SUCCESSFUL hoặc test JVM. Không cài APK, thao tác ADB, đổi Home, gỡ CleanTV/SmartTube, commit/push trong đợt này.
