# 📘 HƯỚNG DẪN CẤU HÌNH MYSQL, SEED DỮ LIỆU & DANH SÁCH TÀI KHOẢN HỆ THỐNG

---

## 1. 🔑 Danh Sách Tài Khoản & Mật Khẩu Demo

Hệ thống đã cấu hình sẵn tính năng **Seed dữ liệu tự động**, sinh sẵn 5 tài khoản tương ứng với các vai trò (Role) trong hệ thống:

| Vai trò (Role) | Email đăng nhập | Mật khẩu | Họ và tên | Trang đích sau Login | Quyền hạn chính |
|---|---|---|---|---|---|
| **Admin** | `admin@onlinelearn.com` | `Admin@123` | Admin System | `/admin/dashboard` | Quản trị toàn hệ thống, cấu hình Settings, quản lý người dùng |
| **Customer** | `customer@onlinelearn.com` | `Customer@123` | Nguyễn Văn A | `/user/dashboard` | Xem khóa học đã mua (My Courses), làm Quiz, cập nhật Profile |
| **Expert** | `expert@onlinelearn.com` | `Expert@123` | Trần Văn B | `/content` | Quản lý nội dung môn học, bài giảng, ngân hàng câu hỏi |
| **Sale** | `sale@onlinelearn.com` | `Sale@123` | Lê Thị C | `/sale` | Quản lý đơn đăng ký khóa học (Registrations), tư vấn học viên |
| **Marketing** | `marketing@onlinelearn.com` | `Marketing@123` | Phạm Văn D | `/marketing` | Quản lý bài viết tin tức (Posts), quản lý Slider quảng cáo |

> 💡 **Ghi chú về bảo mật Demo:**
> - Mật khẩu đã được mã hóa bằng **BCrypt** trước khi lưu vào Database.
> - Sau khi đăng nhập, hệ thống tự động điều hướng đúng trang Dashboard theo vai trò của tài khoản.

---

## 2. 🛠️ Hướng Dẫn Cấu Hình Kết Nối MySQL

Toàn bộ cấu hình kết nối Database nằm tại file:
📁 **`src/main/resources/application-dev.yml`**

### Đoạn cấu hình kết nối:
```yaml
spring:
  datasource:
    # 1. URL kết nối và tên Database (mặc định: onlinelearn_db)
    url: jdbc:mysql://localhost:3306/onlinelearn_db?useSSL=false&serverTimezone=UTC&createDatabaseIfNotExist=true&allowPublicKeyRetrieval=true
    
    # 2. Tài khoản MySQL của bạn (mặc định: root)
    username: root
    
    # 3. Mật khẩu MySQL của bạn (mặc định: root hoặc lấy từ biến môi trường DB_PASSWORD)
    password: ${DB_PASSWORD:root}
    
    driver-class-name: com.mysql.cj.jdbc.Driver

  jpa:
    hibernate:
      # Tự động cập nhật bảng khi thay đổi Entity
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQLDialect
        format_sql: true
```

### Các bước chỉnh sửa theo máy cá nhân:

1. **Đổi Mật khẩu MySQL:**
   - Nếu mật khẩu MySQL của bạn là `123456`, hãy sửa dòng `password`:
     ```yaml
     password: 123456
     ```
   - Hoặc bạn có thể truyền biến môi trường trước khi chạy mà không cần sửa code:
     - PowerShell: `$env:DB_PASSWORD="mật_khẩu_của_bạn"`
     - CMD: `set DB_PASSWORD=mật_khẩu_của_bạn`

2. **Đổi Tên Database:**
   - Thay đổi phần `/onlinelearn_db` trong dòng `url` thành tên database mong muốn (ví dụ `my_learning_db`):
     ```yaml
     url: jdbc:mysql://localhost:3306/my_learning_db?useSSL=false&serverTimezone=UTC&createDatabaseIfNotExist=true&allowPublicKeyRetrieval=true
     ```
   - Nhờ tham số `createDatabaseIfNotExist=true`, MySQL sẽ **tự động tạo database mới** nếu chưa tồn tại.

