package Licenta.Licenta.Service;

import Licenta.Licenta.Dto.ImagineDto;
import Licenta.Licenta.Model.Imagine;
import Licenta.Licenta.Repository.ImagineRepository;
import Licenta.Licenta.Repository.PacientRepository;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@AllArgsConstructor
public class ImagineService {
    @Autowired
    private ImagineRepository imagineRepository;

    @Autowired
    private PacientRepository pacientRepository;

    @Autowired
    private Cloudinary cloudinary;

    public List<Imagine> getAllImaginiByPacientId(String pacientId) {
        return imagineRepository.findByPacientId(pacientId);
    }

    public Optional<Imagine> findByPacientIdAndId(String pacientId, String imagineId) {
        return imagineRepository.findByPacientIdAndId(pacientId, imagineId);
    }

    public Imagine saveImagine(Imagine imagine) {
        return imagineRepository.save(imagine);
    }

    public void deleteImagine(Imagine imagine) {
        imagineRepository.delete(imagine);
    }

    public Optional<Imagine> findById(String id) {
        return imagineRepository.findById(id);
    }

    /**
     * Salvează o imagine adnotată: șterge imaginea veche de pe Cloudinary,
     * uploadează noua versiune adnotată și actualizează înregistrarea din DB.
     *
     * @param imageId     ID-ul imaginii din MongoDB
     * @param base64Image imaginea adnotată în format base64 (cu sau fără prefix data URI)
     * @return ImagineDto cu datele actualizate
     */
    public ImagineDto saveAnnotatedImage(String imageId, String base64Image) throws IOException {
        Imagine imagine = imagineRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("Imaginea nu a fost găsită: " + imageId));

        // Șterge imaginea veche de pe Cloudinary (ignoră eroarea dacă nu există)
        if (imagine.getCloudinaryPublicId() != null && !imagine.getCloudinaryPublicId().isEmpty()) {
            try {
                cloudinary.uploader().destroy(imagine.getCloudinaryPublicId(), ObjectUtils.emptyMap());
            } catch (Exception e) {
                System.err.println("Avertisment: nu s-a putut șterge imaginea veche de pe Cloudinary: " + e.getMessage());
            }
        }

        // Elimină prefixul data URI dacă există (ex: "data:image/jpeg;base64,")
        String base64Data = base64Image;
        if (base64Data.contains(",")) {
            base64Data = base64Data.substring(base64Data.indexOf(',') + 1);
        }

        byte[] imageBytes = Base64.getDecoder().decode(base64Data);

        // Uploadează noua imagine adnotată pe Cloudinary folosind temp file
        // (unele versiuni Cloudinary SDK nu acceptă byte[] direct)
        String publicId = (imagine.getNume() != null ? imagine.getNume() : imageId) + "_annotated";
        java.io.File tempFile = java.io.File.createTempFile("annotated_", ".png");
        try {
            java.nio.file.Files.write(tempFile.toPath(), imageBytes);
            Map uploadResult = cloudinary.uploader().upload(tempFile,
                    ObjectUtils.asMap("public_id", publicId, "overwrite", true, "resource_type", "image"));

            // Actualizează înregistrarea
            imagine.setImageUrl((String) uploadResult.get("secure_url"));
            imagine.setCloudinaryPublicId((String) uploadResult.get("public_id"));
            imagine.setIsDicom(false); // Acum e PNG, nu mai e DICOM
        } finally {
            tempFile.delete();
        }
        imagine.setDataModificare(new Date());

        Imagine saved = imagineRepository.save(imagine);
        return toDto(saved);
    }

    // Conversie internă Imagine → ImagineDto
    private ImagineDto toDto(Imagine img) {
        return new ImagineDto(
                img.getId(),
                img.getPacientId(),
                img.getImageUrl(),
                img.getNume(),
                img.getTip(),
                img.getCloudinaryPublicId(),
                img.getAreTumoare(),
                img.getTipTumoare(),
                img.getConfidenta(),
                img.getDataAnalizei(),
                img.getStatusAnaliza(),
                img.getObservatii(),
                img.getDataIncarcare(),
                img.getDataModificare(),
                img.getIsDicom(),
                img.getDicomMetadata(),
                img.getSeriesId(),
                img.getImagineId(),
                img.getImagineUrl(),
                img.getImagineNume(),
                img.getImagineTip(),
                img.getImagineDataIncarcare(),
                img.getImagineMetadata()
        );
    }
}
