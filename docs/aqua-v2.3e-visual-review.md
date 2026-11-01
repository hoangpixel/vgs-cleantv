# Aqua Galaxy v2.3e — Cosmic Plus preview

**Thiết kế đã duyệt và tích hợp.** Tài liệu này giữ kết quả lượt preview ban đầu; kết quả build APK 2.3e / code 16 và 115 nằm trong [báo cáo finalize v2.3e](aqua-galaxy-v2.3e.md). Candidate Java và ảnh đã duyệt giữ nguyên. v2.3d vẫn preview-only; bản trên TCL trước khi người dùng tự cập nhật vẫn là v2.3c / code 15.

## Preview

- [So sánh LTR: v2.3d bên trái / v2.3e bên phải](../.gradle/aqua-v23e-review/cosmic-plus-comparison.png).
- [So sánh hình học RTL](../.gradle/aqua-v23e-review/cosmic-plus-comparison-rtl.png).

```text
C:\Users\mhoang\OneDrive\Desktop\vgs-cleantv\.gradle\aqua-v23e-review\cosmic-plus-comparison.png
C:\Users\mhoang\OneDrive\Desktop\vgs-cleantv\.gradle\aqua-v23e-review\cosmic-plus-comparison-rtl.png
```

Cả hai ảnh có ba hàng **Idle / Artwork / Focus** và hàng icon cài đặt/Wi-Fi/profile tham chiếu. Pixel texture được tạo bằng renderer; header/card/toolbar là mô phỏng, artwork là underlay tổng hợp. Giờ/ngày trong ảnh là fixture. **Không phải ảnh chụp TV hoặc đo layout thực tế.** Ảnh RTL chỉ kiểm tra hình học mirror/right-align, không phải kiểm thử ngôn ngữ Hebrew/Arabic hoặc Android resource inflation. Đã mở xem cả hai ảnh.

PNG SHA-256:

```text
LTR a186810f73462cee994011dbb26779e4edb78a38e7f885b613c5959820211a42
RTL 8ac6f09e8094a9becf12966e9cf36e72b4f7d955699da46e8d7652bd9bb23d7d
```

## Hành tinh và khoảng cách

- Đường kính **48 → 58 px**, tăng khoảng 20,8%.
- Tâm chính xác **(413,88) → (413,104)**, dịch xuống 16 px so với v2.3d. Không cần điều chỉnh thêm ngoài tọa độ yêu cầu.
- Giữ champagne `#E8C8A0`, tím khói `#77627F`, viền `#F3D9B7`, bóng `#392C46`, ánh sáng xiên trên-phải và chuyển mềm sáng/tối. Không thêm vành hoặc nguồn sáng mới.
- Thân nằm khoảng x=384–442, y=75–133. Halo 8 px, alpha tối đa 14/255 giữ nguyên; bounding box gồm halo x=376–450, y=67–141.
- Toolbar mô phỏng kết thúc tại **y=48**: khoảng trống tới thân **27 px**, tới halo **19 px**. Mép phải còn 37 px tới thân / 29 px tới halo. RTL mirror có cùng khoảng trống tương ứng ở bên trái.

Hành tinh không chạm bounding box toolbar, đồng hồ/ngày/lời chào, tiêu đề hoặc card trong fixture LTR/RTL. Màu champagne/tím khói vẫn tách khỏi Galaxy aqua. Không tăng cường độ glow; diện tích halo tăng theo bán kính nhưng độ sáng không tăng. Tên app dài, số icon thực tế, font scale và overscan trên TCL vẫn cần kiểm tra ở giai đoạn thiết bị nếu thiết kế được duyệt.

## Sao và readability

**28 sao**, giữ chính xác 22 định nghĩa/tọa độ/alpha từ v2.3d và thêm sáu điểm nhỏ:

```text
(305,104), (340,124), (464,119), (370,183), (309,216), (445,246)
```

Sáu điểm có alpha lõi 42–54/255, nhỏ và mềm; vị trí không theo lưới. Toàn bộ sao nằm ở nửa phải LTR, chủ yếu vùng giữa/dưới; RTL mirror sang nửa trái. Vẫn chỉ **hai sao nhấn sáng** cũ (alpha lõi 140 và 130), dưới giới hạn ba. Không thêm sparkle lớn hay nhấp nháy.

