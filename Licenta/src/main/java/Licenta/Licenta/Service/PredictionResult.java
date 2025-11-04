package Licenta.Licenta.Service;

public class PredictionResult {
    private boolean success;
    private String prediction;
    private boolean hasTumor;
    private double confidence;
    private double noTumorProbability;
    private double tumorProbability;
    private String error;

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

    @Override
    public String toString() {
        if (success) {
            return String.format("Prediction: %s (%.2f%% confidence)",
                    prediction, confidence * 100);
        } else {
            return "Error: " + error;
        }
    }
}

