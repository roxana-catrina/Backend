package Licenta.Licenta.Service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.core.io.ByteArrayResource;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

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

        // Create multipart request
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
                return file.getOriginalFilename();
            }
        });

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

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

            JsonNode probabilities = root.get("probabilities");
            result.setNoTumorProbability(probabilities.get("no_tumor").asDouble());
            result.setTumorProbability(probabilities.get("tumor").asDouble());
        } else {
            result.setError(root.get("error").asText());
        }

        return result;
    }
}

