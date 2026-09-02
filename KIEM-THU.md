# Kết quả kiểm tra 02/09/2026

- PASS: 2 kiểm thử tích hợp máy chủ (phân quyền, báo cáo, ảnh, Excel, sao lưu, khởi động lại, khôi phục).
- PASS: Java ServerAddressTest kiểm tra URL LAN/HTTPS và cùng nguồn.
- PASS: toàn bộ Java Android biên dịch bằng compiler Java 17 với android.jar API 35.
- PASS: XML manifest/icon và cấu trúc workflow.
- PASS: đóng gói APK bằng Android Build Tools 35 (aapt2, d8, zipalign, apksigner).
- PASS: chữ ký APK v2/v3, package vn.lekhaccong.congviecteam, version 1.0.0, min API 26, target API 35.
- Chưa xác nhận Gradle assembleDebug/lintDebug toàn bộ: runtime cục bộ thiếu launcher javac nên Gradle báo JAVA_COMPILER. APK kèm theo được tạo bằng compiler Java 17 và Android Build Tools trực tiếp. GitHub workflow cài JDK 17 đầy đủ qua setup-java.
- Chưa chạy trên điện thoại/emulator; cần thử chức năng Android thực tế, đặc biệt chọn tệp và nơi lưu backup.
- GitHub Actions sẽ kiểm tra lại build và lint trên JDK 17 đầy đủ khi mã được đưa lên repo lekhaccong/CongViecTeam.

## 1.1.0 — camera

- PASS: JavaScript trong trình duyệt thật trên server LAN không sửa đổi: thêm ảnh chụp vào ảnh đang chọn, bỏ từng ảnh, giới hạn 3 ảnh và 2 MB, hủy giữ ảnh cũ, gửi hai ảnh vào báo cáo và xác nhận máy chủ lưu đủ. Không có lỗi JavaScript.
- Build APK/lint được kiểm tra bởi GitHub Actions trên commit tương ứng.
- Chưa thử mở ứng dụng camera vật lý trên điện thoại; cần kiểm tra chụp ngang/dọc, hủy camera, chọn ảnh có sẵn và chọn tệp Excel.