Các sao mới tránh bounding box chữ, toolbar, tiêu đề/card của cả hai fixture và tránh halo hành tinh. 22 sao cũ giữ nguyên cả vùng pixel 5×5 quanh từng sao. Decor luôn là lớp nền dưới nội dung/toolbar; một số điểm nền cũ có thể bị toolbar mô phỏng phủ như trước, không di chuyển hoặc vẽ lại chúng lên trên UI. Không thêm pixel decor vào nửa trái LTR gần header. Trong ba preview, chữ trắng và hierarchy giữ nguyên; đây không phải chứng nhận mọi tên app/layout/cấu hình trên TV.

## Kiểm tra thực tế

Chỉ compile/chạy Java desktop với source/bytecode target 17; **không Gradle, unit test Android, APK build, ADB hoặc thiết bị**.

Công cụ `RenderAquaCosmicPlus` xác nhận:

- Galaxy không decor giữ hash ARGB `46e44756d7cf4c920c88f5558042f2be818dda077de58e66c9d9f545aa509b84`.
- Candidate v2.3d giữ hash `e84b9e49addea92fc04d75b0392833b42618f18e52138c1f41a0bafaeb9f6481`.
- Candidate v2.3e ARGB SHA-256: `a9bbfd2737c332b2e83f79e52a4c7852df08c7f13b754f0d6703b88cbcbe6d79`.
- **4.552 pixel** khác v2.3d, đều trong vùng decor cũ/mới ở nửa phải; pixel nền ngoài vùng đó giữ nguyên.
- Decor khác Galaxy chưa trang trí **4.117/129.600 pixel (~3,18%)**. Đây là footprint pixel, không phải phép đo CPU/GPU hoặc frame time.
- Cả 22 tuple sao và vùng pixel quanh sao cũ giữ nguyên. Kiểm tra 28 sao, hai điểm nhấn, 58 px, tâm (413,104), màu sáng/tối, khoảng cách toolbar và kiểm tra bounding box LTR/RTL đều PASS.
- Render texture lặp lại xác định, từng pixel RTL khớp mirror; cả hai PNG cũng lặp lại khớp từng byte. Nhãn cột/trạng thái được kiểm tra có pixel hiển thị trong output.

Decor tiếp tục bake vào cùng texture/mảng ARGB 480×270 (~0,49 MiB), không thêm View/bitmap runtime, render loop, particle, blur hoặc animation. Hình cầu lớn hơn làm tăng phần tính một lần lúc bake; thêm sáu sao cũng chỉ ở bước chuẩn bị. Không có allocation theo focus. Chưa đo chi phí chuẩn bị/FPS/RAM trên TCL và không khẳng định hiệu năng thiết bị từ preview.

Đối chiếu hash snapshot trước/sau xác nhận các file có sẵn, patch, preset, version/package/signing, hai APK/receipt v2.3c và preview v2.3d/v2.3c giữ nguyên. `git diff --check` PASS. Không đổi CleanTV/SmartTube, Home, commit hoặc push.

## File mới và tái tạo

Chỉ thêm bốn file: `scripts/review/candidates/v23e/CosmicDecor.java`, README đi kèm, `scripts/review/RenderAquaCosmicPlus.java`, tài liệu này. Output tạm nằm trong `.gradle/aqua-v23e-review/`.

```powershell
$source = ((Get-Content app/src/aqua/assets/aqua-launcher.properties | Where-Object { $_ -like 'buildSourcePath=*' }).Substring(16))
$review = Join-Path (Get-Location) '.gradle/aqua-v23e-review'
New-Item -ItemType Directory -Force "$review/classes" | Out-Null
$d = [IO.File]::ReadAllText((Join-Path (Get-Location) 'scripts/review/candidates/v23d/CosmicDecor.java')).Replace('CosmicDecor','CosmicDecorD')
[IO.File]::WriteAllText("$review/CosmicDecorD.java",$d,[Text.UTF8Encoding]::new($false))
& "$env:JAVA_HOME/bin/javac.exe" -encoding UTF-8 --release 17 -d "$review/classes" "$source/app/src/main/java/com/gathin/tvhome/AquaSurface.java" "$review/CosmicDecorD.java" scripts/review/candidates/v23e/CosmicDecor.java scripts/review/RenderAquaCosmicPlus.java
if ($LASTEXITCODE -ne 0) { throw 'Preview compilation failed' }
& "$env:JAVA_HOME/bin/java.exe" '-Djava.awt.headless=true' -cp "$review/classes" com.gathin.tvhome.RenderAquaCosmicPlus "$review/cosmic-plus-comparison.png" "$source/app/src/main/res/font/lexend.ttf"
```

Lượt preview này đã dừng trước build và sau đó được người dùng duyệt. Bước finalize được ghi riêng trong báo cáo liên kết ở đầu tài liệu; không triển khai lên TV. Không có APK v2.3d.
