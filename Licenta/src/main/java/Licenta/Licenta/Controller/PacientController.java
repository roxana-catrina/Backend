package Licenta.Licenta.Controller;

import Licenta.Licenta.Dto.ImagineDto;
import Licenta.Licenta.Dto.PacientDto;
import Licenta.Licenta.Model.Imagine;
import Licenta.Licenta.Model.Pacient;
import Licenta.Licenta.Model.Sex;
import Licenta.Licenta.Model.User;
import Licenta.Licenta.Repository.UserRepository;
import Licenta.Licenta.Service.ImagineService;
import Licenta.Licenta.Service.PacientService;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/user")
public class PacientController {

    @Autowired
    private PacientService pacientService;

    @Autowired
    private ImagineService imagineService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private Cloudinary cloudinary;

    // Create a new pacient with image
    @PostMapping("/{userId}/pacient")
    public ResponseEntity<?> createPacient(
            @PathVariable String userId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("nume_pacient") String numePacient,
            @RequestParam("prenume_pacient") String prenumePacient,
            @RequestParam("sex") Sex sex,
            @RequestParam("detalii") String detalii,
            @RequestParam("cnp") String cnp,
            @RequestParam("numar_telefon") String numarTelefon,
            @RequestParam("data_nasterii") String dataNasterii,
            @RequestParam("istoric_medical") String istoricMedical) {

        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        try {
            // Create and save Pacient
            Pacient pacient = new Pacient();
            pacient.setUserId(userId);
            pacient.setNumePacient(numePacient);
            pacient.setPrenumePacient(prenumePacient);
            pacient.setSex(sex);
            pacient.setDetalii(detalii);
            pacient.setCnp(cnp);
            pacient.setNumarTelefon(numarTelefon);
            if (!dataNasterii.isEmpty()) {
                pacient.setDataNasterii(LocalDate.parse(dataNasterii));
            }
            pacient.setIstoricMedical(istoricMedical);

            Pacient savedPacient = pacientService.savePacient(pacient);

            // Upload image to Cloudinary
            File tempFile = File.createTempFile("temp-", file.getOriginalFilename());
            file.transferTo(tempFile);

            Map uploadResult = cloudinary.uploader().upload(tempFile, ObjectUtils.emptyMap());
            String imageUrl = (String) uploadResult.get("secure_url");
            String publicId = (String) uploadResult.get("public_id");

            // Create and save Imagine
            Imagine imagine = new Imagine();
            imagine.setPacientId(savedPacient.getId());
            imagine.setNume(file.getOriginalFilename());

            String contentType = file.getContentType();
            if ("image/jpg".equals(contentType)) {
                contentType = "image/jpeg";
            }

            imagine.setTip(contentType);
            imagine.setImageUrl(imageUrl);
            imagine.setCloudinaryPublicId(publicId);

            imagineService.saveImagine(imagine);

            return ResponseEntity.ok(Map.of("pacientId", savedPacient.getId(), "imageUrl", imageUrl));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Upload failed: " + e.getMessage());
        }
    }

