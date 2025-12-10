package Licenta.Licenta.Service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for communicating with the Python ML model API
 * This service sends brain scan images to the Flask server and receives tumor predictions
 */
@Service
public class BrainTumorPredictionService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String pythonApiUrl;

    public BrainTumorPredictionService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
        // Configure your Python API URL here
        this.pythonApiUrl = "http://localhost:5000/api";
    }

    /**
     * Predict tumor presence in a brain scan image using multipart file upload
     *
     * @param file The brain scan image file
     * @return PredictionResult containing the prediction details
     * @throws IOException if there's an error processing the image
     */
    public PredictionResult predictTumor(MultipartFile file) throws IOException {
        String url = pythonApiUrl + "/predict";

        System.out.println("=== BrainTumorPredictionService.predictTumor ===");
        System.out.println("Sending to URL: " + url);
        System.out.println("Original filename: " + file.getOriginalFilename());
        System.out.println("File size: " + file.getSize());
        System.out.println("Content type: " + file.getContentType());

        // Read bytes ONCE and store them
        byte[] fileBytes = file.getBytes();
        System.out.println("Bytes read: " + fileBytes.length);
        System.out.println("First 32 bytes (hex): " + bytesToHex(fileBytes, 32));

        // Check if it's a valid DICOM file (DICOM has 128-byte preamble of zeros, then "DICM")
        if (fileBytes.length > 132) {
            String dicmSignature = new String(fileBytes, 128, 4);
            System.out.println("Bytes 128-131 (DICOM signature): " + dicmSignature);
            if ("DICM".equals(dicmSignature)) {
                System.out.println("✅ Valid DICOM file detected!");
            }
        }

        // Verify bytes are not all zeros by checking a sample
        boolean hasNonZeroBytes = false;
        for (int i = 0; i < Math.min(fileBytes.length, 1000); i++) {
            if (fileBytes[i] != 0) {
                hasNonZeroBytes = true;
                System.out.println("First non-zero byte at position: " + i + " value: " + String.format("%02x", fileBytes[i]));
                break;
            }
        }
        if (!hasNonZeroBytes && fileBytes.length > 1000) {
            // Check further in the file
            for (int i = 1000; i < fileBytes.length; i += 1000) {
                if (fileBytes[i] != 0) {
                    hasNonZeroBytes = true;
                    System.out.println("First non-zero byte at position: " + i + " value: " + String.format("%02x", fileBytes[i]));
                    break;
                }
            }
        }
        System.out.println("File has non-zero content: " + hasNonZeroBytes);

        // Detect if it's a valid DICOM file
        boolean isValidDicom = false;
        if (fileBytes.length > 132) {
            String dicmSignature = new String(fileBytes, 128, 4);
            isValidDicom = "DICM".equals(dicmSignature);
        }

        // Create multipart request
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        // Add custom header to indicate valid DICOM
        if (isValidDicom) {
            headers.add("X-Is-Valid-DICOM", "true");
        }

        // Determine content type and filename
        String contentType = file.getContentType();
        String originalFilename = file.getOriginalFilename();
        String filenameToSend = originalFilename;

        if (contentType == null || contentType.equals("application/octet-stream")) {
            // Try to determine from filename or DICOM signature
            if (isValidDicom) {
                contentType = "application/dicom";
                // Ensure filename has .dcm extension for Python to recognize
                if (filenameToSend == null || !filenameToSend.toLowerCase().endsWith(".dcm")) {
                    filenameToSend = (filenameToSend != null ? filenameToSend : "image") + ".dcm";
                }
            } else if (originalFilename != null) {
                if (originalFilename.toLowerCase().endsWith(".jpg") || originalFilename.toLowerCase().endsWith(".jpeg")) {
                    contentType = "image/jpeg";
                } else if (originalFilename.toLowerCase().endsWith(".png")) {
                    contentType = "image/png";
                } else if (originalFilename.toLowerCase().endsWith(".dcm")) {
                    contentType = "application/dicom";
                }
            }
        }

        final String finalContentType = contentType;
        final String finalFilename = filenameToSend;
        System.out.println("Using content type: " + finalContentType);
        System.out.println("Using filename: " + finalFilename);

        // Write bytes to a temporary file for reliable multipart transfer
        File tempFile = File.createTempFile("upload-", finalFilename != null ? "-" + finalFilename : ".tmp");
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(fileBytes);
            fos.flush();
        }

        System.out.println("Created temp file: " + tempFile.getAbsolutePath() + " with size: " + tempFile.length());

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

        // Use FileSystemResource which properly handles file transfer
        FileSystemResource fileResource = new FileSystemResource(tempFile) {
            @Override
            public String getFilename() {
                return finalFilename != null ? finalFilename : super.getFilename();
            }
        };

        body.add("file", fileResource);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        System.out.println("Sending request to Python ML service...");

        try {
            // Send request
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            System.out.println("Received response: " + response.getStatusCode());
            // Parse response
            return parseResponse(response.getBody());
        } finally {
            // Clean up temp file
            if (tempFile.exists()) {
                tempFile.delete();
            }
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
     * Predict tumor presence using base64 encoded image
     *
     * @param imageBytes The brain scan image as byte array
     * @return PredictionResult containing the prediction details
     * @throws IOException if there's an error processing the response
     */
    public PredictionResult predictTumorBase64(byte[] imageBytes) throws IOException {
        String url = pythonApiUrl + "/predict";

        // Encode image to base64
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);

        // Create JSON request
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("image", base64Image);

        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);

        // Send request
        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                requestEntity,
                String.class
        );

        // Parse response
        return parseResponse(response.getBody());
    }

    /**
     * Check if the Python API server is healthy
     *
     * @return true if the server is healthy, false otherwise
     */
    public boolean healthCheck() {
        try {
            String url = pythonApiUrl.replace("/api", "") + "/health";
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode root = objectMapper.readTree(response.getBody());
                return root.get("status").asText().equals("healthy")
                        && root.get("model_loaded").asBoolean();
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Parse the JSON response from the Python API
     */
    private PredictionResult parseResponse(String jsonResponse) throws IOException {
        JsonNode root = objectMapper.readTree(jsonResponse);

        PredictionResult result = new PredictionResult();
        result.setSuccess(root.get("success").asBoolean());

        if (result.isSuccess()) {
            result.setPrediction(root.get("prediction").asText());
            result.setHasTumor(root.get("has_tumor").asBoolean());
            result.setConfidence(root.get("confidence").asDouble());

            // Parse tumor type information
            if (root.has("tumor_type")) {
                result.setType(root.get("tumor_type").asText());
            }

            if (root.has("tumor_type_confidence")) {
                result.setTumorTypeConfidence(root.get("tumor_type_confidence").asDouble());
            }

            // Parse basic probabilities
            if (root.has("probabilities")) {
                JsonNode probabilities = root.get("probabilities");
                result.setNoTumorProbability(probabilities.get("no_tumor").asDouble());
                result.setTumorProbability(probabilities.get("tumor").asDouble());
            }

            // Parse tumor type probabilities
            if (root.has("tumor_type_probabilities")) {
                JsonNode tumorTypeProbs = root.get("tumor_type_probabilities");
                Map<String, Double> tumorTypeProbMap = new HashMap<>();
                tumorTypeProbs.fields().forEachRemaining(entry ->
                    tumorTypeProbMap.put(entry.getKey(), entry.getValue().asDouble())
                );
                result.setTumorTypeProbabilities(tumorTypeProbMap);
            }

            // Parse raw multiclass probabilities
            if (root.has("raw_multiclass_probabilities")) {
                JsonNode rawProbs = root.get("raw_multiclass_probabilities");
                Map<String, Double> rawProbMap = new HashMap<>();
                rawProbs.fields().forEachRemaining(entry ->
                    rawProbMap.put(entry.getKey(), entry.getValue().asDouble())
                );
                result.setRawMulticlassProbabilities(rawProbMap);
            }

            System.out.println(result);
        } else {
            result.setError(root.get("error").asText());
        }

        return result;
    }
}

