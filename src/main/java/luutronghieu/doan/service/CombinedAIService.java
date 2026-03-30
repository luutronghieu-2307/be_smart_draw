package luutronghieu.doan.service;

import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.output.BoundingBox;
import ai.djl.modality.cv.output.DetectedObjects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import luutronghieu.doan.dto.TravelPredictionResult;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CombinedAIService {

    private final InferenceService inferenceService; // YOLOv8
    private final ImageClassificationService classificationService; // MobileNetV2

    private static final List<String> TRAVEL_ITEMS = Arrays.asList("Backpack", "LongCoat", "HandBag");
    private static final double CLASSIFICATION_THRESHOLD = 0.3; // Ngưỡng tin cậy cho phân loại

    public TravelPredictionResult predictTravel(byte[] imageBytes) throws Exception {
        // 1. Chạy YOLOv8 để phát hiện người
        DetectedObjects detections = inferenceService.runInference(imageBytes);
        List<DetectedObjects.DetectedObject> people = new ArrayList<>();
        List<DetectedObjects.DetectedObject> detectedItemsList = detections.items();
        for (int i = 0; i < detectedItemsList.size(); i++) {
            DetectedObjects.DetectedObject d = detectedItemsList.get(i);
            if ("person".equals(d.getClassName())) {
                people.add(d);
            }
        }

        if (people.isEmpty()) {
            log.info("No people detected in the image.");
            return new TravelPredictionResult("success", new ArrayList<>(), Arrays.asList("YOLOv8"));
        }

        log.info("Detected {} people in the image.", people.size());

        List<TravelPredictionResult.PersonPrediction> predictions = new ArrayList<>();

        for (DetectedObjects.DetectedObject person : people) {
            // Mỗi lần cắt ảnh, hãy đọc lại ảnh gốc từ byte array để tránh lỗi ranh giới
            BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(imageBytes));
            
            // 2. Cắt ảnh của từng người từ ảnh gốc mới
            BufferedImage croppedPersonImage = cropImage(originalImage, person.getBoundingBox());

            // 3. Thêm padding để ảnh thành hình vuông
            BufferedImage paddedImage = addPadding(croppedPersonImage);

            // Chuyển đổi ảnh đã xử lý thành byte array để đưa vào MobileNetV2
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(paddedImage, "jpg", baos);
            byte[] personImageBytes = baos.toByteArray();

            // 4. Chạy MobileNetV2 để phân loại
            ai.djl.modality.Classifications classifications = classificationService.runInference(personImageBytes);

            // 5. Phân tích kết quả và đưa ra kết luận
            TravelPredictionResult.PersonPrediction prediction = analyzeClassification(person, classifications);
            predictions.add(prediction);
        }

        return new TravelPredictionResult("success", predictions, Arrays.asList("YOLOv8", "MobileNetV2"));
    }

    private BufferedImage cropImage(BufferedImage originalImage, BoundingBox box) {
        int imgWidth = originalImage.getWidth();
        int imgHeight = originalImage.getHeight();
        
        double boundsX = box.getBounds().getX();
        double boundsY = box.getBounds().getY();
        double boundsWidth = box.getBounds().getWidth();
        double boundsHeight = box.getBounds().getHeight();
        
        log.debug("Raw bounds from YOLOv8: x={}, y={}, w={}, h={}", 
                  boundsX, boundsY, boundsWidth, boundsHeight);
        
        // YOLOv8 được resize ảnh về 640x640 trong pipeline
        // Bounding box trả về là tọa độ trên ảnh 640x640
        // Nếu giá trị < 640, thì đó là tọa độ từ ảnh resized, cần scale lại về ảnh gốc
        final int YOLO_SIZE = 640;
        int x, y, width, height;
        
        if (boundsX < YOLO_SIZE && boundsY < YOLO_SIZE && boundsWidth < YOLO_SIZE && boundsHeight < YOLO_SIZE) {
            // Tọa độ từ ảnh 640x640 (resized) -> scale về ảnh gốc
            x = (int) (boundsX * imgWidth / YOLO_SIZE);
            y = (int) (boundsY * imgHeight / YOLO_SIZE);
            width = (int) (boundsWidth * imgWidth / YOLO_SIZE);
            height = (int) (boundsHeight * imgHeight / YOLO_SIZE);
            log.debug("Detected coordinates from 640x640 model, scaling to original {}x{}", imgWidth, imgHeight);
        } else if (boundsX <= 1.0 && boundsY <= 1.0 && boundsWidth <= 1.0 && boundsHeight <= 1.0) {
            // Normalized (0-1) coordinates
            x = (int) (boundsX * imgWidth);
            y = (int) (boundsY * imgHeight);
            width = (int) (boundsWidth * imgWidth);
            height = (int) (boundsHeight * imgHeight);
            log.debug("Detected normalized coordinates (0-1)");
        } else {
            // Absolute pixel coordinates
            x = (int) boundsX;
            y = (int) boundsY;
            width = (int) boundsWidth;
            height = (int) boundsHeight;
            log.debug("Detected absolute pixel coordinates");
        }

        log.debug("Crop before validation: x={}, y={}, w={}, h={} | Image: {}x{}", 
                  x, y, width, height, imgWidth, imgHeight);

        // Đảm bảo tọa độ không vượt ra ngoài
        x = Math.max(0, x);
        y = Math.max(0, y);
        
        // Giới hạn width và height để không vượt biên
        if (x + width > imgWidth) {
            width = imgWidth - x;
        }
        if (y + height > imgHeight) {
            height = imgHeight - y;
        }
        
        // Đảm bảo width và height phải > 0
        if (width <= 0) width = 1;
        if (height <= 0) height = 1;

        log.debug("Crop after validation: x={}, y={}, w={}, h={}", x, y, width, height);
        return originalImage.getSubimage(x, y, width, height);
    }

    private BufferedImage addPadding(BufferedImage source) {
        int size = Math.max(source.getWidth(), source.getHeight());
        BufferedImage paddedImage = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = paddedImage.createGraphics();

        // Tô nền màu đen
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, size, size);

        // Vẽ ảnh gốc vào giữa
        int x = (size - source.getWidth()) / 2;
        int y = (size - source.getHeight()) / 2;
        g2d.drawImage(source, x, y, null);
        g2d.dispose();

        return paddedImage;
    }

    private TravelPredictionResult.PersonPrediction analyzeClassification(DetectedObjects.DetectedObject person, ai.djl.modality.Classifications classifications) {
        List<String> detectedItems = new ArrayList<>();
        double maxConfidence = 0.0;

        for (ai.djl.modality.Classifications.Classification item : classifications.items()) {
            if (TRAVEL_ITEMS.contains(item.getClassName()) && item.getProbability() >= CLASSIFICATION_THRESHOLD) {
                detectedItems.add(item.getClassName());
                if (item.getProbability() > maxConfidence) {
                    maxConfidence = item.getProbability();
                }
            }
        }

        TravelPredictionResult.TravelStatus status = detectedItems.isEmpty() ? 
            TravelPredictionResult.TravelStatus.KHONG_DI_XA : 
            TravelPredictionResult.TravelStatus.DI_XA;

        log.info("Person at {} -> Travel Status: {}, Detected Items: {}", person.getBoundingBox(), status, detectedItems);

        // Tạo BoundingBoxData từ BoundingBox object
        var bounds = person.getBoundingBox().getBounds();
        TravelPredictionResult.BoundingBoxData bboxData = new TravelPredictionResult.BoundingBoxData(
            bounds.getX(),
            bounds.getY(),
            bounds.getWidth(),
            bounds.getHeight()
        );

        return new TravelPredictionResult.PersonPrediction(
                bboxData,
                detectedItems,
                status,
                maxConfidence
        );
    }
}