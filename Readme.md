# AI-IoT Smart Drawer Project

Dự án này là một hệ thống phát hiện đối tượng sử dụng mô hình YOLOv8, được xây dựng bằng Java Spring Boot và triển khai với Docker.

## Yêu cầu

-   [Docker](https://www.docker.com/get-started)
-   [Docker Compose](https://docs.docker.com/compose/install/)
-   [Git](https://git-scm.com/downloads)

## Cài đặt và Khởi chạy

1.  **Clone repository từ Git:**
    Mở terminal và chạy lệnh sau để tải mã nguồn về máy của bạn.

    ```bash
    git clone <URL_CỦA_REPOSITORY>
    ```
    *(Thay thế `<URL_CỦA_REPOSITORY>` bằng URL thực tế của kho Git của bạn)*

2.  **Di chuyển vào thư mục dự án:**

    ```bash
    cd doan
    ```
    *(Giả sử tên thư mục sau khi clone là `doan`)*

3.  **Khởi chạy hệ thống với Docker Compose:**
    Lệnh này sẽ build các image cần thiết (bao gồm ứng dụng Java) và khởi chạy tất cả các dịch vụ (API, MQTT Broker, Media Server) được định nghĩa trong `docker-compose.yml`.

    ```bash
    docker-compose up --build
    ```
    Cờ `--build` đảm bảo rằng Docker sẽ build lại image của ứng dụng nếu có bất kỳ thay đổi nào trong mã nguồn. Quá trình build lần đầu có thể mất vài phút.

## Kiểm tra

Sau khi tất cả các container đã khởi động thành công, bạn có thể kiểm tra xem dịch vụ API đã hoạt động đúng hay chưa.

1.  Mở trình duyệt web của bạn.
2.  Truy cập vào địa chỉ sau:
    ```
    http://localhost:8080/swagger-ui.html
    ```
3.  Nếu bạn thấy giao diện Swagger UI với danh sách các API (như `Vision API`), điều đó có nghĩa là ứng dụng đã chạy thành công.

## Xử lý lỗi

Trong quá trình phát triển, nếu bạn gặp lỗi khi khởi động hoặc ứng dụng hoạt động không như mong muốn, cách tốt nhất là dừng, xóa toàn bộ container và volume, sau đó khởi động lại.

1.  **Dừng và xóa các container, network và volume:**
    Lệnh này sẽ dọn dẹp hoàn toàn môi trường Docker của dự án, xóa cả các volume chứa dữ liệu (nếu có).

    ```bash
    docker-compose down -v
    ```

2.  **Khởi động lại:**
    Sau khi đã dọn dẹp, bạn có thể chạy lại lệnh build và khởi động.

    ```bash
    docker-compose up --build
    ```