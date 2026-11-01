# Aqua Galaxy v2.3c — Champagne Moon preview

**Đã duyệt và tích hợp chính thức.** Nội dung bên dưới ghi lại lượt preview ban đầu. Kết quả build/test/APK sau tích hợp ở [báo cáo v2.3c](aqua-galaxy-v2.3c.md). Candidate Java và ảnh đã duyệt giữ nguyên; v2.3b vẫn được giữ để so sánh.

[Mở ảnh A/B ba trạng thái](../.gradle/aqua-v23c-review/champagne-moon-comparison.png)

```text
C:\Users\mhoang\OneDrive\Desktop\vgs-cleantv\.gradle\aqua-v23c-review\champagne-moon-comparison.png
```

Ảnh gồm hai cột: trái v2.3b (aqua 36 px), phải v2.3c (champagne 48 px); ba hàng Idle, artwork tham chiếu, focused app. Texture dùng pixel renderer thực; header/card là mô phỏng, artwork là underlay tổng hợp, không phải screenshot Android TV. Ngày/giờ chỉ là fixture. Đã mở PNG kiểm tra trực quan.

## Thay đổi duy nhất về diện mạo

- Đường kính hành tinh **36 → 48 px**, tăng 33,3% trên texture 480×270. Tâm x=413 giữ nguyên, y=56 → 64 để chừa mép khi tăng kích thước.
- Mặt sáng `#E8C8A0`, mặt tối `#77627F`, viền `#F3D9B7`, bóng `#392C46`. Dùng pháp tuyến hình cầu và ánh sáng xiên trên-phải; chuyển mềm sang nửa tối tím khói. Halo 8 px, alpha tối đa 14/255, viền chỉ sáng nhẹ ở phía nhận ánh sáng.
- Thân nằm khoảng x=389–437, y=40–88; cách mép trên 40 px, mép phải 42 px. Kể cả halo vẫn còn khoảng 32 px phía trên và 34 px phía phải. Không thêm hành tinh thứ hai.
- Giữ nguyên chính xác 12 sao, Galaxy gradient, header/lời chào dưới giờ/ngày, màu focus và bố cục tham chiếu. Không sửa animation, focus, remote hoặc source launcher đã build.

Mặt sáng ấm và mặt tối tím giúp hành tinh tách khỏi nền aqua; độ sáng giới hạn ở vùng nhỏ bên phải. Trong cả ba preview, hành tinh không chồng vùng chữ/card. Chữ trắng và phân cấp header giữ nguyên. Không thấy banding mới rõ rệt ở kích thước preview; overscan, màu sắc thực, tên app dài và khả năng đọc từ xa vẫn cần kiểm tra trên TCL sau khi thiết kế được duyệt.

## Kiểm tra đã chạy

Compile/chạy công cụ Java desktop, không Gradle hoặc build Android:

- Hash Galaxy nền không đổi: `46e44756d7cf4c920c88f5558042f2be818dda077de58e66c9d9f545aa509b84`.
- Hash pixel candidate v2.3b khớp: `309b96c289831226889d407b51f32cfe81ddf81b91acbdb80889b248437e5361`.
- **2.950/129.600 pixel** khác v2.3b, tất cả trong vùng hành tinh bên phải. Pixel bên ngoài giữ nguyên, gồm nền và 12 sao.
- Kết quả xác định khi render lại; mirror RTL khớp. Kiểm tra bán kính 24 px, số sao 12, khoảng cách mép và sắc độ sáng/tối đều PASS.
- Pixel mẫu mặt sáng/tối: `#D6B898` / `#594964`.
- Candidate ARGB SHA-256: `61b836ba35389065601e687afa875b84e43d4556d34437179cb6fff418201885`.

Hành tinh tiếp tục bake tĩnh vào cùng texture 480×270. Không thêm animation, particle, blur, View, công việc theo focus hoặc tác vụ nền. Bước chuẩn bị có thêm tính pháp tuyến cho vùng hành tinh, chưa đo chi phí trên TCL; preview không chứng minh frame time trên thiết bị. Các test Android của APK cũ không được tính là test cho candidate này.

Đã đối chiếu SHA-256 với snapshot trước lượt chỉnh này: code/README/preview/renderer v2.3b, patch chính thức, Launcher APK và Manager APK đều giữ nguyên.

## File và tái tạo

Chỉ thêm candidate `scripts/review/candidates/v23c/CosmicDecor.java`, README đi kèm, công cụ `scripts/review/RenderAquaChampagne.java` và tài liệu này. Output nằm trong `.gradle/aqua-v23c-review`, không tham gia build APK hiện tại.

```powershell
$source = '.gradle/aqua-launcher/f1dbb0542b7d4a398c5152b869969130/launcher/app/src/main/java/com/gathin/tvhome'
$review = Join-Path (Get-Location) '.gradle/aqua-v23c-review'
New-Item -ItemType Directory -Force "$review/classes" | Out-Null
$b = [IO.File]::ReadAllText((Join-Path (Get-Location) 'scripts/review/candidates/v23b/CosmicDecor.java')).Replace('CosmicDecor','CosmicDecorB')
[IO.File]::WriteAllText("$review/CosmicDecorB.java", $b, [Text.UTF8Encoding]::new($false))
& "$env:JAVA_HOME/bin/javac.exe" -encoding UTF-8 --release 17 -d "$review/classes" "$source/AquaSurface.java" "$review/CosmicDecorB.java" scripts/review/candidates/v23c/CosmicDecor.java scripts/review/RenderAquaChampagne.java
if ($LASTEXITCODE -ne 0) { throw 'Preview compilation failed' }
& "$env:JAVA_HOME/bin/java.exe" '-Djava.awt.headless=true' -cp "$review/classes" com.gathin.tvhome.RenderAquaChampagne "$review/champagne-moon-comparison.png" "$source/../../../../res/font/lexend.ttf"
```

Lượt preview ban đầu dừng trước patch/test/build APK. Sau khi người dùng duyệt, đã tích hợp và xác minh theo báo cáo v2.3c được liên kết ở đầu tài liệu. Không dùng ADB, cài TV, đổi Home, commit hoặc push.
