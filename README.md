# CongViecTeam

Ứng dụng Android riêng cho hệ thống giao việc nội bộ, package `vn.lekhaccong.congviecteam`, phiên bản 1.0.0. Cài song song với CongViecPro. Android 8 trở lên.

## Kết nối máy chủ đang dùng

1. Trên Windows, chạy `lan-server/KHOI-DONG.bat` như trước và giữ máy tính bật.
2. Mở CongViecTeam trên điện thoại, nhập địa chỉ được in ở dòng điện thoại cùng Wi-Fi, ví dụ `http://192.168.100.111:8080`.
3. Đăng nhập bằng tài khoản quản trị hoặc nhân viên đã tạo. Dữ liệu nằm trên máy chủ hiện tại; không phải nhập lại.
4. Bấm **Chi tiết & báo cáo** để gửi tiến độ hoặc gửi hoàn thành để quản trị duyệt.

Lần đầu tạo quản trị phải mở `http://localhost:8080` trên chính máy tính chạy máy chủ. Không nhập localhost vào điện thoại. Nếu IP đổi, bấm **Máy chủ** trong app. Cho phép Node qua tường lửa trên mạng riêng; không chuyển tiếp cổng ra Internet.

## Tính năng

- Nhớ địa chỉ máy chủ; giữ phiên đăng nhập theo thời hạn máy chủ.
- Phân quyền quản trị/nhân viên, giao việc, cập nhật số lượng, lý do vướng mắc, duyệt hoàn thành.
- Chọn ảnh có sẵn và tệp Excel bằng bộ chọn tệp Android.
- Sao lưu và tải mẫu Excel: Android hỏi nơi lưu và báo khi ghi thành công. Có xử lý cookie đăng nhập khi tải.
- Cập nhật thông báo trong giao diện khi đang mở app. Chưa có thông báo đẩy khi đóng app, chưa hỗ trợ gửi báo cáo ngoại tuyến hay chụp ảnh trực tiếp trong app.
- Nút tải lại có xác nhận để tránh mất nội dung báo cáo chưa gửi.

Đây là bản thử nghiệm LAN. HTTP chỉ cho địa chỉ IPv4 riêng; máy chủ từ xa phải dùng HTTPS. Chứng chỉ HTTPS lỗi không được bỏ qua. Không có JavaScript bridge truy cập tệp hay quyền quản trị thiết bị. Không cần quyền truy cập toàn bộ bộ nhớ.

## Build APK bằng GitHub Actions

Đưa **toàn bộ nội dung thư mục này** vào gốc repo `lekhaccong/CongViecTeam`, gồm cả `.github/workflows/build-apk.yml` và `android/gradle/wrapper/gradle-wrapper.jar`.

Push lên nhánh `main` sẽ chạy workflow **Build CongViecTeam APK**. Vào **Actions → lần chạy xanh → Artifacts → CongViecTeam-APK**, tải ZIP, giải nén và cài `CongViecTeam-1.0.0.apk`.

Workflow dùng Java 17, Gradle 8.11.1, AGP 8.7.2, Android SDK 35. Gọi wrapper bằng `bash gradlew` để không phụ thuộc quyền executable sau khi tải lên. Job riêng kiểm thử máy chủ Node 24. Không cần Node để build phần Android.

APK debug dành cho thử nghiệm, không phải bản phát hành lên Google Play. Các lần build trên runner khác nhau có thể dùng khóa debug khác nhau: khi đó cần gỡ APK trước khi cài bản mới, rồi nhập lại địa chỉ máy chủ. Dữ liệu máy chủ không mất. Trước phát hành dài hạn cần cấu hình khóa ký ổn định trong GitHub Secrets, không commit khóa vào repo.

## Máy chủ đi kèm

`lan-server/` giữ API và định dạng dữ liệu tương thích bản CongViecPro LAN đang thử. Không chép đè thư mục dữ liệu đang dùng. Nếu đã có máy chủ chạy ổn thì tiếp tục dùng máy chủ đó, chỉ cài APK mới.

Nếu dựng máy chủ mới: cài Node 24, chạy `KHOI-DONG.bat` (tự cài thư viện lần đầu). Xem `lan-server/HUONG-DAN.md`. Mã nguồn không chứa tài khoản, dữ liệu cá nhân hoặc bản sao lưu.

## Kiểm thử

- `cd lan-server && npm ci --ignore-scripts && npm test`
- `javac -d /tmp/team-tests android/app/src/main/java/vn/lekhaccong/congviecteam/ServerAddress.java tests/ServerAddressTest.java`
- `java -cp /tmp/team-tests ServerAddressTest`
- `cd android && bash gradlew assembleDebug lintDebug`

Trên điện thoại cần thử: kết nối, đăng nhập hai vai trò, báo cáo kèm ảnh, duyệt, nhập Excel, chọn nơi lưu backup, mất Wi-Fi rồi kết nối lại và đổi IP máy chủ. Build thành công không thay thế kiểm thử thiết bị thật.
