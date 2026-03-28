package luutronghieu.doan.controller;

import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.ModelException;
import ai.djl.translate.TranslateException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import luutronghieu.doan.dto.DetectionResult;
import luutronghieu.doan.service.InferenceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/vision")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Vision API", description = "APIs for computer vision and object detection")
public class VisionController {

    private final InferenceService inferenceService;

    @PostMapping(value = "/detect-people", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
        summary = "Detect only people in image",
        description = "Upload an image and detect only people"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Detection successful",
            content = @Content(schema = @Schema(implementation = DetectionResult.class))),
        @ApiResponse(responseCode = "400", description = "Invalid image file"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<DetectionResult> detectPeople(
            @Parameter(description = "Image file to upload", required = true)
            @RequestParam("file") MultipartFile file) {
        
        log.info("Received image upload request: {} ({} bytes)", 
                file.getOriginalFilename(), file.getSize());
        
        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(createErrorResult("File is empty"));
        }
        
        if (!isValidImageFile(file)) {
            return ResponseEntity.badRequest()
                    .body(createErrorResult("Invalid image file format. Supported: JPEG, PNG, BMP"));
        }
        
        try {
            if (!inferenceService.isModelLoaded()) {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body(createErrorResult("AI model is not loaded yet. Please try again later."));
            }
            
            byte[] imageBytes = file.getBytes();
            DetectedObjects detections = inferenceService.runInference(imageBytes);
            DetectionResult result = inferenceService.convertToDetectionResult(detections, imageBytes);
            
            log.info("Detection completed. Found {} people.", result.getDetections().size());
            
            return ResponseEntity.ok(result);
            
        } catch (ModelException | TranslateException e) {
            log.error("Model inference error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResult("Model inference failed: " + e.getMessage()));
        } catch (IOException e) {
            log.error("File processing error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResult("File processing failed: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResult("Unexpected error: " + e.getMessage()));
        }
    }

    @PostMapping(value = "/test-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
        summary = "Legacy endpoint - detect objects",
        description = "Upload an image file and get YOLOv8 object detection results"
    )
    public ResponseEntity<DetectionResult> testImageDetection(
            @Parameter(description = "Image file to upload", required = true)
            @RequestParam("file") MultipartFile file) {
        return detectPeople(file); // Forward to same logic
    }

    @GetMapping("/health")
    @Operation(summary = "Check vision service health")
    public ResponseEntity<DetectionResult> healthCheck() {
        DetectionResult result = new DetectionResult();
        result.setStatus(inferenceService.isModelLoaded() ? "healthy" : "loading");
        result.setMessage(inferenceService.isModelLoaded() ? 
                "YOLOv8 model is loaded and ready (PyTorch engine)" : 
                "Model is still loading or failed to load");
        result.setDetections(List.of());
        result.setInferenceTimeMs(0);
        
        return ResponseEntity.ok(result);
    }

    private boolean isValidImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        String fileName = file.getOriginalFilename();
        
        if (contentType == null && fileName != null) {
            return fileName.toLowerCase().matches(".*\\.(jpg|jpeg|png|bmp|gif)$");
        }
        
        return contentType != null && 
               (contentType.startsWith("image/jpeg") || 
                contentType.startsWith("image/png") || 
                contentType.startsWith("image/bmp") ||
                contentType.startsWith("image/gif"));
    }

    private DetectionResult createErrorResult(String message) {
        DetectionResult result = new DetectionResult();
        result.setStatus("error");
        result.setMessage(message);
        result.setDetections(List.of());
        result.setInferenceTimeMs(0);
        return result;
    }
}