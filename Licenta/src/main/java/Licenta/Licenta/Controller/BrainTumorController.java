package Licenta.Licenta.Controller;

import Licenta.Licenta.Service.BrainTumorPredictionService;
import Licenta.Licenta.Service.PredictionResult;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * REST Controller for brain tumor detection
 * This controller receives image uploads from Angular frontend and returns predictions
 */
@RestController
@RequestMapping("/api/brain-tumor")
@CrossOrigin(origins = "http://localhost:4200")
public class BrainTumorController {

    @Autowired
    private BrainTumorPredictionService predictionService;

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<HealthResponse> healthCheck() {
        boolean isHealthy = predictionService.healthCheck();
        HealthResponse response = new HealthResponse(isHealthy);
        return ResponseEntity.ok(response);
    }

    /**
     * Predict tumor from uploaded image
     *
     * @param file Brain scan image file
     * @return Prediction result
     */
    @PostMapping("/predict")
    public ResponseEntity<?> predictTumor(@RequestParam("file") MultipartFile file) {
        try {
            // Validate file
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ErrorResponse("Please select a file to upload"));
            }

            // Check file type
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest()
                        .body(new ErrorResponse("Please upload an image file"));
            }

            // Get prediction from ML service
            PredictionResult result = predictionService.predictTumor(file);

            if (result.isSuccess()) {
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new ErrorResponse(result.getError()));
            }

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error processing image: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Unexpected error: " + e.getMessage()));
        }
    }

    /**
     * Response classes
     */
    public static class HealthResponse {
        private boolean healthy;
        private String message;

        public HealthResponse(boolean healthy) {
            this.healthy = healthy;
            this.message = healthy ? "ML service is running" : "ML service is not available";
        }

        public boolean isHealthy() {
            return healthy;
        }

        public String getMessage() {
            return message;
        }
    }

    public static class ErrorResponse {
        private boolean success = false;
        private String error;

        public ErrorResponse(String error) {
            this.error = error;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getError() {
            return error;
        }
    }
}

