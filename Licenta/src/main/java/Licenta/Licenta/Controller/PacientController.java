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
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Date;
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

    // Create a new pacient with optional image
    @PostMapping("/{userId}/pacient")
    public ResponseEntity<?> createPacient(
            @PathVariable String userId,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam("nume_pacient") String numePacient,
            @RequestParam("prenume_pacient") String prenumePacient,
            @RequestParam("sex") Sex sex,
            @RequestParam(value = "detalii", required = false, defaultValue = "") String detalii,
            @RequestParam("cnp") String cnp,
            @RequestParam("numar_telefon") String numarTelefon,
            @RequestParam(value = "data_nasterii", required = false, defaultValue = "") String dataNasterii,
            @RequestParam(value = "istoric_medical", required = false, defaultValue = "") String istoricMedical,
            @RequestParam(value = "isDicom", required = false) Boolean isDicom,
            @RequestParam(value = "dicomMetadata", required = false) String dicomMetadataJson) {

        System.out.println("=== CREATE PACIENT REQUEST ===");
        System.out.println("User ID: " + userId);
        System.out.println("File present: " + (file != null && !file.isEmpty()));
        System.out.println("Nume pacient: " + numePacient);

        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            System.out.println("User not found!");
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
            if (dataNasterii != null && !dataNasterii.isEmpty()) {
                pacient.setDataNasterii(LocalDate.parse(dataNasterii));
            }
            pacient.setIstoricMedical(istoricMedical);

            Pacient savedPacient = pacientService.savePacient(pacient);

            // Upload image to Cloudinary if file is provided
            String imageUrl = null;
            if (file != null && !file.isEmpty()) {
                File tempFile = File.createTempFile("temp-", file.getOriginalFilename());
                file.transferTo(tempFile);

                Map uploadResult;
                // Check if file is DICOM - upload as raw file
                if (Boolean.TRUE.equals(isDicom) || file.getOriginalFilename().toLowerCase().endsWith(".dcm")) {
                    uploadResult = cloudinary.uploader().upload(tempFile,
                        ObjectUtils.asMap("resource_type", "raw"));
                } else {
                    uploadResult = cloudinary.uploader().upload(tempFile, ObjectUtils.emptyMap());
                }

                imageUrl = (String) uploadResult.get("secure_url");
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
                imagine.setDataIncarcare(new Date());
                imagine.setStatusAnaliza("neanalizata");

                // Process DICOM metadata if provided
                if (Boolean.TRUE.equals(isDicom) || file.getOriginalFilename().toLowerCase().endsWith(".dcm")) {
                    imagine.setIsDicom(true);
                    if (dicomMetadataJson != null && !dicomMetadataJson.isEmpty()) {
                        try {
                            ObjectMapper mapper = new ObjectMapper();
                            Map<String, Object> metadata = mapper.readValue(
                                    dicomMetadataJson,
                                    new TypeReference<Map<String, Object>>() {}
                            );
                            imagine.setDicomMetadata(metadata);
                        } catch (Exception e) {
                            System.err.println("Error parsing DICOM metadata: " + e.getMessage());
                        }
                    }
                }

                imagineService.saveImagine(imagine);

                tempFile.delete(); // Clean up temp file
            }

            if (imageUrl != null) {
                return ResponseEntity.ok(Map.of(
                    "pacientId", savedPacient.getId(),
                    "imageUrl", imageUrl,
                    "message", "Pacient created successfully with image"
                ));
            } else {
                return ResponseEntity.ok(Map.of(
                    "pacientId", savedPacient.getId(),
                    "message", "Pacient created successfully without image"
                ));
            }
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Upload failed: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error creating pacient: " + e.getMessage()));
        }
    }

    // Create a new pacient without image (JSON body)
    @PostMapping("/{userId}/pacient/simple")
    public ResponseEntity<?> createPacientSimple(
            @PathVariable String userId,
            @RequestBody PacientDto pacientDto) {

        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        try {
            // Create and save Pacient
            Pacient pacient = new Pacient();
            pacient.setUserId(userId);
            pacient.setNumePacient(pacientDto.getNumePacient());
            pacient.setPrenumePacient(pacientDto.getPrenumePacient());
            pacient.setSex(pacientDto.getSex());
            pacient.setDetalii(pacientDto.getDetalii());
            pacient.setCnp(pacientDto.getCnp());
            pacient.setNumarTelefon(pacientDto.getNumarTelefon());
            pacient.setDataNasterii(pacientDto.getDataNasterii());
            pacient.setIstoricMedical(pacientDto.getIstoricMedical());

            Pacient savedPacient = pacientService.savePacient(pacient);

            return ResponseEntity.ok(Map.of(
                "pacientId", savedPacient.getId(),
                "message", "Pacient created successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error creating pacient: " + e.getMessage()));
        }
    }

    // Create pacient with JSON data + optional file
    @PostMapping(value = "/{userId}/pacient/withdata", consumes = {"multipart/form-data"})
    public ResponseEntity<?> createPacientWithJsonData(
            @PathVariable String userId,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam("pacientData") String pacientDataJson,
            @RequestParam(value = "isDicom", required = false) Boolean isDicom,
            @RequestParam(value = "dicomMetadata", required = false) String dicomMetadataJson) {

        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        try {
            // Parse JSON data
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
            PacientDto pacientDto = mapper.readValue(pacientDataJson, PacientDto.class);

            // Create and save Pacient
            Pacient pacient = new Pacient();
            pacient.setUserId(userId);
            pacient.setNumePacient(pacientDto.getNumePacient());
            pacient.setPrenumePacient(pacientDto.getPrenumePacient());
            pacient.setSex(pacientDto.getSex());
            pacient.setDetalii(pacientDto.getDetalii() != null ? pacientDto.getDetalii() : "");
            pacient.setCnp(pacientDto.getCnp());
            pacient.setNumarTelefon(pacientDto.getNumarTelefon());
            pacient.setDataNasterii(pacientDto.getDataNasterii());
            pacient.setIstoricMedical(pacientDto.getIstoricMedical() != null ? pacientDto.getIstoricMedical() : "");

            Pacient savedPacient = pacientService.savePacient(pacient);

            // Upload image to Cloudinary if file is provided
            String imageUrl = null;
            if (file != null && !file.isEmpty()) {
                File tempFile = File.createTempFile("temp-", file.getOriginalFilename());
                file.transferTo(tempFile);

                Map uploadResult;
                // Check if file is DICOM - upload as raw file
                if (Boolean.TRUE.equals(isDicom) || file.getOriginalFilename().toLowerCase().endsWith(".dcm")) {
                    uploadResult = cloudinary.uploader().upload(tempFile,
                        ObjectUtils.asMap("resource_type", "raw"));
                } else {
                    uploadResult = cloudinary.uploader().upload(tempFile, ObjectUtils.emptyMap());
                }

                imageUrl = (String) uploadResult.get("secure_url");
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
                imagine.setDataIncarcare(new Date());
                imagine.setStatusAnaliza("neanalizata");

                // Process DICOM metadata if provided
                if (Boolean.TRUE.equals(isDicom) || file.getOriginalFilename().toLowerCase().endsWith(".dcm")) {
                    imagine.setIsDicom(true);
                    if (dicomMetadataJson != null && !dicomMetadataJson.isEmpty()) {
                        try {
                            ObjectMapper dicomMapper = new ObjectMapper();
                            Map<String, Object> metadata = dicomMapper.readValue(
                                    dicomMetadataJson,
                                    new TypeReference<Map<String, Object>>() {}
                            );
                            imagine.setDicomMetadata(metadata);
                        } catch (Exception e) {
                            System.err.println("Error parsing DICOM metadata: " + e.getMessage());
                        }
                    }
                }

                imagineService.saveImagine(imagine);

                tempFile.delete();
            }

            if (imageUrl != null) {
                return ResponseEntity.ok(Map.of(
                    "pacientId", savedPacient.getId(),
                    "imageUrl", imageUrl,
                    "message", "Pacient created successfully with image"
                ));
            } else {
                return ResponseEntity.ok(Map.of(
                    "pacientId", savedPacient.getId(),
                    "message", "Pacient created successfully without image"
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error creating pacient: " + e.getMessage()));
        }
    }

    // Add another image to existing pacient
    @PostMapping("/pacient/{pacientId}/imagine")
    public ResponseEntity<?> addImageToPacient(
            @PathVariable String pacientId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "isDicom", required = false) Boolean isDicom,
            @RequestParam(value = "dicomMetadata", required = false) String dicomMetadataJson) {

        Optional<Pacient> pacientOptional = pacientService.findById(pacientId);
        if (pacientOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        try {
            // Upload image to Cloudinary
            File tempFile = File.createTempFile("temp-", file.getOriginalFilename());
            file.transferTo(tempFile);

            Map uploadResult;
            // Check if file is DICOM - upload as raw file
            if (Boolean.TRUE.equals(isDicom) || file.getOriginalFilename().toLowerCase().endsWith(".dcm")) {
                uploadResult = cloudinary.uploader().upload(tempFile,
                    ObjectUtils.asMap("resource_type", "raw"));
            } else {
                uploadResult = cloudinary.uploader().upload(tempFile, ObjectUtils.emptyMap());
            }

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
            imagine.setDataIncarcare(new Date());
            imagine.setStatusAnaliza("neanalizata");

            // Process DICOM metadata if provided
            if (Boolean.TRUE.equals(isDicom) || file.getOriginalFilename().toLowerCase().endsWith(".dcm")) {
                imagine.setIsDicom(true);
                if (dicomMetadataJson != null && !dicomMetadataJson.isEmpty()) {
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        Map<String, Object> metadata = mapper.readValue(
                                dicomMetadataJson,
                                new TypeReference<Map<String, Object>>() {}
                        );
                        imagine.setDicomMetadata(metadata);
                    } catch (Exception e) {
                        System.err.println("Error parsing DICOM metadata: " + e.getMessage());
                    }
                }
            }

            imagineService.saveImagine(imagine);

            return ResponseEntity.ok(Map.of("imageUrl", imageUrl));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Upload failed: " + e.getMessage());
        }
    }

    // Upload imagine with additional details (nume, tip, observatii, statusAnaliza)
    @PostMapping("/{userId}/pacient/{pacientId}/imagine")
    public ResponseEntity<?> uploadImagineWithDetails(
            @PathVariable String userId,
            @PathVariable String pacientId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("nume") String nume,
            @RequestParam("tip") String tip,
            @RequestParam(value = "observatii", required = false) String observatii,
            @RequestParam(value = "statusAnaliza", defaultValue = "neanalizata") String statusAnaliza,
            @RequestParam(value = "isDicom", required = false) Boolean isDicom,
            @RequestParam(value = "dicomMetadata", required = false) String dicomMetadataJson,
            @RequestHeader(value = "Authorization", required = false) String token) {

        // Verify user exists
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "User not found"));
        }

        // Verify pacient exists and belongs to user
        Optional<Pacient> pacientOptional = pacientService.findByUserIdAndId(userId, pacientId);
        if (pacientOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "Pacient not found or does not belong to this user"));
        }

        try {
            System.out.println("=== uploadImagineWithDetails ===");
            System.out.println("File: " + file.getOriginalFilename());
            System.out.println("File size: " + file.getSize());
            System.out.println("Content type: " + file.getContentType());

            // Check if file is empty BEFORE doing anything
            if (file.isEmpty() || file.getSize() == 0) {
                System.err.println("ERROR: File is EMPTY!");
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "File is empty. Please select a valid file."));
            }

            // Read bytes to verify content
            byte[] fileBytes = file.getBytes();
            System.out.println("Read " + fileBytes.length + " bytes from multipart file");

            // Check if all bytes are zero
            boolean allZeros = true;
            for (int i = 0; i < Math.min(1000, fileBytes.length); i++) {
                if (fileBytes[i] != 0) {
                    allZeros = false;
                    break;
                }
            }

            if (allZeros) {
                System.err.println("ERROR: File contains only ZERO bytes!");
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "File appears to be corrupted or empty (all zero bytes)"));
            }

            System.out.println("First 32 bytes (hex): " + bytesToHex(fileBytes, 32));

            // Upload file to Cloudinary using the byte array
            Map uploadResult;
            // Check if file is DICOM - upload as raw file
            if (Boolean.TRUE.equals(isDicom) || file.getOriginalFilename().toLowerCase().endsWith(".dcm")) {
                // Upload DICOM as raw file
                uploadResult = cloudinary.uploader().upload(fileBytes,
                    ObjectUtils.asMap("resource_type", "raw"));
            } else {
                // Upload regular images
                uploadResult = cloudinary.uploader().upload(fileBytes, ObjectUtils.emptyMap());
            }

            String imageUrl = (String) uploadResult.get("secure_url");
            String publicId = (String) uploadResult.get("public_id");

            // Create and save Imagine
            Imagine imagine = new Imagine();
            imagine.setPacientId(pacientId);
            imagine.setNume(nume);

            String contentType = tip;
            if ("image/jpg".equals(contentType)) {
                contentType = "image/jpeg";
            }

            imagine.setTip(contentType);
            imagine.setImageUrl(imageUrl);
            imagine.setCloudinaryPublicId(publicId);
            imagine.setObservatii(observatii);
            imagine.setStatusAnaliza(statusAnaliza);
            imagine.setDataIncarcare(new Date());

            // Process DICOM metadata if provided
            if (Boolean.TRUE.equals(isDicom) || file.getOriginalFilename().toLowerCase().endsWith(".dcm")) {
                imagine.setIsDicom(true);
                if (dicomMetadataJson != null && !dicomMetadataJson.isEmpty()) {
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        Map<String, Object> metadata = mapper.readValue(
                                dicomMetadataJson,
                                new TypeReference<Map<String, Object>>() {}
                        );
                        imagine.setDicomMetadata(metadata);
                    } catch (Exception e) {
                        System.err.println("Error parsing DICOM metadata: " + e.getMessage());
                    }
                }
            }

            Imagine savedImagine = imagineService.saveImagine(imagine);

            System.out.println("✅ Image saved successfully: " + savedImagine.getId());

            return ResponseEntity.ok(savedImagine);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Upload failed: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", "Error uploading image: " + e.getMessage()));
        }
    }

    // Update imagine details
    @PutMapping("/{userId}/pacient/{pacientId}/imagine/{imagineId}")
    public ResponseEntity<?> updateImagineDetails(
            @PathVariable String userId,
            @PathVariable String pacientId,
            @PathVariable String imagineId,
            @RequestBody Imagine imagineUpdate,
            @RequestHeader(value = "Authorization", required = false) String token) {

        // Verify user exists
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "User not found"));
        }

        // Verify pacient exists and belongs to user
        Optional<Pacient> pacientOptional = pacientService.findByUserIdAndId(userId, pacientId);
        if (pacientOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "Pacient not found or does not belong to this user"));
        }

        // Verify imagine exists and belongs to pacient
        Optional<Imagine> imagineOptional = imagineService.findByPacientIdAndId(pacientId, imagineId);
        if (imagineOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "Imagine not found or does not belong to this pacient"));
        }

        try {
            Imagine imagine = imagineOptional.get();

            // Update all fields if provided
            if (imagineUpdate.getNume() != null) {
                imagine.setNume(imagineUpdate.getNume());
            }
            if (imagineUpdate.getTip() != null) {
                imagine.setTip(imagineUpdate.getTip());
            }
            if (imagineUpdate.getObservatii() != null) {
                imagine.setObservatii(imagineUpdate.getObservatii());
            }
            if (imagineUpdate.getStatusAnaliza() != null) {
                imagine.setStatusAnaliza(imagineUpdate.getStatusAnaliza());
            }
            if (imagineUpdate.getAreTumoare() != null) {
                imagine.setAreTumoare(imagineUpdate.getAreTumoare());
            }
            if (imagineUpdate.getTipTumoare() != null) {
                imagine.setTipTumoare(imagineUpdate.getTipTumoare());
            }
            if (imagineUpdate.getConfidenta() != null) {
                imagine.setConfidenta(imagineUpdate.getConfidenta());
            }
            if (imagineUpdate.getDataAnalizei() != null) {
                imagine.setDataAnalizei(imagineUpdate.getDataAnalizei());
            }

            // Update dataModificare timestamp
            imagine.setDataModificare(new Date());

            Imagine updatedImagine = imagineService.saveImagine(imagine);
            return ResponseEntity.ok(updatedImagine);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error updating imagine: " + e.getMessage()));
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
                                    img.getImagineId(),
                                    img.getImagineUrl(),
                                    img.getImagineNume(),
                                    img.getImagineTip(),
                                    img.getImagineDataIncarcare()
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
                        img.getImagineId(),
                        img.getImagineUrl(),
                        img.getImagineNume(),
                        img.getImagineTip(),
                        img.getImagineDataIncarcare()
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
                .map(img -> new ImagineDto(
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
                        img.getImagineId(),
                        img.getImagineUrl(),
                        img.getImagineNume(),
                        img.getImagineTip(),
                        img.getImagineDataIncarcare()
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
                Map result;
                // Check if it's a DICOM file and delete as raw resource
                if (Boolean.TRUE.equals(imagine.getIsDicom())) {
                    result = cloudinary.uploader().destroy(imagine.getCloudinaryPublicId(),
                        ObjectUtils.asMap("resource_type", "raw"));
                } else {
                    result = cloudinary.uploader().destroy(imagine.getCloudinaryPublicId(), ObjectUtils.emptyMap());
                }
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
                    // Check if it's a DICOM file and delete as raw resource
                    if (Boolean.TRUE.equals(imagine.getIsDicom())) {
                        cloudinary.uploader().destroy(imagine.getCloudinaryPublicId(),
                            ObjectUtils.asMap("resource_type", "raw"));
                    } else {
                        cloudinary.uploader().destroy(imagine.getCloudinaryPublicId(), ObjectUtils.emptyMap());
                    }
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

    // Helper method to debug byte content
    private String bytesToHex(byte[] bytes, int limit) {
        StringBuilder sb = new StringBuilder();
        int count = Math.min(bytes.length, limit);
        for (int i = 0; i < count; i++) {
            sb.append(String.format("%02x", bytes[i]));
        }
        return sb.toString();
    }
}