    // Add another image to existing pacient
    @PostMapping("/pacient/{pacientId}/imagine")
    public ResponseEntity<?> addImageToPacient(
            @PathVariable String pacientId,
            @RequestParam("file") MultipartFile file) {

        Optional<Pacient> pacientOptional = pacientService.findById(pacientId);
        if (pacientOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        try {
            // Upload image to Cloudinary
            File tempFile = File.createTempFile("temp-", file.getOriginalFilename());
            file.transferTo(tempFile);

            Map uploadResult = cloudinary.uploader().upload(tempFile, ObjectUtils.emptyMap());
            String imageUrl = (String) uploadResult.get("secure_url");
            String publicId = (String) uploadResult.get("public_id");

            // Create and save Imagine
            Imagine imagine = new Imagine();
            imagine.setPacientId(pacientId);
            imagine.setNume(file.getOriginalFilename());

            String contentType = file.getContentType();
            if ("image/jpg".equals(contentType)) {
                contentType = "image/jpeg";
            }

            imagine.setTip(contentType);
            imagine.setImageUrl(imageUrl);
            imagine.setCloudinaryPublicId(publicId);

            imagineService.saveImagine(imagine);

            return ResponseEntity.ok(Map.of("imageUrl", imageUrl));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Upload failed: " + e.getMessage());
        }
    }

    // Get all pacienti for a user
    @GetMapping("/{userId}/pacienti")
    public ResponseEntity<List<PacientDto>> getUserPacienti(@PathVariable String userId) {
        List<Pacient> pacienti = pacientService.getAllPacientsByUserId(userId);

        if (pacienti.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<PacientDto> pacientDTOList = pacienti.stream()
                .map(pacient -> {
                    List<Imagine> pacientImagini = imagineService.getAllImaginiByPacientId(pacient.getId());
                    List<ImagineDto> imaginiDto = pacientImagini.stream()
                            .map(img -> new ImagineDto(
                                    img.getId(),
                                    img.getPacientId(),
                                    img.getImageUrl(),
                                    img.getNume(),
                                    img.getTip(),
                                    img.getCloudinaryPublicId()
                            ))
                            .collect(Collectors.toList());

                    return new PacientDto(
                            pacient.getId(),
                            pacient.getNumePacient(),
                            pacient.getPrenumePacient(),
                            pacient.getSex(),
                            pacient.getDetalii(),
                            pacient.getDataNasterii(),
                            pacient.getCnp(),
                            pacient.getNumarTelefon(),
                            pacient.getIstoricMedical(),
                            imaginiDto
                    );
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok().body(pacientDTOList);
    }

    // Get a specific pacient
    @GetMapping("/{userId}/pacient/{pacientId}")
    public ResponseEntity<PacientDto> getPacient(@PathVariable String userId, @PathVariable String pacientId) {
        Optional<Pacient> pacientOptional = pacientService.findByUserIdAndId(userId, pacientId);

        if (pacientOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Pacient pacient = pacientOptional.get();
        List<Imagine> pacientImagini = imagineService.getAllImaginiByPacientId(pacient.getId());
        List<ImagineDto> imaginiDto = pacientImagini.stream()
                .map(img -> new ImagineDto(
                        img.getId(),
                        img.getPacientId(),
                        img.getImageUrl(),
                        img.getNume(),
                        img.getTip(),
                        img.getCloudinaryPublicId()
                ))
                .collect(Collectors.toList());

        PacientDto pacientDto = new PacientDto(
                pacient.getId(),
                pacient.getNumePacient(),
                pacient.getPrenumePacient(),
                pacient.getSex(),
                pacient.getDetalii(),
                pacient.getDataNasterii(),
                pacient.getCnp(),
                pacient.getNumarTelefon(),
                pacient.getIstoricMedical(),
                imaginiDto
        );

        return ResponseEntity.ok(pacientDto);
    }

    // Get all images for a pacient
    @GetMapping("/pacient/{pacientId}/imagini")
    public ResponseEntity<List<ImagineDto>> getPacientImages(@PathVariable String pacientId) {
        List<Imagine> imagini = imagineService.getAllImaginiByPacientId(pacientId);

        if (imagini.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<ImagineDto> imagineDTOList = imagini.stream()
                .map(image -> new ImagineDto(
                        image.getId(),
                        image.getPacientId(),
                        image.getImageUrl(),
                        image.getNume(),
                        image.getTip(),
                        image.getCloudinaryPublicId()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok().body(imagineDTOList);
    }

    // Delete an image
    @DeleteMapping("/pacient/{pacientId}/imagine/{imageId}")
    public ResponseEntity<?> deleteImage(@PathVariable String pacientId, @PathVariable String imageId) {
        try {
            Optional<Imagine> imagineOptional = imagineService.findByPacientIdAndId(pacientId, imageId);

            if (imagineOptional.isEmpty()) {
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body("Imaginea nu a fost găsită sau nu aparține acestui pacient.");
            }

            Imagine imagine = imagineOptional.get();

            // Delete from Cloudinary
            if (imagine.getCloudinaryPublicId() != null && !imagine.getCloudinaryPublicId().isEmpty()) {
                Map result = cloudinary.uploader().destroy(imagine.getCloudinaryPublicId(), ObjectUtils.emptyMap());
                System.out.println("Cloudinary delete result: " + result);
            }

            // Delete from database
            imagineService.deleteImagine(imagine);

            return ResponseEntity.ok("Imaginea a fost ștearsă cu succes.");
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Eroare la ștergerea imaginii: " + e.getMessage());
        }
    }

    // Delete a pacient (and all its images)
    @DeleteMapping("/{userId}/pacient/{pacientId}")
    public ResponseEntity<?> deletePacient(@PathVariable String userId, @PathVariable String pacientId) {
        try {
            Optional<Pacient> pacientOptional = pacientService.findByUserIdAndId(userId, pacientId);

            if (pacientOptional.isEmpty()) {
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body("Pacientul nu a fost găsit.");
            }

            Pacient pacient = pacientOptional.get();

            // Get all images for this patient
            List<Imagine> imagini = imagineService.getAllImaginiByPacientId(pacient.getId());

            // Delete all images from Cloudinary
            for (Imagine imagine : imagini) {
                if (imagine.getCloudinaryPublicId() != null && !imagine.getCloudinaryPublicId().isEmpty()) {
                    cloudinary.uploader().destroy(imagine.getCloudinaryPublicId(), ObjectUtils.emptyMap());
                }
                imagineService.deleteImagine(imagine);
            }

            // Delete pacient
            pacientService.deletePacient(pacient);

            return ResponseEntity.ok("Pacientul și imaginile au fost șterse cu succes.");
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Eroare la ștergerea pacientului: " + e.getMessage());
        }
    }

    // Update pacient details
    @PutMapping("/{userId}/pacient/{pacientId}")
    public ResponseEntity<?> updatePacient(
            @PathVariable String userId,
            @PathVariable String pacientId,
            @RequestBody PacientDto pacientDto) {

        Optional<Pacient> pacientOptional = pacientService.findByUserIdAndId(userId, pacientId);

        if (pacientOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Pacient pacient = pacientOptional.get();
        pacient.setNumePacient(pacientDto.getNumePacient());
        pacient.setPrenumePacient(pacientDto.getPrenumePacient());
        pacient.setSex(pacientDto.getSex());
        pacient.setDetalii(pacientDto.getDetalii());
        pacient.setDataNasterii(pacientDto.getDataNasterii());
        pacient.setCnp(pacientDto.getCnp());
        pacient.setNumarTelefon(pacientDto.getNumarTelefon());
        pacient.setIstoricMedical(pacientDto.getIstoricMedical());

        Pacient updatedPacient = pacientService.savePacient(pacient);

        return ResponseEntity.ok(updatedPacient);
    }
}

