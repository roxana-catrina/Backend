package Licenta.Licenta.Controller;

import Licenta.Licenta.Service.BrainTumorPredictionService;
import Licenta.Licenta.Service.PredictionResult;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

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
            System.out.println("=== BrainTumorController.predictTumor ===");
            System.out.println("File received: " + (file != null ? file.getOriginalFilename() : "null"));
            System.out.println("File size: " + (file != null ? file.getSize() : 0));
            System.out.println("Content type: " + (file != null ? file.getContentType() : "null"));

            // Validate file - check size instead of isEmpty() to avoid consuming the stream
            if (file == null || file.getSize() == 0) {
                String error = "Please select a file to upload";
                System.out.println("ERROR: " + error);
                return ResponseEntity.badRequest()
                        .body(new ErrorResponse(error));
            }


            // Check file type - accept both images and DICOM files
            String contentType = file.getContentType();
            String fileName = file.getOriginalFilename();

            // Check if it's a DICOM file by extension
            boolean isDicom = fileName != null && fileName.toLowerCase().endsWith(".dcm");

            // Check if it's an image by content type OR file extension
            boolean isImageByContentType = contentType != null && contentType.startsWith("image/");
            boolean isImageByExtension = fileName != null &&
                (fileName.toLowerCase().endsWith(".jpg") ||
                 fileName.toLowerCase().endsWith(".jpeg") ||
                 fileName.toLowerCase().endsWith(".png") ||
                 fileName.toLowerCase().endsWith(".gif") ||
                 fileName.toLowerCase().endsWith(".bmp"));

            boolean isImage = isImageByContentType || isImageByExtension;

            System.out.println("Is DICOM: " + isDicom);
            System.out.println("Is Image (by content type): " + isImageByContentType);
            System.out.println("Is Image (by extension): " + isImageByExtension);
            System.out.println("Is Image (final): " + isImage);

            if (!isImage && !isDicom) {
                String error = "Please upload an image file (JPG, PNG) or DICOM file (.dcm). Received: " + contentType + ", filename: " + fileName;
                System.out.println("ERROR: " + error);
                return ResponseEntity.badRequest()
                        .body(new ErrorResponse(error));
            }

            System.out.println("Sending to ML service...");
            // Get prediction from ML service
            PredictionResult result = predictionService.predictTumor(file);
            System.out.println("ML service response: success=" + result.isSuccess());

            if (result.isSuccess()) {
                System.out.println("Prediction successful, returning result");
                return ResponseEntity.ok(result);
            } else {
                System.out.println("ERROR from ML service: " + result.getError());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new ErrorResponse(result.getError()));
            }

        } catch (IOException e) {
            System.err.println("IOException: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error processing image: " + e.getMessage()));
        } catch (org.springframework.web.client.HttpServerErrorException e) {
            System.err.println("HTTP Server Error from ML service: " + e.getMessage());
            System.err.println("Response body: " + e.getResponseBodyAsString());

            // Try to parse error from Python service
            String errorMsg = "ML service error";
            try {
                String responseBody = e.getResponseBodyAsString();
                if (responseBody.contains("cannot identify image file")) {
                    errorMsg = "The uploaded file could not be identified as a valid image. Please ensure you're uploading a JPG, PNG, or DICOM file.";
                } else if (responseBody.contains("error")) {
                    // Try to extract error message
                    int errorStart = responseBody.indexOf("\"error\":\"");
                    if (errorStart != -1) {
                        errorStart += 9;
                        int errorEnd = responseBody.indexOf("\"", errorStart);
                        if (errorEnd != -1) {
                            errorMsg = responseBody.substring(errorStart, errorEnd);
                        }
                    }
                }
            } catch (Exception parseEx) {
                System.err.println("Error parsing ML service response: " + parseEx.getMessage());
            }

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(errorMsg));
        } catch (Exception e) {
            System.err.println("Exception: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Unexpected error: " + e.getMessage()));
        }
    }

    /**
     * Predict tumor from Cloudinary URL
     * This endpoint downloads the image from Cloudinary and sends it to the ML service
     */
    @PostMapping("/predict-from-url")
    public ResponseEntity<?> predictFromUrl(@RequestBody Map<String, String> request) {
        try {
            String imageUrl = request.get("imageUrl");

            if (imageUrl == null || imageUrl.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ErrorResponse("Image URL is required"));
            }

            System.out.println("=== BrainTumorController.predictFromUrl ===");
            System.out.println("Image URL: " + imageUrl);

            // Validate that it's a proper Cloudinary file URL (not a management page)
            if (imageUrl.contains("summary?") || imageUrl.contains("view_mode") || imageUrl.contains("context=manage")) {
                return ResponseEntity.badRequest()
                        .body(new ErrorResponse("Invalid Cloudinary URL. Please use the direct file URL (should contain /upload/ or /raw/upload/)"));
            }

            // Download image from Cloudinary
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<byte[]> response = restTemplate.getForEntity(imageUrl, byte[].class);
            byte[] imageBytes = response.getBody();

            if (imageBytes == null || imageBytes.length == 0) {
                return ResponseEntity.badRequest()
                        .body(new ErrorResponse("Failed to download image from URL"));
            }

            System.out.println("Downloaded " + imageBytes.length + " bytes from Cloudinary");
            System.out.println("First 32 bytes from download (hex): " + bytesToHex(imageBytes, 32));

            // Check if we got HTML instead of an image (Cloudinary error page)
            if (imageBytes.length < 100000 && new String(imageBytes, 0, Math.min(100, imageBytes.length)).contains("<!DOCTYPE") ||
                new String(imageBytes, 0, Math.min(100, imageBytes.length)).contains("<!--")) {
                return ResponseEntity.badRequest()
                        .body(new ErrorResponse("Received HTML page instead of image file. Please check the Cloudinary URL. Make sure it's a direct file URL, not a management page URL."));
            }

            // Determine file name and content type from URL
            String fileName = imageUrl.substring(imageUrl.lastIndexOf('/') + 1);

            // Remove query parameters if present
            if (fileName.contains("?")) {
                fileName = fileName.substring(0, fileName.indexOf("?"));
            }

            String contentType = "application/octet-stream";

            if (fileName.toLowerCase().endsWith(".dcm")) {
                contentType = "application/dicom";
            } else if (fileName.toLowerCase().endsWith(".jpg") || fileName.toLowerCase().endsWith(".jpeg")) {
                contentType = "image/jpeg";
            } else if (fileName.toLowerCase().endsWith(".png")) {
                contentType = "image/png";
            }

            System.out.println("File name: " + fileName + ", Content type: " + contentType);

            // Create a COPY of the byte array to avoid reference issues
            byte[] fileBytesCopy = new byte[imageBytes.length];
            System.arraycopy(imageBytes, 0, fileBytesCopy, 0, imageBytes.length);

            System.out.println("Created byte copy, first 32 bytes (hex): " + bytesToHex(fileBytesCopy, 32));

            // Create MultipartFile from copied bytes
            MultipartFile file = new MockMultipartFile(
                "file",
                fileName,
                contentType,
                fileBytesCopy  // Use the copy
            );

            System.out.println("Created MultipartFile with size: " + file.getSize());
            System.out.println("Sending to ML service...");

            // Send to prediction service
            PredictionResult result = predictionService.predictTumor(file);

            if (result.isSuccess()) {
                System.out.println("Prediction successful from URL");
                return ResponseEntity.ok(result);
            } else {
                System.out.println("Prediction failed: " + result.getError());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new ErrorResponse(result.getError()));
            }

        } catch (IOException e) {
            System.err.println("IOException in predictFromUrl: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error processing image from URL: " + e.getMessage()));
        } catch (Exception e) {
            System.err.println("Exception in predictFromUrl: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Unexpected error: " + e.getMessage()));
        }
    }

    // Helper method to debug byte content
    private String bytesToHex(byte[] bytes, int limit) {
        StringBuilder sb = new StringBuilder();
        int count = Math.min(bytes.length, limit);
        for (int i = 0; i < count; i++) {
            sb.append(String.format("%02x", bytes[i]));
        }
        return sb.toString();
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

