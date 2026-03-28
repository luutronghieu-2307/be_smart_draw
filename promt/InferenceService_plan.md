# Phân tích `InferenceService.java`

Lớp này là "bộ não" của ứng dụng, chịu trách nhiệm tải mô hình AI và thực hiện suy luận.

### Thư viện quan trọng

*   **`ai.djl.*`**: Bộ thư viện Deep Java Library (DJL) được sử dụng rộng rãi:
    *   `ai.djl.Application`: Định nghĩa loại tác vụ AI (ở đây là `ObjectDetection`).
    *   `ai.djl.repository.zoo.ModelZoo` và `Criteria`: Các lớp dùng để xây dựng yêu cầu và tải mô hình từ một vị trí cụ thể (ở đây là từ hệ thống tệp cục bộ).
    *   `ai.djl.modality.cv.*`: Các lớp tiện ích cho thị giác máy tính, như `ImageFactory` (để đọc ảnh từ mảng byte), `Image` (đại diện cho ảnh).
    *   `ai.djl.translate.*`: Chứa `Translator` (như `YoloV5Translator`), thành phần cực kỳ quan trọng giúp chuyển đổi dữ liệu giữa định dạng Java (`Image`) và định dạng tensor mà mô hình AI yêu cầu.
    *   `ai.djl.inference.Predictor`: Đối tượng được tối ưu hóa để thực hiện các lệnh dự đoán (suy luận) một cách hiệu quả.
*   **`jakarta.annotation.PostConstruct` và `jakarta.annotation.PreDestroy`**: Các chú thích chuẩn của Java để quản lý vòng đời của một bean. Spring Boot sẽ tự động gọi các phương thức được đánh dấu bằng các chú thích này.
*   **`org.springframework.stereotype.Service`**: Đánh dấu lớp này là một dịch vụ (service), cho phép Spring Boot quản lý và tiêm (inject) nó vào các lớp khác (như `VisionController`).

### Các hàm chính

*   **`void initialize()` (`@PostConstruct`)**:
    *   **Chức năng**: Hàm này được tự động gọi một lần ngay sau khi ứng dụng khởi động. Nó có nhiệm vụ quan trọng là tìm, tải mô hình AI (`.onnx`) vào bộ nhớ và chuẩn bị sẵn đối tượng `predictor` để có thể thực hiện suy luận ngay lập tức khi có yêu cầu.

*   **`void close()` (`@PreDestroy`)**:
    *   **Chức năng**: Hàm này được tự động gọi ngay trước khi ứng dụng tắt. Nó đảm bảo rằng mô hình và predictor được đóng lại một cách an toàn, giải phóng tài nguyên (bộ nhớ, GPU).

*   **`public DetectedObjects runInference(byte[] imageBytes)`**:
    *   **Chức năng**: Nhận đầu vào là dữ liệu của một hình ảnh, chuyển nó thành đối tượng `Image` của DJL, sau đó đưa vào `predictor` để thực hiện suy luận và trả về kết quả thô.

*   **`public DetectionResult convertToDetectionResult(...)`**:
    *   **Chức năng**: Chuyển đổi đối tượng `DetectedObjects` thô từ mô hình thành đối tượng `DetectionResult` thân thiện với người dùng. Nó lọc ra các đối tượng mong muốn (chỉ "person"), tính toán lại tọa độ của hộp bao (bounding box) cho phù hợp với kích thước ảnh gốc, và tạo thông điệp kết quả.