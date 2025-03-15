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
        userImage.setTip(file.getContentType());
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

    /* @GetMapping("/{id}/imagini")
     public ResponseEntity<List<String>> getUserImages(@PathVariable Long id) {
         List<Imagine> imagini = imagineService.getAllImaginIByIdUser(id);
         if (imagini.isEmpty()) {
             return ResponseEntity.notFound().build();
         }

         List<String> imageDataList = imagini.stream()
                 .map(image -> Base64.getEncoder().encodeToString(decompressBytes(image.getImagine())))
                 .collect(Collectors.toList());

         return ResponseEntity.ok().body(imageDataList);
     }*/
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


    /*@DeleteMapping("/{userId}/imagine")
    public ResponseEntity<?> deleteImage(@PathVariable Long userId, @RequestBody Map<String, String> payload) {
        try {
            String imageData = payload.get("imagine");
            if (imageData == null) {
                return ResponseEntity.badRequest().body("No image data provided");
            }

            // Remove the "data:image/jpeg;base64," prefix if present
            String base64Image = imageData.contains(",") ?
                    imageData.substring(imageData.indexOf(",") + 1) :
                    imageData;

            // Convert base64 string back to byte array
            byte[] imageBytes = Base64.getDecoder().decode(base64Image);

            // Find and delete the image from the database
            boolean deleted = imagineService.deteleImagineByImagineAndUser(imageBytes,userId);

            if (deleted) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting image: " + e.getMessage());
        }
    }*/
    @DeleteMapping("/{id}/imagini")
    public ResponseEntity<String> deleteImage(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String base64Image = body.get("imagine").replaceAll("\\s+", "");  // Elimină caracterele de linie nouă și spațiile
        byte[] imageBytes = Base64.getDecoder().decode(base64Image);  // Decodifici în byte[]

        // Găsește imaginea în baza de date folosind imageBytes
        Optional<Imagine> imagine = imagineRepository.findByImagineAndUser(imageBytes, userRepository.findById(id).get());

        if (imagine.isPresent()) {
            imagineRepository.delete(imagine.get());  // Șterge imaginea din baza de date
            return ResponseEntity.ok("Imaginea a fost ștearsă");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Imaginea nu a fost găsită");
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

        return ResponseEntity.ok(imagineDto);
    }


}

