package Licenta.Licenta.Service;

import org.dcm4che3.imageio.plugins.dcm.DicomImageReadParam;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;

/**
 * Utilitar pentru conversia fișierelor DICOM în format PNG folosind dcm4che.
 */
@Component
public class DicomConverter {

    /**
     * Verifică dacă un array de bytes este un fișier DICOM valid.
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
     * Convertește un fișier DICOM (bytes) în PNG (bytes) folosind dcm4che.
     */
    public byte[] convertDicomToPng(byte[] dicomBytes) throws IOException {
        System.out.println("🔄 Conversie DICOM → PNG cu dcm4che (" + dicomBytes.length + " bytes)");

        BufferedImage image = null;

        // Metoda 1: dcm4che ImageReader (cea mai robustă)
        try {
            image = readWithDcm4cheReader(dicomBytes);
        } catch (Exception e) {
            System.err.println("⚠️ dcm4che ImageReader a eșuat: " + e.getMessage());
        }

        // Metoda 2: Fallback cu ImageIO generic
        if (image == null) {
            try {
                ByteArrayInputStream bais = new ByteArrayInputStream(dicomBytes);
                image = ImageIO.read(bais);
            } catch (Exception e) {
                System.err.println("⚠️ ImageIO generic a eșuat: " + e.getMessage());
            }
        }

        if (image == null) {
            throw new IOException("Nu s-a putut converti DICOM-ul în imagine. " +
                    "Fișierul poate fi corupt sau într-un format DICOM nesuportat.");
        }

        // Convertește la RGB dacă e necesar
        BufferedImage rgbImage = ensureRgb(image);

        // Scrie ca PNG
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(rgbImage, "PNG", baos);
        byte[] pngBytes = baos.toByteArray();

        System.out.println("✅ Conversie DICOM → PNG reușită: " +
                rgbImage.getWidth() + "x" + rgbImage.getHeight() + " → " + pngBytes.length + " bytes");

        return pngBytes;
    }

    /**
     * Citește imaginea DICOM folosind dcm4che ImageReader plugin.
     */
    private BufferedImage readWithDcm4cheReader(byte[] dicomBytes) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(dicomBytes);
        ImageInputStream iis = ImageIO.createImageInputStream(bais);

        Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName("DICOM");
        if (!readers.hasNext()) {
            throw new IOException("Nu s-a găsit ImageReader pentru format DICOM. " +
                    "Verifică că dcm4che-imageio e pe classpath.");
        }

        ImageReader reader = readers.next();
        try {
            reader.setInput(iis);
            DicomImageReadParam param = (DicomImageReadParam) reader.getDefaultReadParam();
            BufferedImage image = reader.read(0, param);
            System.out.println("✅ dcm4che a citit DICOM: " +
                    image.getWidth() + "x" + image.getHeight());
            return image;
        } finally {
            reader.dispose();
            iis.close();
        }
    }

    /**
     * Asigură că imaginea e în format RGB (compatibil PNG).
     */
    private BufferedImage ensureRgb(BufferedImage source) {
        if (source.getType() == BufferedImage.TYPE_INT_RGB ||
            source.getType() == BufferedImage.TYPE_3BYTE_BGR) {
            return source;
        }

        BufferedImage rgbImage = new BufferedImage(
                source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = rgbImage.createGraphics();
        g.drawImage(source, 0, 0, null);
        g.dispose();
        return rgbImage;
    }
}
