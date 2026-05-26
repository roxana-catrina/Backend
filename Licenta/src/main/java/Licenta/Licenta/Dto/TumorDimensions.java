package Licenta.Licenta.Dto;

public class TumorDimensions {
    private int widthPixels;
    private int heightPixels;
    private double widthMm;
    private double heightMm;
    private int areaPixels;
    private double areaMm2;
    private double tumorPercentage;
    private double pixelSpacingMm;

    public int getWidthPixels() { return widthPixels; }
    public void setWidthPixels(int widthPixels) { this.widthPixels = widthPixels; }

    public int getHeightPixels() { return heightPixels; }
    public void setHeightPixels(int heightPixels) { this.heightPixels = heightPixels; }

    public double getWidthMm() { return widthMm; }
    public void setWidthMm(double widthMm) { this.widthMm = widthMm; }

    public double getHeightMm() { return heightMm; }
    public void setHeightMm(double heightMm) { this.heightMm = heightMm; }

    public int getAreaPixels() { return areaPixels; }
    public void setAreaPixels(int areaPixels) { this.areaPixels = areaPixels; }

    public double getAreaMm2() { return areaMm2; }
    public void setAreaMm2(double areaMm2) { this.areaMm2 = areaMm2; }

    public double getTumorPercentage() { return tumorPercentage; }
    public void setTumorPercentage(double tumorPercentage) { this.tumorPercentage = tumorPercentage; }

    public double getPixelSpacingMm() { return pixelSpacingMm; }
    public void setPixelSpacingMm(double pixelSpacingMm) { this.pixelSpacingMm = pixelSpacingMm; }
}
