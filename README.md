# 🚀 IT WORLD - Developer Community & Real-time Platform

> **Ứng dụng Mạng xã hội Công nghệ & Nhắn tin thời gian thực dành riêng cho cộng đồng Lập trình viên IT.**  
> Dự án môn học Lập trình Mạng / Java Nâng cao • Tác giả: **Nguyễn Trần Ngọc Anh (24ITB012)**

---

## 📌 Giới thiệu dự án

**IT World** là một ứng dụng Desktop hoàn chỉnh được xây dựng trên nền tảng **Java Swing (Modern UI)** và kiến trúc **Client - Server kết hợp P2P (Peer-to-Peer)**. Ứng dụng tích hợp hệ quản trị cơ sở dữ liệu **MySQL (XAMPP)**, cho phép các lập trình viên kết nối, chia sẻ kiến thức, giao lưu học hỏi và cộng tác làm việc trong cùng mạng LAN hoặc Internet.

---

## ✨ Các tính năng chính

### 1. 📝 Bảng tin Công nghệ (Tech Feed)
- **Đăng bài viết kỹ thuật:** Hỗ trợ tiêu đề, nội dung phân tích, phân loại thẻ tag (`#java`, `#springboot`, `#docker`...).
- **Hộp mã nguồn (Code Snippet Box):** Giao diện Dark Theme dành riêng cho lập trình viên, làm nổi bật mã nguồn và hỗ trợ nút sao chép code 1-chạm tiện lợi.
- **Tương tác thời gian thực:** Thả tim (Like/Unlike), bình luận trao đổi kỹ thuật, bộ lọc bài viết *"Tất cả"* hoặc *"Đang theo dõi"*.

### 2. 👤 Hồ sơ Lập trình viên (Tech Profile)
- **Thông tin chi tiết:** Cập nhật trình độ IT (*Fresher, Junior, Senior, Tech Lead, Solution Architect...*), Tech Stack, Tiểu sử (Bio), đường dẫn GitHub cá nhân.
- **Thống kê chuyên môn:** Đếm số lượng bài viết đã đăng, số người theo dõi (Followers) và số người đang theo dõi (Following).

### 3. 🌐 Mạng lưới kết nối (Dev Network)
- **Khám phá lập trình viên:** Danh sách cộng đồng IT với thẻ Level màu sắc trực quan.
- **Theo dõi (Follow System):** Theo dõi các lập trình viên yêu thích để cập nhật bài viết mới nhất từ họ.
- **Kết nối nhanh:** Mở cửa sổ chat 1-1 trực tiếp chỉ với 1 click.

### 4. 💬 Nhắn tin thời gian thực (Real-time Socket Chat)
- Trò chuyện 1-1 bảo mật và mượt mà qua luồng TCP Socket Server.
- Giao diện bong bóng tin nhắn (Message Bubble) chuẩn mực, hiển thị thời gian và trạng thái tin nhắn.

### 5. ⚡ Truyền file P2P tốc độ cao (P2P Binary File Transfer)
- Chuyển giao file nhị phân (hình ảnh, tài liệu, file mã nguồn, đồ án...) trực tiếp giữa 2 máy Client thông qua cổng Socket ngang hàng riêng (Port 9000+).
- Tải file tốc độ tối đa của mạng LAN mà không làm nghẽn băng thông của Server trung tâm.

---

## 🛠️ Công nghệ sử dụng

| Phân hệ | Công nghệ |
| :--- | :--- |
| **Ngôn ngữ** | Java 21 (LTS) |
| **Giao diện (UI)** | Java Swing, AWT, Graphics2D Custom Vector Icons (chống vỡ icon / tofu box) |
| **Mạng (Networking)** | Java Sockets (`ServerSocket`, `Socket`), Object Serialization (`ObjectInputStream` / `ObjectOutputStream`) |
| **Kiến trúc mạng** | Client - Server (Chat & Quản lý dữ liệu) + P2P PeerService (Truyền file nhị phân) |
| **Cơ sở dữ liệu** | MySQL 8.x (XAMPP Server), kết nối qua `mysql-connector-j-9.3.0.jar` |
| **Cơ chế dự phòng** | Tự động chuyển đổi sang In-Memory Cache nếu mất kết nối Database |

---

## 📁 Cấu trúc thư mục

