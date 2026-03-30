package luutronghieu.doan.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TravelPredictionResult {
    private String status;
    private List<PersonPrediction> predictions;
    private List<String> modelNames;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PersonPrediction {
        private BoundingBoxData boundingBox;
        private List<String> detectedItems;
        private TravelStatus travelStatus;
        private double confidence;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BoundingBoxData {
        private double x;
        private double y;
        private double width;
        private double height;
    }

    public enum TravelStatus {
        DI_XA, // Đi xa
        KHONG_DI_XA // Không đi xa
    }
}