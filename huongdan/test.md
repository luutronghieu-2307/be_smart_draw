Cách test hệ thống:
Chạy docker-compose up để khởi động hệ thống
Truy cập http://localhost:8080/swagger-ui.html
Sử dụng endpoint POST /api/vision/test-image để upload ảnh
Nhận kết quả JSON với các object detected, bounding boxes, confidence scores