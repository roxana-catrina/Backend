package Licenta.Licenta.Service;

import java.util.Map;

public class PredictionResult {
    private boolean success;
    private String prediction;
    private boolean hasTumor;
    private double confidence;
    private double noTumorProbability;
    private double tumorProbability;
    private String error;
    private String type;
    private double tumorTypeConfidence;
    private Map<String, Double> tumorTypeProbabilities;
    private Map<String, Double> rawMulticlassProbabilities;

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getPrediction() {
        return prediction;
    }

    public void setPrediction(String prediction) {
        this.prediction = prediction;
    }

    public boolean isHasTumor() {
        return hasTumor;
    }

    public void setHasTumor(boolean hasTumor) {
        this.hasTumor = hasTumor;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public double getNoTumorProbability() {
        return noTumorProbability;
    }

    public void setNoTumorProbability(double noTumorProbability) {
        this.noTumorProbability = noTumorProbability;
    }

    public double getTumorProbability() {
        return tumorProbability;
    }

    public void setTumorProbability(double tumorProbability) {
        this.tumorProbability = tumorProbability;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public void setType(String type) {
        this.type= type;
    }

    public String getType() {
        return type;
    }

    public double getTumorTypeConfidence() {
        return tumorTypeConfidence;
    }

    public void setTumorTypeConfidence(double tumorTypeConfidence) {
        this.tumorTypeConfidence = tumorTypeConfidence;
    }

    public Map<String, Double> getTumorTypeProbabilities() {
        return tumorTypeProbabilities;
    }

    public void setTumorTypeProbabilities(Map<String, Double> tumorTypeProbabilities) {
        this.tumorTypeProbabilities = tumorTypeProbabilities;
    }

    public Map<String, Double> getRawMulticlassProbabilities() {
        return rawMulticlassProbabilities;
    }

    public void setRawMulticlassProbabilities(Map<String, Double> rawMulticlassProbabilities) {
        this.rawMulticlassProbabilities = rawMulticlassProbabilities;
    }

    @Override
    public String toString() {
        if (success) {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("Prediction: %s (%.2f%% confidence)\n",
                    prediction, confidence * 100));

            if (hasTumor && type != null) {
                sb.append(String.format("Tumor Type: %s (%.2f%% confidence)\n",
                        type, tumorTypeConfidence * 100));

                if (tumorTypeProbabilities != null) {
                    sb.append("Tumor Type Probabilities:\n");
                    tumorTypeProbabilities.forEach((key, value) ->
                        sb.append(String.format("  %s: %.2f%%\n", key, value * 100)));
                }

                if (rawMulticlassProbabilities != null) {
                    sb.append("Raw Multiclass Probabilities:\n");
                    rawMulticlassProbabilities.forEach((key, value) ->
                        sb.append(String.format("  %s: %.2f%%\n", key, value * 100)));
                }
            }

            return sb.toString();
        } else {
            return "Error: " + error;
        }
    }
}

