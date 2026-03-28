package luutronghieu.doan.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DetectionResult {
    private String status;
    private String message;
    private List<Detection> detections;
    private long inferenceTimeMs;
    private ImageInfo imageInfo;
    private String detectionType;  // Thêm trường này

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Detection {
        private String className;
        private double confidence;
        private BoundingBox bbox;
        
        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class BoundingBox {
            private int x;
            private int y;
            private int width;
            private int height;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImageInfo {
        private int originalWidth;
        private int originalHeight;
        private int processedWidth;
        private int processedHeight;
    }
}