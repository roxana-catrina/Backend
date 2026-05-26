package Licenta.Licenta.Service;

import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Utilitar pentru conversia fișierelor DICOM în format PNG.
 * Implementare simplificată fără dependențe externe (dcm4che).
 * Parsează manual structura DICOM pentru a extrage pixel data.
 */
@Component
public class DicomConverter {

    /**
     * Verifică dacă un array de bytes este un fișier DICOM valid.
     * DICOM are un preamble de 128 bytes urmat de "DICM" la offset 128.
     */
    public boolean isDicomFile(byte[] fileBytes, String fileName) {
        if (fileName != null && fileName.toLowerCase().endsWith(".dcm")) {
            return true;
        }

        if (fileBytes != null && fileBytes.length > 132) {
            return fileBytes[128] == 'D' &&
                   fileBytes[129] == 'I' &&
                   fileBytes[130] == 'C' &&
                   fileBytes[131] == 'M';
        }

        return false;
    }

    /**
     * Convertește un fișier DICOM (bytes) în PNG (bytes).
     * Parsează manual structura DICOM pentru a extrage dimensiunile și pixel data.
     */
    public byte[] convertDicomToPng(byte[] dicomBytes) throws IOException {
        System.out.println("🔄 Conversie DICOM → PNG (" + dicomBytes.length + " bytes)");

        // Mai întâi încearcă cu ImageIO (funcționează dacă un plugin DICOM e pe classpath)
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(dicomBytes);
            BufferedImage image = ImageIO.read(bais);
            if (image != null) {
                System.out.println("✅ ImageIO a citit DICOM-ul direct");
                return bufferedImageToPng(image);
            }
        } catch (Exception e) {
            System.out.println("⚠️ ImageIO nu poate citi DICOM: " + e.getMessage());
        }

