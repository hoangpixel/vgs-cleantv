# Aqua Galaxy v2.3d — planet position and star density preview

**READY FOR PREVIEW REVIEW.** Chỉ tạo candidate riêng, chưa đổi patch/preset/version/signing hoặc APK v2.3c. Chờ người dùng duyệt trước khi tích hợp và build.

[Mở preview A/B Idle / Artwork / Focus](../.gradle/aqua-v23d-review/planet-lowered-comparison.png)

```text
C:\Users\mhoang\OneDrive\Desktop\vgs-cleantv\.gradle\aqua-v23d-review\planet-lowered-comparison.png
PNG SHA-256 24ca2666a91a06cc4bd85ff3436b857c9d78cab2336165301ad86963c9e7b1b0
```

Cột trái lấy texture production v2.3c (12 sao, hành tinh y=64). Cột phải là candidate v2.3d (22 sao, hành tinh y=88). Cả hai dùng cùng header, hàng icon tham chiếu và nội dung mô phỏng; ba hàng là Idle, artwork tổng hợp và focused app. Không phải screenshot Android hoặc phép đo tọa độ icon trên TCL. Đã mở ảnh cuối để kiểm tra trực quan.

## Tinh chỉnh

- Hành tinh dịch **xuống 24 px**, tâm `(413,64) → (413,88)` trên texture 480×270. Đường kính giữ **48 px**; giữ nguyên palette champagne/tím khói, thuật toán ánh sáng và halo tĩnh 8 px.
- Thân nằm khoảng x=389–437, y=64–112, cách mép phải 42 px. Halo bắt đầu ở y=56.
- Hàng icon **mô phỏng** nằm trong dải y=16–48: thân hành tinh mới cách đáy dải này 16 px, kể cả halo còn khoảng 8 px. Trong preview, hành tinh mới không còn chạm hàng icon tham chiếu. Khoảng cách thực tế phụ thuộc layout/scale/overscan và số nút đang hiện trên TCL.
- Tổng **22 sao**: giữ 12 sao cũ, thêm **10 điểm nhỏ/nhẹ** ở vùng phải giữa/phải dưới. Chỉ **hai sao** cũ có lõi/cross-sparkle nhấn hơn; không thêm điểm nhấn sáng lớn.
- Tâm sao trải x=278–470 (~58–98% chiều rộng), y=22–251. Mười sao mới có y=153–251, không tăng mật độ quanh header hoặc hàng icon. Nửa trái vẫn giữ nguyên pixel nền; tránh sao mới trong vùng tiêu đề app của fixture Focus.

Nền Galaxy, lời chào dưới giờ/ngày/lịch âm, màu focus và bố cục tham chiếu giữ nguyên. Trong preview, nền đầy hơn ở phần phải dưới nhưng vẫn tối và thưa; đồng hồ/lời chào/tên app dễ đọc. Tên app dài, dock thật và vùng hiển thị thực tế chưa được kiểm chứng bằng ảnh TV.

## Kiểm tra đã chạy

Chỉ compile/chạy renderer Java desktop; **không Gradle, Android unit test hay build APK** trong lượt này.

- Hash ARGB Galaxy nền giữ nguyên: `46e44756d7cf4c920c88f5558042f2be818dda077de58e66c9d9f545aa509b84`.
- Texture v2.3c production giữ hash `61b836ba35389065601e687afa875b84e43d4556d34437179cb6fff418201885`.
- Candidate: **4.388 pixel** khác v2.3c, đều trong vùng decor cũ/mới ở nửa phải. Nền ngoài vùng decor giữ nguyên; 12 lõi sao cũ giữ nguyên pixel.
- Kiểm tra số sao 22, đường kính 48 px, dịch xuống 24 px, màu sáng/tối, khoảng cách mép và hàng icon tham chiếu đều PASS.
- Render lặp lại xác định, RTL mirror khớp; footprint so với Galaxy chưa decor dưới 4% (guard thô của công cụ preview, không phải kết quả đo hiệu năng).
- Candidate ARGB SHA-256: `e84b9e49addea92fc04d75b0392833b42618f18e52138c1f41a0bafaeb9f6481`.

Decor vẫn vẽ sẵn vào cùng mảng pixel 480×270; không thêm animation, particle, blur, View, bitmap theo focus hoặc tác vụ nền. Chỉ thêm 10 điểm sao lúc bake; vùng tính hành tinh giữ kích thước cũ và đổi vị trí. Chưa đo frame time/RAM trên TCL, không coi preview là bằng chứng hiệu năng thiết bị. 383 test của APK v2.3c không được tính thành test cho candidate này.

Đối chiếu snapshot trước/sau xác nhận các file có sẵn, patch chính thức, preset, package/version/signing config, APK/receipt Manager–Launcher và ảnh v2.3c không đổi. Không dùng ADB, cài TV, đổi Home, gỡ CleanTV/SmartTube, commit hoặc push.

## File mới và tái tạo

Chỉ thêm bốn file: candidate `scripts/review/candidates/v23d/CosmicDecor.java`, README đi kèm, `scripts/review/RenderAquaCosmicLowered.java`, tài liệu này. Output tạm ở `.gradle/aqua-v23d-review/`.

```powershell
$source = ((Get-Content app/src/aqua/assets/aqua-launcher.properties | Where-Object { $_ -like 'buildSourcePath=*' }).Substring(16))
$review = Join-Path (Get-Location) '.gradle/aqua-v23d-review'
New-Item -ItemType Directory -Force "$review/classes" | Out-Null
$c = [IO.File]::ReadAllText("$source/app/src/main/java/com/gathin/tvhome/CosmicDecor.java").Replace('CosmicDecor','CosmicDecorC')
[IO.File]::WriteAllText("$review/CosmicDecorC.java",$c,[Text.UTF8Encoding]::new($false))
& "$env:JAVA_HOME/bin/javac.exe" -encoding UTF-8 --release 17 -d "$review/classes" "$source/app/src/main/java/com/gathin/tvhome/AquaSurface.java" "$review/CosmicDecorC.java" scripts/review/candidates/v23d/CosmicDecor.java scripts/review/RenderAquaCosmicLowered.java
if ($LASTEXITCODE -ne 0) { throw 'Preview compilation failed' }
& "$env:JAVA_HOME/bin/java.exe" '-Djava.awt.headless=true' -cp "$review/classes" com.gathin.tvhome.RenderAquaCosmicLowered "$review/planet-lowered-comparison.png" "$source/app/src/main/res/font/lexend.ttf"
```

**Chưa có APK v2.3d.** Nếu thiết kế được duyệt, bước sau mới quyết định preset riêng để tiếp tục giữ v2.3c, tích hợp patch, tăng versionCode và chạy hồi quy/build/xác minh hai APK.
