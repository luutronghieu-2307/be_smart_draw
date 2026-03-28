src/main/java/luutronghieu/doan/
├── controller/
│   └── VisionController.java          # REST API endpoints
├── service/
│   └── InferenceService.java          # AI inference với DJL
├── dto/
│   └── DetectionResult.java           # DTO cho kết quả detection
├── DoanApplication.java               # Main class
└── resources/application.properties   # Cấu hình

assets/models/
├── yolov8n.onnx                       # Model YOLOv8
└── classes.txt                        # 80 class names COCO

Cấu hình:
├── docker-compose.yml                 # + MediaMTX service
├── Dockerfile                         # + Copy assets
├── mediamtx.yml                       # MediaMTX config
└── pom.xml                            # + DJL dependencies
