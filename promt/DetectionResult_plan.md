# Phân tích `DetectionResult.java`

Lớp này là một **DTO (Data Transfer Object)**, dùng để định nghĩa cấu trúc dữ liệu JSON trả về cho client. Nó không chứa logic xử lý phức tạp.

### Thư viện quan trọng

*   **`lombok.*`**: Thư viện Lombok được sử dụng để làm cho mã nguồn cực kỳ gọn gàng.
    *   `@Data`: Một chú thích "thần kỳ" tự động tạo ra tất cả các phương thức cần thiết cho một lớp dữ liệu, bao gồm: `getters` (ví dụ: `getStatus()`), `setters` (ví dụ: `setStatus(...)`), `toString()`, `equals()`, và `hashCode()`.
    *   `@NoArgsConstructor`: Tự động tạo một hàm khởi tạo (constructor) rỗng, không có tham số. Điều này thường cần thiết cho các thư viện chuyển đổi JSON.
    *   `@AllArgsConstructor`: Tự động tạo một hàm khởi tạo với tất cả các trường của lớp làm tham số.

### Các hàm

Trong tệp `.java` thực tế, bạn sẽ không thấy bất kỳ phương thức nào được viết tường minh. Tất cả chúng (getters, setters, constructors,...) đều được **Lombok tự động sinh ra** trong quá trình biên dịch mã. Cấu trúc của lớp này chỉ đơn giản là khai báo các trường (fields) để xác định các thuộc tính sẽ có trong JSON output, bao gồm:

*   `status`: Trạng thái yêu cầu (`success` hoặc `error`).
*   `message`: Thông điệp mô tả.
*   `detections`: Danh sách các đối tượng được phát hiện, mỗi đối tượng có `className`, `confidence`, và `bbox` (hộp bao).
*   `inferenceTimeMs`: Thời gian xử lý.
*   `imageInfo`: Kích thước ảnh gốc và ảnh đã xử lý.
*   `detectionType`: Loại nhận dạng.