3. **Đổi Cổng Port kết nối:**
   - Nếu MySQL chạy ở cổng khác (ví dụ `3307`), sửa `localhost:3306` thành `localhost:3307`.

---

## 3. 🔄 Hướng Dẫn Reset / Chạy Lại Seed Dữ Liệu Mẫu

### Cơ chế hoạt động của Seeder:
- Class `com.onlinelearn.database.DataSeeder` triển khai `CommandLineRunner`.
- Khi khởi động, seeder kiểm tra:
  1. `app.seed-data: true` trong `application.yml`.
  2. Bảng `users` trong database đang rỗng (`userRepository.count() == 0`).
- Nếu thỏa mãn 2 điều kiện trên, hệ thống sẽ tự động tạo mới toàn bộ: Cài đặt $\rightarrow$ Tài khoản $\rightarrow$ Khóa học $\rightarrow$ Bài học $\rightarrow$ Câu hỏi Quiz $\rightarrow$ Đơn đăng ký mẫu.

### Cách 1: Đổi tên Database mới (Đơn giản & Khuyến nghị nhất ⭐)
1. Mở file `src/main/resources/application-dev.yml`.
2. Đổi tên database trong `url` thành tên mới (ví dụ từ `onlinelearn_db` sang `onlinelearn_db_v2`):
   ```yaml
   url: jdbc:mysql://localhost:3306/onlinelearn_db_v2?...
   ```
3. Chạy lại ứng dụng: Spring Boot sẽ tự tạo database mới và chạy lại seeder 100%.

---

### Cách 2: Xóa Database cũ trong MySQL Workbench / Navicat / Command Line
1. Mở MySQL Client (hoặc MySQL Workbench/DBeaver/Navicat/Terminal).
2. Chạy 2 câu lệnh SQL:
   ```sql
   DROP DATABASE IF EXISTS onlinelearn_db;
   CREATE DATABASE onlinelearn_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```
3. Khởi động lại ứng dụng Spring Boot.

---

### Cách 3: Cấu hình Hibernate ddl-auto tự động xóa & tạo lại
1. Mở file `src/main/resources/application-dev.yml`.
2. Tạm thời đổi dòng `ddl-auto`:
   ```yaml
   jpa:
     hibernate:
       ddl-auto: create-drop   # hoặc create
   ```
3. Chạy ứng dụng một lần để Hibernate drop & recreate sạch toàn bộ schema và seeder sẽ insert lại dữ liệu.
4. Sau khi chạy xong, đổi lại thành `update` để tránh mất dữ liệu ở các lần chạy sau.

---

## 4. 🚀 Hướng Dẫn Khởi Chạy Ứng Dụng

Mở Terminal / PowerShell tại thư mục gốc dự án (`d:\HOC\SBA301\online-learning-system`):

```powershell
# Chạy ứng dụng với Maven Wrapper
.\mvnw.cmd spring-boot:run
```

Khi ứng dụng khởi động thành công, màn hình console sẽ hiển thị thông báo:

```
====================================================================================
✅ Seed data completed.
👉 Admin login:     admin@onlinelearn.com / Admin@123
👉 Customer login:  customer@onlinelearn.com / Customer@123
👉 Expert login:    expert@onlinelearn.com / Expert@123
👉 Sale login:      sale@onlinelearn.com / Sale@123
👉 Marketing login: marketing@onlinelearn.com / Marketing@123
====================================================================================
```

### Các đường dẫn kiểm tra trực tiếp trên trình duyệt:
- 🌐 Trang chủ: [http://localhost:8080](http://localhost:8080)
- 🔐 Trang đăng nhập: [http://localhost:8080/login](http://localhost:8080/login)
- 📝 Trang đăng ký: [http://localhost:8080/register](http://localhost:8080/register)
- 📊 Admin Portal: [http://localhost:8080/admin/dashboard](http://localhost:8080/admin/dashboard)
- 📚 Học viên Portal: [http://localhost:8080/user/dashboard](http://localhost:8080/user/dashboard)
