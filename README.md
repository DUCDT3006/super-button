# Super Button - Nút Nguồn Ảo Đa Năng Cho Samsung Galaxy Note 20

Ứng dụng Android Native thay thế hoàn toàn nút nguồn vật lý bị mất hoặc hỏng trên điện thoại Samsung Galaxy Note 20.

---

## 🌟 Tính Năng Nổi Bật

1. **Khóa Màn Hình An Toàn (Screen Lock)**
   - Sử dụng chuẩn `AccessibilityService` của Android (API 28+).
   - **Giữ trọn vẹn mở khóa vân tay / khuôn mặt**: Không bị ép buộc nhập mã PIN/mật khẩu như các ứng dụng dùng Device Admin cũ.

2. **Menu Tắt Nguồn & Khởi Động Lại (Power Dialog)**
   - Mở trực tiếp bảng chọn gốc của Samsung gồm: **Tắt nguồn (Power off)**, **Khởi động lại (Restart)**, và **Chế độ khẩn cấp (Emergency Mode)**.

3. **Chụp Ảnh Màn Hình Nhanh (Screenshot)**
   - Thao tác 1 chạm hoặc giữ nút để chụp màn hình thay vì phải bấm tổ hợp phím cứng.

4. **Kéo Thả Di Chuyển Tự Do 360° (Draggable)**
   - Chạm giữ và di chuyển nút đến bất kỳ vị trí nào trên màn hình.
   - **Tùy chọn Hút mép tự động (Magnetic Snap)**: Thả tay ra nút sẽ tự lướt êm ái nép sát vào mép trái hoặc mép phải màn hình.
   - **Tự động ghi nhớ vị trí**: Tọa độ $(X, Y)$ được lưu lại, không bị reset vị trí khi tắt mở máy.

5. **Độ Trong Suốt & Tự Động Làm Mờ (Frosted Glass & Auto-fade)**
   - Tùy chỉnh độ mờ khi chạm (Active Opacity: 20% - 100%).
   - Tùy chỉnh độ mờ khi nhàn rỗi (Idle Opacity: 5% - 80%).
   - Tự động mờ sâu sau 1 - 10 giây không chạm để không che nội dung xem phim, đọc báo, chơi game.

6. **Tùy Biến Cử Chỉ (Gesture Mapping)**
   - **1 chạm (Single Tap)**: Tùy chọn Khóa màn hình / Mở menu tròn / Âm lượng...
   - **2 chạm (Double Tap)**: Tùy chọn Menu Tắt nguồn / Screenshot...
   - **Nhấn giữ (Long Press)**: Tùy chọn Chụp ảnh màn hình / Thanh thông báo...
   - **Rung phản hồi (Haptic Feedback)**: Rung nhẹ mô phỏng cảm giác bấm nút vật lý.

7. **Menu Tròn Mở Rộng (Quick Radial Menu)**
   - Bung menu phím tắt: Nguồn, Khóa, Âm lượng, Ảnh chụp, Thông báo, Phím Home.

---

## 🛠 Hướng Dẫn Cài Đặt & Biên Dịch (Build APK)

### Cách 1: Mở trực tiếp bằng Android Studio (Khuyên dùng)
1. Tải và cài đặt [Android Studio](https://developer.android.com/studio).
2. Mở Android Studio -> Chọn **Open** -> Trỏ đến thư mục dự án `super button`.
3. Chờ Gradle đồng bộ (Sync) xong.
4. Bật chế độ **USB Debugging** (Gỡ lỗi USB) trên Samsung Note 20, cắm cáp kết nối vào máy tính.
5. Nhấn nút **Run ▶** (hoặc tổ hợp phím `Shift + F10`) để cài trực tiếp ứng dụng lên điện thoại.

### Cách 2: Build file APK độc lập qua lệnh
Mở terminal tại thư mục dự án và chạy:
```bash
# Windows
./gradlew assembleDebug

# File APK tạo ra sẽ nằm tại:
# app/build/outputs/apk/debug/app-debug.apk
```
Chép file `app-debug.apk` vào điện thoại Note 20 và nhấn cài đặt.

---

## 🚀 Hướng Dẫn Kích Hoạt Lần Đầu Trên Samsung Note 20

1. **Mở ứng dụng Super Button**:
   - Nhấn **CẤP QUYỀN** tại mục *Quyền Hiển thị trên ứng dụng khác* -> Gạt nút bật cho Super Button.
   - Nhấn **CẤP QUYỀN** tại mục *Quyền Trợ năng (Accessibility)* -> Trong danh sách dịch vụ đã cài đặt, chọn **Super Button - Hỗ trợ Nút Nguồn Ảo** -> Bật ON.
2. **Bật nút nguồn ảo**:
   - Gạt công tắc **Kích Hoạt Nút Nguồn Ảo** sang BẬT. Nút ảo sẽ lập tức xuất hiện trên màn hình.
3. **Cài đặt đánh thức màn hình (Vì mất nút nguồn vật lý)**:
   - Vào **Cài đặt** của điện thoại Samsung -> **Tính năng nâng cao** -> **Chuyển động và thao tác**:
     - Bật **"Chạm hai lần để bật màn hình"**.
     - Bật **"Nhấc lên để đánh thức"**.
   - Kích hoạt **Mở khóa bằng vân tay dưới màn hình**.
   *(Bây giờ: Chạm vân tay/chạm 2 lần để BẬT máy -> Chạm nút ảo Super Button để TẮT/KHỞI ĐỘNG LẠI máy).*
