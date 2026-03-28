package luutronghieu.doan.service;

import ai.djl.ModelException;
import ai.djl.inference.Predictor;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.modality.cv.transform.Resize;
import ai.djl.modality.cv.transform.ToTensor;
import ai.djl.modality.cv.translator.YoloV5Translator;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelZoo;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.translate.Pipeline;
import ai.djl.translate.TranslateException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import luutronghieu.doan.dto.DetectionResult;

@Service
@Slf4j
public class InferenceService {

    @Value("${ai.model.path}")
    private String modelPath;

    @Value("${ai.model.confidence.threshold:0.5}")
    private float confidenceThreshold;

    private ZooModel<Image, DetectedObjects> model;
    private Predictor<Image, DetectedObjects> predictor;

    @PostConstruct
    public void initialize() {
        log.info("Initializing YOLOv8 ONNX model from: {}", modelPath);
        
        try {
            Path modelFile = Paths.get(modelPath);
            if (!modelFile.toFile().exists()) {
                throw new IOException("Model file not found at: " + modelFile.toAbsolutePath());
            }
            
            log.info("Model file found: {}", modelFile.toAbsolutePath());

            // Tạo pipeline để resize ảnh về 640x640
            Pipeline pipeline = new Pipeline();
            pipeline.add(new Resize(640, 640))
                    .add(new ToTensor());

            // Tạo translator với pipeline resize
            YoloV5Translator translator = YoloV5Translator.builder()
                    .setPipeline(pipeline)
                    .optThreshold(confidenceThreshold)
                    .build();

            // Load model với ONNX Runtime engine
            Criteria<Image, DetectedObjects> criteria = Criteria.builder()
                    .setTypes(Image.class, DetectedObjects.class)
                    .optModelPath(modelFile)
                    .optTranslator(translator)
                    .optEngine("OnnxRuntime")
                    .build();

            log.info("Loading ONNX model...");
            this.model = ModelZoo.loadModel(criteria);
            this.predictor = model.newPredictor();
            log.info("YOLOv8 ONNX model loaded successfully with resize pipeline");
            
        } catch (Exception e) {
            log.error("Failed to load model: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to load model: " + e.getMessage(), e);
        }
    }

    public boolean isModelLoaded() {
        return predictor != null;
    }

    public DetectedObjects runInference(byte[] imageBytes) throws ModelException, IOException, TranslateException {
        if (predictor == null) {
            throw new IllegalStateException("Model not loaded");
        }
        Image img = ImageFactory.getInstance().fromInputStream(new ByteArrayInputStream(imageBytes));
        log.info("Original image size: {}x{}", img.getWidth(), img.getHeight());
        return predictor.predict(img);
    }

    public DetectionResult convertToDetectionResult(DetectedObjects detections, byte[] originalImageBytes) throws IOException {
        Image originalImage = ImageFactory.getInstance().fromInputStream(new ByteArrayInputStream(originalImageBytes));
        
        DetectionResult result = new DetectionResult();
        result.setStatus("success");
        result.setInferenceTimeMs(System.currentTimeMillis());
        
        List<DetectedObjects.DetectedObject> allDetections = detections.items();
        
        // Lọc chỉ giữ lại "person"
        List<DetectedObjects.DetectedObject> peopleDetections = allDetections.stream()
                .filter(item -> "person".equalsIgnoreCase(item.getClassName()))
                .toList();
        
        log.info("Found {} people in image (total {} objects detected)", 
                 peopleDetections.size(), allDetections.size());
        
        if (peopleDetections.isEmpty()) {
            result.setMessage("No people detected");
        } else if (peopleDetections.size() == 1) {
            result.setMessage("Detected 1 person");
        } else {
            result.setMessage("Detected " + peopleDetections.size() + " people");
        }
        
        DetectionResult.ImageInfo imageInfo = new DetectionResult.ImageInfo();
        imageInfo.setOriginalWidth(originalImage.getWidth());
        imageInfo.setOriginalHeight(originalImage.getHeight());
        imageInfo.setProcessedWidth(640);
        imageInfo.setProcessedHeight(640);
        result.setImageInfo(imageInfo);
        
        List<DetectionResult.Detection> detectionList = new ArrayList<>();
        for (DetectedObjects.DetectedObject item : peopleDetections) {
            DetectionResult.Detection d = new DetectionResult.Detection();
            d.setClassName(item.getClassName());
            d.setConfidence(item.getProbability());
            
            var bounds = item.getBoundingBox().getBounds();
            // Bounding box đã được tính trên ảnh 640x640, cần scale lại về ảnh gốc
            DetectionResult.Detection.BoundingBox bbox = 
                new DetectionResult.Detection.BoundingBox(
                    (int) (bounds.getX() * originalImage.getWidth() / 640.0),
                    (int) (bounds.getY() * originalImage.getHeight() / 640.0),
                    (int) (bounds.getWidth() * originalImage.getWidth() / 640.0),
                    (int) (bounds.getHeight() * originalImage.getHeight() / 640.0)
                );
            d.setBbox(bbox);
            detectionList.add(d);
        }
        result.setDetections(detectionList);
        
        return result;
    }

    @PreDestroy
    public void close() {
        if (predictor != null) {
            predictor.close();
        }
        if (model != null) {
            model.close();
        }
        log.info("Model resources released.");
    }
}