package Licenta.Licenta.Dto;

public class SegmentationResult {
    private String overlayImageBase64;
    private String contourImageBase64;
    private TumorDimensions dimensions;
    private BoundingBox boundingBox;
    private int tumorAreaPixels;
    private double tumorPercentage;

    public String getOverlayImageBase64() { return overlayImageBase64; }
    public void setOverlayImageBase64(String overlayImageBase64) { this.overlayImageBase64 = overlayImageBase64; }

    public String getContourImageBase64() { return contourImageBase64; }
    public void setContourImageBase64(String contourImageBase64) { this.contourImageBase64 = contourImageBase64; }

    public TumorDimensions getDimensions() { return dimensions; }
    public void setDimensions(TumorDimensions dimensions) { this.dimensions = dimensions; }

    public BoundingBox getBoundingBox() { return boundingBox; }
    public void setBoundingBox(BoundingBox boundingBox) { this.boundingBox = boundingBox; }

    public int getTumorAreaPixels() { return tumorAreaPixels; }
    public void setTumorAreaPixels(int tumorAreaPixels) { this.tumorAreaPixels = tumorAreaPixels; }

    public double getTumorPercentage() { return tumorPercentage; }
    public void setTumorPercentage(double tumorPercentage) { this.tumorPercentage = tumorPercentage; }
}
