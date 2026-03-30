package luutronghieu.doan.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClassificationResult {
    private String status;
    private String message;
    private List<Classification> classifications;
    private long inferenceTimeMs;
    private String modelName;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Classification {
        private String className;
        private double probability;
    }
}