```
24ITB012_Nguyen Tran Ngoc Anh_LAB4/
├── lib/
│   └── mysql-connector-j-9.3.0.jar     # Thư viện JDBC Driver kết nối MySQL
├── src/
│   ├── client/                          # Giao diện và dịch vụ phía Client
│   │   ├── AppIcons.java                # Vẽ icon vector thuần Graphics2D
│   │   ├── ChatAreaPanel.java           # Khung chat và gửi file
│   │   ├── ClientNetworkService.java    # Xử lý giao tiếp mạng Client
│   │   ├── CreatePostDialog.java        # Hộp thoại soạn thảo bài viết
│   │   ├── DevNetworkPanel.java         # Màn hình khám phá lập trình viên
│   │   ├── EditProfileDialog.java       # Hộp thoại cập nhật hồ sơ
│   │   ├── FeedPanel.java               # Màn hình Bảng tin công nghệ
│   │   ├── ITWorldMainFrame.java        # Cửa sổ chính (Navigation Rail + Tabs)
│   │   ├── LoginGUI.java                # Màn hình Đăng nhập & Đăng ký
│   │   ├── PeerService.java             # Server/Client truyền file P2P
│   │   └── ...
│   ├── model/                           # Đối tượng truyền nhận dữ liệu (Serializable)
│   │   ├── Message.java                 # Gói tin giao tiếp qua Socket
│   │   ├── MessageType.java             # Định nghĩa loại yêu cầu/phản hồi
│   │   ├── Post.java                    # Mô hình bài viết
│   │   ├── Comment.java                 # Mô hình bình luận
│   │   ├── User.java                    # Mô hình tài khoản
│   │   └── UserProfile.java             # Mô hình hồ sơ cá nhân
│   └── server/                          # Dịch vụ phía Server
│       ├── Server.java                  # Máy chủ Socket trung tâm (Port 8000)
│       └── DatabaseManager.java         # Quản trị thao tác CSDL XAMPP MySQL
├── schema.sql                           # Script khởi tạo Database `it_world`
├── .gitignore                           # Quy tắc loại trừ file rác / file nhị phân
└── README.md                            # Tài liệu hướng dẫn dự án
```

---

## 🚀 Hướng dẫn cài đặt & Khởi chạy

### Bước 1: Khởi động XAMPP & CSDL MySQL
1. Mở ứng dụng **XAMPP Control Panel**, nhấn **Start** tại dịch vụ **Apache** và **MySQL**.
2. Truy cập trình duyệt vào địa chỉ: [http://localhost/phpmyadmin](http://localhost/phpmyadmin)
3. Tạo Database tên `it_world` hoặc nhập trực tiếp từ file [schema.sql](schema.sql):
   - Chọn tab **Import** -> Chọn file `schema.sql` -> Bấm **Import**.

### Bước 2: Chạy Server trung tâm
- Trong IntelliJ IDEA: Mở file `src/server/Server.java` và chọn **Run 'Server.main()'**.
- Hoặc chạy qua Terminal:
  ```bash
  javac --release 21 -encoding UTF-8 -cp "lib/*" -d out/production/Lab4 src/server/*.java src/model/*.java
  java -cp "out/production/Lab4;lib/*" server.Server
  ```
- Khi Server khởi động thành công sẽ hiển thị:
  ```
  ✅ [DatabaseManager] Kết nối XAMPP MySQL thành công (Database: it_world)!
  -> Máy chủ đã sẵn sàng lắng nghe kết nối từ các lập trình viên...
  ```

### Bước 3: Chạy Client (Đăng nhập / Đăng ký)
- Trong IntelliJ IDEA: Mở file `src/client/LoginGUI.java` và chọn **Run 'LoginGUI.main()'**.
- Hoặc chạy qua Terminal:
  ```bash
  java -cp "out/production/Lab4;lib/*" client.LoginGUI
  ```
- **Tài khoản mẫu có sẵn:**
  - `alex_dev` (Mật khẩu: `123456`)
  - `sarah_tech` (Mật khẩu: `123456`)
  - `david_cloud` (Mật khẩu: `123456`)
  - Hoặc bạn có thể tự bấm nút **"Tạo tài khoản"** để đăng ký tài khoản mới cho riêng mình.

---

## 🤝 Tác giả & Bản quyền
- **Họ và tên:** Nguyễn Trần Ngọc Anh
- **Mã sinh viên:** 24ITB012
- **Trường:** Đại học Công nghệ Thông tin & Truyền thông Việt - Hàn (VKU), Đại học Đà Nẵng
- Dự án phục vụ mục đích học tập và nghiên cứu.
