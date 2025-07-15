package Licenta.Licenta.Controller;

import Licenta.Licenta.Dto.ImagineDto;
import Licenta.Licenta.Model.Imagine;
import Licenta.Licenta.Model.User;
import Licenta.Licenta.Repository.ImagineRepository;
import Licenta.Licenta.Repository.UserRepository;
import Licenta.Licenta.Service.ImagineService;
import Licenta.Licenta.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;


@RestController
//@RequestMapping("/user")
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/user")
public class ImagineController {
    @Autowired
    private ImagineRepository imagineRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ImagineService imagineService;
    @Autowired
    private UserService userService;

    /// incarcarea img
    @PostMapping("/{id}/imagine")
    public ResponseEntity<String> uploadImage(@PathVariable Long id, @RequestParam("file") MultipartFile file) throws IOException {
        Optional<User> userOptional = userRepository.findById(id);
        if (!userOptional.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        User user = userOptional.get();
        Imagine userImage = new Imagine();
        userImage.setUser(user);
        userImage.setNume(file.getOriginalFilename());
        String contentType = file.getContentType();
        if ("image/jpg".equals(contentType)) {
            contentType = "image/jpeg";
        }
        userImage.setTip(contentType);
        userImage.setImagine(compressBytes(file.getBytes())); // Dacă salvezi în DB
        imagineRepository.save(userImage);
        System.out.println("Uploading image for user: " + id);
        return ResponseEntity.ok(HttpStatus.OK.toString());
    }

    public static byte[] compressBytes(byte[] data) {
        Deflater deflater = new Deflater();
        deflater.setInput(data);
        deflater.finish();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
        byte[] buffer = new byte[1024];
        while (!deflater.finished()) {
            int count = deflater.deflate(buffer);
            outputStream.write(buffer, 0, count);
        }
        try {
            outputStream.close();
        } catch (IOException e) {
        }
        System.out.println("Compressed Image Byte Size - " + outputStream.toByteArray().length);

        return outputStream.toByteArray();
    }

  ;
    public static byte[] decompressBytes(byte[] data) {
        Inflater inflater = new Inflater();
        inflater.setInput(data);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
        byte[] buffer = new byte[1024];
        try {
            while (!inflater.finished()) {
                int count = inflater.inflate(buffer);
                outputStream.write(buffer, 0, count);
            }
            outputStream.close();
        } catch (IOException ioe) {
        } catch (DataFormatException e) {
        }
        return outputStream.toByteArray();
    }

    @GetMapping("/{id}/imagini")
    public ResponseEntity<List<ImagineDto>> getUserImages(@PathVariable Long id) {
        List<Imagine> imagini = imagineService.getAllImaginIByIdUser(id);

        if (imagini.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        List<ImagineDto> imagineDTOList = imagini.stream()
                .map(image -> new ImagineDto(
                        image.getId(),  // ID-ul imaginii
                        Base64.getEncoder().encodeToString(decompressBytes(image.getImagine())),  // Imaginea în format Base64
                        image.getNume(),  // Numele imaginii
                        image.getTip()  // Tipul imaginii
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok().body(imagineDTOList);
    }


    @DeleteMapping("/{userId}/imagine/{imageId}")
    public ResponseEntity<?> deleteImage(@PathVariable Long userId, @PathVariable Long imageId) {
        try {
            // First check if image exists and belongs to user
            Optional<Imagine> imagine = imagineRepository.findByUserIdAndId(userId,imageId);

            if (!imagine.isPresent()) {
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body("Imaginea nu a fost găsită sau nu aparține acestui utilizator");
            }

            // Delete the image
            imagineRepository.delete(imagine.get());
            getUserImages(userId);

            return ResponseEntity.ok("Imaginea a fost ștearsă cu succes");
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Eroare la ștergerea imaginii: " + e.getMessage());
        }
    }
    @GetMapping("/{id}/imagine/{imageId}")
    public ResponseEntity<ImagineDto> getImage(@PathVariable Long id, @PathVariable Long imageId) {
        Optional<Imagine> imagine = imagineService.findByUserIdAndId(id, imageId);
        if (imagine.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Creăm DTO-ul corect
        ImagineDto imagineDto = new ImagineDto(
                imagine.get().getId(),
                Base64.getEncoder().encodeToString(decompressBytes(imagine.get().getImagine())),
                imagine.get().getNume(),
                imagine.get().getTip()
        );
          getUserImages(id);
        return ResponseEntity.ok(imagineDto);
    }


}