        // Fallback: parsare manuală DICOM
        return parseDicomManual(dicomBytes);
    }

    /**
     * Parsare manuală a structurii DICOM.
     * Caută tag-urile Rows, Columns, BitsAllocated și PixelData.
     */
    private byte[] parseDicomManual(byte[] data) throws IOException {
        // Skip preamble (128 bytes) + "DICM" (4 bytes)
        int offset = 132;

        int rows = 0;
        int cols = 0;
        int bitsAllocated = 16;
        int pixelRepresentation = 0; // 0 = unsigned, 1 = signed
        int samplesPerPixel = 1;
        int pixelDataOffset = -1;
        int pixelDataLength = 0;
        double windowCenter = 0;
        double windowWidth = 0;
        double rescaleSlope = 1;
        double rescaleIntercept = 0;

        while (offset < data.length - 8) {
            int group = getUShort(data, offset);
            int element = getUShort(data, offset + 2);
            offset += 4;

            // Determine VR and length
            int length;
            if (isExplicitVR(data, offset)) {
                String vr = new String(data, offset, 2);
                offset += 2;

                if (vr.equals("OB") || vr.equals("OW") || vr.equals("OF") ||
                    vr.equals("SQ") || vr.equals("UC") || vr.equals("UN") ||
                    vr.equals("UR") || vr.equals("UT")) {
                    offset += 2; // skip reserved
                    length = getInt(data, offset);
                    offset += 4;
                } else {
                    length = getUShort(data, offset);
                    offset += 2;
                }
            } else {
                // Implicit VR
                length = getInt(data, offset);
                offset += 4;
            }

            // Handle undefined length
            if (length == -1 || length == 0xFFFFFFFF) {
                // For pixel data with undefined length, take remaining bytes
                if (group == 0x7FE0 && element == 0x0010) {
                    pixelDataOffset = offset;
                    pixelDataLength = data.length - offset;
                }
                break;
            }

            if (length < 0 || offset + length > data.length) {
                break;
            }

            // Extract relevant tags
            if (group == 0x0028) {
                switch (element) {
                    case 0x0010: // Rows
                        rows = getUShort(data, offset);
                        break;
                    case 0x0011: // Columns
                        cols = getUShort(data, offset);
                        break;
                    case 0x0100: // BitsAllocated
                        bitsAllocated = getUShort(data, offset);
                        break;
                    case 0x0103: // PixelRepresentation
                        pixelRepresentation = getUShort(data, offset);
                        break;
                    case 0x0002: // SamplesPerPixel
                        samplesPerPixel = getUShort(data, offset);
                        break;
                    case 0x1050: // WindowCenter
                        try {
                            String wcStr = new String(data, offset, length).trim();
                            if (wcStr.contains("\\")) wcStr = wcStr.split("\\\\")[0];
                            windowCenter = Double.parseDouble(wcStr);
                        } catch (Exception ignored) {}
                        break;
                    case 0x1051: // WindowWidth
                        try {
                            String wwStr = new String(data, offset, length).trim();
                            if (wwStr.contains("\\")) wwStr = wwStr.split("\\\\")[0];
                            windowWidth = Double.parseDouble(wwStr);
                        } catch (Exception ignored) {}
                        break;
                    case 0x1052: // RescaleIntercept
                        try {
                            rescaleIntercept = Double.parseDouble(new String(data, offset, length).trim());
                        } catch (Exception ignored) {}
                        break;
                    case 0x1053: // RescaleSlope
                        try {
                            rescaleSlope = Double.parseDouble(new String(data, offset, length).trim());
                        } catch (Exception ignored) {}
                        break;
                }
            }

            // PixelData tag (7FE0,0010)
            if (group == 0x7FE0 && element == 0x0010) {
                pixelDataOffset = offset;
                pixelDataLength = length;
            }

            offset += length;
        }

        System.out.println("DICOM parsed: " + cols + "x" + rows +
                ", bits=" + bitsAllocated + ", pixelData at offset=" + pixelDataOffset +
                ", length=" + pixelDataLength);

        if (rows == 0 || cols == 0 || pixelDataOffset < 0) {
            throw new IOException("Nu s-au putut extrage dimensiunile sau pixel data din DICOM. " +
                    "rows=" + rows + ", cols=" + cols + ", pixelDataOffset=" + pixelDataOffset);
        }

        // Construiește imaginea
        BufferedImage image = new BufferedImage(cols, rows, BufferedImage.TYPE_INT_RGB);

        if (bitsAllocated == 16) {
            // 16-bit pixels
            int expectedPixels = rows * cols * samplesPerPixel;
            int availablePixels = pixelDataLength / 2;

            // Primul pas: găsește min/max pentru normalizare (sau folosește window/level)
            int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;

            for (int i = 0; i < Math.min(expectedPixels, availablePixels); i++) {
                int idx = pixelDataOffset + i * 2;
                if (idx + 1 >= data.length) break;
                int val;
                if (pixelRepresentation == 1) {
                    val = (short) ((data[idx] & 0xFF) | ((data[idx + 1] & 0xFF) << 8));
                } else {
                    val = (data[idx] & 0xFF) | ((data[idx + 1] & 0xFF) << 8);
                }
                if (val < min) min = val;
                if (val > max) max = val;
            }

            // Folosește window/level dacă e disponibil
            double wMin, wMax;
            if (windowWidth > 0) {
                wMin = windowCenter - windowWidth / 2.0;
                wMax = windowCenter + windowWidth / 2.0;
            } else {
                wMin = min;
                wMax = max;
            }

            double range = wMax - wMin;
            if (range == 0) range = 1;

            // Al doilea pas: construiește imaginea
            int pixelIdx = 0;
            for (int y = 0; y < rows; y++) {
                for (int x = 0; x < cols; x++) {
                    int idx = pixelDataOffset + pixelIdx * 2;
                    if (idx + 1 >= data.length) break;

                    int val;
                    if (pixelRepresentation == 1) {
                        val = (short) ((data[idx] & 0xFF) | ((data[idx + 1] & 0xFF) << 8));
                    } else {
                        val = (data[idx] & 0xFF) | ((data[idx + 1] & 0xFF) << 8);
                    }

                    // Apply rescale
                    double rescaled = val * rescaleSlope + rescaleIntercept;

                    // Apply windowing
                    int normalized = (int) (((rescaled - wMin) / range) * 255.0);
                    normalized = Math.max(0, Math.min(255, normalized));

                    int rgb = (normalized << 16) | (normalized << 8) | normalized;
                    image.setRGB(x, y, rgb);
                    pixelIdx++;
                }
            }
        } else {
            // 8-bit pixels
            int pixelIdx = 0;
            for (int y = 0; y < rows; y++) {
                for (int x = 0; x < cols; x++) {
                    int idx = pixelDataOffset + pixelIdx;
                    if (idx >= data.length) break;
                    int val = data[idx] & 0xFF;
                    int rgb = (val << 16) | (val << 8) | val;
                    image.setRGB(x, y, rgb);
                    pixelIdx++;
                }
            }
        }

        byte[] pngBytes = bufferedImageToPng(image);
        System.out.println("✅ Conversie DICOM → PNG reușită: " + cols + "x" + rows + " → " + pngBytes.length + " bytes");
        return pngBytes;
    }

    private byte[] bufferedImageToPng(BufferedImage image) throws IOException {
        // Asigură-te că e RGB
        if (image.getType() != BufferedImage.TYPE_INT_RGB &&
            image.getType() != BufferedImage.TYPE_3BYTE_BGR) {
            BufferedImage rgbImage = new BufferedImage(
                    image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D g = rgbImage.createGraphics();
            g.drawImage(image, 0, 0, null);
            g.dispose();
            image = rgbImage;
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "PNG", baos);
        return baos.toByteArray();
    }

    private int getUShort(byte[] data, int offset) {
        if (offset + 1 >= data.length) return 0;
        return (data[offset] & 0xFF) | ((data[offset + 1] & 0xFF) << 8);
    }

    private int getInt(byte[] data, int offset) {
        if (offset + 3 >= data.length) return 0;
        return (data[offset] & 0xFF) |
               ((data[offset + 1] & 0xFF) << 8) |
               ((data[offset + 2] & 0xFF) << 16) |
               ((data[offset + 3] & 0xFF) << 24);
    }

    /**
     * Verifică dacă la offset curent avem un VR explicit (2 litere ASCII uppercase).
     */
    private boolean isExplicitVR(byte[] data, int offset) {
        if (offset + 1 >= data.length) return false;
        char c1 = (char) (data[offset] & 0xFF);
        char c2 = (char) (data[offset + 1] & 0xFF);
        return Character.isUpperCase(c1) && Character.isUpperCase(c2);
    }
}
