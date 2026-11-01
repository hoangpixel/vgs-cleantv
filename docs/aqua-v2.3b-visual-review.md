# Aqua Galaxy v2.3b — tuned visual candidate

**READY FOR PREVIEW REVIEW.** Đây là bản tinh chỉnh để xem trước, chưa phải APK hoặc preset mới được phát hành. Không build lại Android, không sửa patch launcher/Manager, package, signing hoặc các APK v2.3 đã xác minh. Không ADB, cài TV, đổi Home, commit hoặc push.

## Preview

[cosmic-tuned-comparison.png](../.gradle/aqua-v23b-review/cosmic-tuned-comparison.png)

Đường dẫn tuyệt đối:

```text
C:\Users\mhoang\OneDrive\Desktop\vgs-cleantv\.gradle\aqua-v23b-review\cosmic-tuned-comparison.png
```

Cột trái là decor v2.3 hiện tại; cột phải là candidate v2.3b. Ba hàng Idle / artwork / focused app dùng cùng gradient, header và nội dung tham chiếu. Đã mở ảnh kiểm tra. Texture và phép compositing lấy từ renderer; layout/header/card là mô phỏng, không phải screenshot Android TV. Giờ/ngày trong ảnh là fixture, không thay logic thời gian launcher.

## Thay đổi thẩm mỹ

- **Hành tinh:** bán kính 10 → 18 pixel, đường kính 20 → 36 pixel trên texture 480×270, tức **1,8×**. Tăng tỷ lệ vùng bán nguyệt tương ứng để giữ hình dáng cũ. Viền sáng nhích nhẹ, halo mềm rộng 7 pixel thay vì 4, alpha thấp.
- **Vị trí:** vẫn ở bên phải, tâm x=413. Dịch tâm xuống y=56 từ y=49 để chừa khoảng phía trên khi tăng kích thước. Phần thân nằm khoảng x=395–431, y=38–74, ngoài vùng chữ trái.
- **Sao:** **12 điểm**, so với 5 trước đó. Hai điểm có lõi/cross-sparkle sáng hơn một chút; mười điểm còn lại nhỏ, alpha thấp và halo rất nhẹ.
- **Phân bố:** tâm sao x=278–466 (~58–97% chiều rộng), y=22–157 (~8–58% chiều cao). Tất cả ở nửa phải; tránh gom quanh hành tinh hoặc phủ kín màn hình. Không thêm sao vào nửa trái.
- Giữ Galaxy gradient, vị trí lời chào dưới giờ/ngày, Cinematic, focus và nội dung. Không thêm animation hay particle runtime.

## Kiểm tra đã chạy

Chỉ compile/chạy công cụ Java desktop để tạo preview. Không chạy Gradle, unit test Android hoặc build APK vì bản này còn chờ đánh giá thẩm mỹ.

Công cụ kiểm tra trên mảng pixel xác nhận:

- Galaxy nền giữ hash ARGB v2.2: `46e44756d7cf4c920c88f5558042f2be818dda077de58e66c9d9f545aa509b84`.
- Decor v2.3 gốc giữ hash: `fc37adc2cc58ceaac569ca4a3afd96e7b02bc1ce3bb7a8f3e9fba5a7dc428471`.
- Candidate tác động **1.826/129.600 pixel (~1,41%)** so với Galaxy chưa thêm decor; chỉ ở vùng hành tinh/sao bên phải. Ngoài vùng đó pixel nền giữ nguyên.
- Kết quả cố định khi vẽ lại, mirror RTL khớp; không có yếu tố thời gian/random.
- Candidate ARGB SHA-256: `309b96c289831226889d407b51f32cfe81ddf81b91acbdb80889b248437e5361`.

Con số 1,41% lớn hơn giới hạn test decor v2.3 cũ (<1%) do hành tinh lớn hơn. Nếu đưa candidate vào launcher, cần cập nhật budget test theo vùng mới, chạy lại hồi quy và xác minh build. Không coi 382 test PASS của APK cũ là test cho candidate này.

Chiến lược vẫn bake tĩnh vào bitmap 480×270 (~0,49 MiB), không cần View/bitmap mới hoặc công việc theo focus. Vùng tính hành tinh lớn hơn chỉ ảnh hưởng bước chuẩn bị texture một lần; chưa đo trên TCL, không khẳng định hiệu năng từ preview.

## File candidate và tái tạo

- `scripts/review/candidates/v23b/CosmicDecor.java`: candidate riêng, **không nằm trong patch hoặc APK hiện tại**.
- `scripts/review/candidates/v23b/README.md`: phạm vi và lưu ý khi đưa vào launcher sau review.
- `scripts/review/RenderAquaCosmicTuned.java`: render A/B và kiểm tra pixel.
- Tài liệu này.

Tái tạo từ root workspace, dùng source v2.3 đã build:

```powershell
$source = '.gradle/aqua-launcher/f1dbb0542b7d4a398c5152b869969130/launcher/app/src/main/java/com/gathin/tvhome'
$review = Join-Path (Get-Location) '.gradle/aqua-v23b-review'
New-Item -ItemType Directory -Force "$review/classes" | Out-Null
$original = [IO.File]::ReadAllText((Join-Path (Get-Location) "$source/CosmicDecor.java")).Replace('CosmicDecor','CosmicDecorOriginal')
[IO.File]::WriteAllText("$review/CosmicDecorOriginal.java", $original, [Text.UTF8Encoding]::new($false))
& "$env:JAVA_HOME/bin/javac.exe" -encoding UTF-8 --release 17 -d "$review/classes" "$source/AquaSurface.java" "$review/CosmicDecorOriginal.java" scripts/review/candidates/v23b/CosmicDecor.java scripts/review/RenderAquaCosmicTuned.java
& "$env:JAVA_HOME/bin/java.exe" '-Djava.awt.headless=true' -cp "$review/classes" com.gathin.tvhome.RenderAquaCosmicTuned "$review/cosmic-tuned-comparison.png" "$source/../../../../res/font/lexend.ttf"
```

## Trạng thái APK

**Chưa cần build APK ở bước review này.** Nếu chọn dùng diện mạo tuned trên TV thì phải đưa candidate vào patch, test và build hai APK mới; APK v2.3 hiện có chưa chứa thay đổi tuned.

Giữ nguyên:

```text
Launcher v2.3 a4cdac16e884f03d90bb3e64cb4397370d02bba431472dc858f9f69b8cf3a84c
Manager  v2.3 036f6eda1dc383755b13d95f8a705030be0c79f2aca305fc259c2fe36ad381eb
Patch         be3b33c297b55375677296023c2e7f59cce1d523a2756411a742faad736c8ed6
```

Ảnh preview v2.3 cũ cũng giữ nguyên. Các kiểm tra tiếp theo trên TCL, nếu candidate được dùng: kích thước hành tinh từ xa, độ rõ 12 sao, khả năng chồng với toolbar/tên app dài, banding và frame time. Preview không thay thế kiểm thử thiết bị.
