# Phân tích `DoanApplication.java`

Tệp này là điểm khởi đầu của ứng dụng Spring Boot.

### Thư viện quan trọng

*   **`org.springframework.boot.SpringApplication`**: Lớp cốt lõi của Spring Boot, chứa phương thức `run()` để khởi chạy toàn bộ ứng dụng.
*   **`org.springframework.boot.autoconfigure.SpringBootApplication`**: Đây là một chú thích (annotation) đặc biệt, nó bao gồm 3 chức năng:
    1.  `@EnableAutoConfiguration`: Tự động cấu hình ứng dụng dựa trên các thư viện có trong classpath.
    2.  `@ComponentScan`: Tự động quét các thành phần (như `@RestController`, `@Service`) trong cùng gói và các gói con.
    3.  `@Configuration`: Cho phép đăng ký các bean bổ sung trong ngữ cảnh ứng dụng.

### Hàm chính

*   **`public static void main(String[] args)`**: Đây là phương thức tiêu chuẩn của Java, là nơi chương trình bắt đầu thực thi. Bên trong nó gọi `SpringApplication.run()`, giao phó toàn bộ quyền kiểm soát cho Spring Boot để khởi động máy chủ web và quản lý các thành-phần-ứng-dụng.