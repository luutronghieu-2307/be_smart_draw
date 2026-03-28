Đây là một dự án AI/IoT (Trí tuệ nhân tạo / Vạn vật kết nối) được xây dựng bằng Java Spring Boot, quản lý bởi Maven và triển khai dưới dạng các container Docker.

Mục tiêu chính của dự án có vẻ là xây dựng một hệ thống có khả năng:

Nhận và xử lý các luồng video/hình ảnh.
Sử dụng một mô hình AI (cụ thể là YOLOv8) để phát hiện đối tượng trong đó.
Giao tiếp với các thiết bị IoT khác thông qua giao thức MQTT.
Hãy cùng đi vào chi tiết từng phần:

1. pom.xml (Tệp quản lý dự án Maven)
Đây là "trái tim" của dự án, định nghĩa các công nghệ và thư viện được sử dụng.

Nền tảng:
Spring Boot (phiên bản 4.0.4): Một framework mạnh mẽ giúp xây dựng các ứng dụng Java, đặc biệt là các dịch vụ web (REST API), một cách nhanh chóng.
Java 21: Sử dụng phiên bản Java hiện đại.
Thư viện Trí tuệ nhân tạo (AI):
DJL (Deep Java Library): Đây là thư viện chủ chốt cho các tác vụ AI. Nó cho phép chạy các mô hình deep learning trực tiếp trong Java.
PyTorch Engine & ONNX Runtime Engine: Dự án đã tích hợp hai "engine" để chạy mô hình. Điều này cho thấy sự linh hoạt, nhưng engine chính đang được sử dụng là ONNX (dựa vào tệp yolov8n.onnx trong resources).
OpenCV: Một thư viện kinh điển về xử lý hình ảnh, có thể được dùng cho các tác vụ tiền xử lý (thay đổi kích thước, màu sắc ảnh) trước khi đưa vào mô hình AI.
Giao tiếp IoT:
Paho MQTT Client: Thư viện này cho phép ứng dụng Java kết nối đến một MQTT Broker để gửi và nhận tin nhắn. Đây là giao thức rất phổ biến trong các hệ thống IoT.
Khác:
SpringDoc OpenAPI: Tự động tạo tài liệu (documentation) cho các API của bạn, giúp việc kiểm thử và tích hợp dễ dàng hơn.
Lombok: Giúp giảm thiểu việc phải viết các đoạn code lặp đi lặp lại (như getters, setters).
2. docker-compose.yml (Tệp điều phối Container)
Tệp này mô tả kiến trúc hệ thống khi triển khai. Thay vì chạy mọi thứ trên một máy, dự án của bạn được chia thành các "dịch vụ" (services) độc lập, mỗi dịch vụ chạy trong một container Docker riêng.

doan-api (Dịch vụ API chính):

Đây chính là ứng dụng Spring Boot của bạn.
build: . nghĩa là Docker sẽ xây dựng image cho dịch vụ này từ Dockerfile trong thư mục gốc của dự án.
Mở cổng 8080 để bên ngoài có thể gọi vào các API.
volumes: - .:/app là một cấu hình rất hữu ích cho môi trường phát triển, nó ánh xạ toàn bộ thư mục code của bạn vào trong container. Khi bạn thay đổi code, thay đổi sẽ được cập nhật ngay lập tức mà không cần build lại image.
Phần cấu hình GPU (deploy.resources...) đang được chú thích. Điều này có nghĩa là hiện tại, ứng dụng được thiết lập để chạy trên CPU theo mặc định (như được xác nhận bởi tệp .env với GPU_COUNT=0).
mqtt-broker (Dịch vụ môi giới MQTT):

Sử dụng image eclipse-mosquitto, một MQTT broker rất phổ biến và nhẹ.
Đây là trung tâm giao tiếp, nơi các thiết bị IoT và dịch vụ API của bạn có thể gửi và nhận thông điệp từ nhau.
Mở cổng 1883 (cổng MQTT chuẩn) và 9001 (cho MQTT qua WebSockets).
mediamtx (Dịch vụ Media Server):

Sử dụng image bluenviron/mediamtx. Đây là một máy chủ media đa năng.
Vai trò của nó là nhận, quản lý và phân phối các luồng video từ các camera hoặc các nguồn khác qua các giao thức như RTSP (cổng 8554) và RTMP (cổng 1935).
Dịch vụ API của bạn có thể sẽ kết nối đến máy chủ này để lấy hình ảnh từ luồng video và thực hiện phân tích.
3. /src (Cấu trúc mã nguồn)
Đây là nơi chứa toàn bộ logic của ứng dụng Spring Boot, được tổ chức theo cấu trúc chuẩn của Maven.

DoanApplication.java: Điểm khởi đầu của ứng dụng.
controller/VisionController.java: Lớp này định nghĩa các API endpoints. Ví dụ, nó có thể có một API POST /detect nhận URL của một luồng video và trả về kết quả phát hiện đối tượng.
service/InferenceService.java: Đây là nơi chứa logic xử lý cốt lõi.
Nó sẽ nhận yêu cầu từ VisionController.
Sử dụng OpenCV để kết nối tới mediamtx và chụp một khung hình (frame) từ luồng video.
Sử dụng DJL để tải mô hình yolov8n.onnx từ thư mục resources/models.
Thực hiện suy luận (inference) trên khung hình để tìm ra các đối tượng.
Có thể gửi kết quả (ví dụ: "phát hiện một người") tới mqtt-broker.
dto/DetectionResult.java: (DTO - Data Transfer Object) Đây là một lớp Java đơn giản định nghĩa cấu trúc dữ liệu cho kết quả trả về. Nó có thể chứa các thông tin như: tên đối tượng (ví dụ: 'person', 'car'), tọa độ khung bao (bounding box), và độ tin cậy (confidence score).
resources/models/:
yolov8n.onnx: Tệp mô hình AI theo định dạng ONNX. Đây là mô hình YOLOv8 phiên bản "nano", là phiên bản nhỏ và nhanh.
synset.txt: Tệp này thường chứa danh sách các tên lớp (class names) mà mô hình có thể nhận dạng, tương ứng với thứ tự đầu ra của mô hình.
Tổng kết luồng hoạt động (Plan)
Một camera IP đẩy luồng video (RTSP/RTMP) đến dịch vụ mediamtx.
Người dùng hoặc một hệ thống khác gửi yêu cầu đến API của doan-api (thông qua VisionController).
InferenceService được kích hoạt. Nó kết nối đến mediamtx để lấy một khung hình từ luồng video.
Khung hình được tiền xử lý (nếu cần) bằng OpenCV.
InferenceService sử dụng thư viện DJL để đưa khung hình vào mô hình yolov8n.onnx và nhận về kết quả phát hiện.
Kết quả được định dạng bằng DetectionResult.java và trả về qua API.
Đồng thời, InferenceService có thể xuất bản một thông điệp lên mqtt-broker (ví dụ: topic: 'drawer/status', message: 'object_detected') để một thiết bị IoT khác (như một cái đèn, một cái khóa) có thể nhận và hành động.