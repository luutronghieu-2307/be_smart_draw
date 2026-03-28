# Phân tích `VisionController.java`

Lớp này định nghĩa các API endpoint để tương tác với dịch vụ nhận dạng.

### Thư viện quan trọng

*   **`org.springframework.web.bind.annotation.*`**: Cung cấp các chú thích để định nghĩa API, bao gồm:
    *   `@RestController`: Đánh dấu lớp này là một controller xử lý các yêu cầu HTTP và trả về JSON.
    *   `@RequestMapping`: Định nghĩa tiền tố chung cho tất cả các API trong lớp (ví dụ: `/api/vision`).
    *   `@PostMapping`, `@GetMapping`: Ánh xạ các yêu cầu HTTP POST và GET tới các phương thức cụ thể.
    *   `@RequestParam`: Trích xuất dữ liệu từ các tham số của yêu cầu (ví dụ: lấy tệp từ form-data).
*   **`org.springframework.http.ResponseEntity`**: Đại diện cho toàn bộ phản hồi HTTP, cho phép kiểm soát mã trạng thái (status code), tiêu đề (headers), và nội dung (body).
*   **`io.swagger.v3.oas.annotations.*`**: Các chú thích từ thư viện SpringDoc OpenAPI, dùng để tự động tạo tài liệu cho API.
*   **`lombok.extern.slf4j.Slf4j`**: Kích hoạt khả năng ghi log bằng cách thêm một đối tượng `log` vào lớp.
*   **`ai.djl.modality.cv.output.DetectedObjects`**: Kiểu dữ liệu của DJL, chứa kết quả thô trả về từ mô hình AI.

### Các hàm chính

*   **`public ResponseEntity<DetectionResult> detectPeople(@RequestParam("file") MultipartFile file)`**:
    *   **Chức năng**: Đây là API chính (`POST /detect-people`). Nó nhận một tệp hình ảnh, kiểm tra tính hợp lệ, gọi `InferenceService` để xử lý, sau đó trả về kết quả nhận dạng được đóng gói trong `DetectionResult`.
    *   **Xử lý lỗi**: Trả về các mã lỗi HTTP (400, 500, 503) nếu tệp không hợp lệ hoặc có lỗi xảy ra trong quá trình xử lý.

*   **`public ResponseEntity<String> healthCheck()`**:
    *   **Chức năng**: Một API đơn giản (`GET /health`) để kiểm tra xem mô hình AI đã được tải và sẵn sàng hoạt động hay chưa.

*   **`private boolean isValidImageFile(MultipartFile file)`**:
    *   **Chức năng**: Một hàm tiện ích nội bộ, dùng để kiểm tra xem loại nội dung (MIME type) của tệp được tải lên có phải là một trong các định dạng ảnh được hỗ trợ (JPEG, PNG, BMP) hay không.