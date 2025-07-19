package Licenta.Licenta.Controller;

import Licenta.Licenta.Dto.ImagineDto;
import Licenta.Licenta.Model.Imagine;
import Licenta.Licenta.Model.User;
import Licenta.Licenta.Repository.ImagineRepository;
import Licenta.Licenta.Repository.UserRepository;
import Licenta.Licenta.Service.ImagineService;
import Licenta.Licenta.Service.UserService;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
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
    private Cloudinary cloudinary;
    @Autowired
    private ImagineService imagineService;
    @Autowired
    private UserService userService;

    /// incarcarea img
    @PostMapping("/{id}/imagine")
    public ResponseEntity<String> uploadImage(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        Optional<User> userOptional = userRepository.findById(id);
        if (!userOptional.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        try {
            // Salvează temporar fișierul pe disc
            File tempFile = File.createTempFile("temp-", file.getOriginalFilename());
            file.transferTo(tempFile);

            // Urcă fișierul pe Cloudinary
            Map uploadResult = cloudinary.uploader().upload(tempFile, ObjectUtils.emptyMap());
            String imageUrl = (String) uploadResult.get("secure_url");
            String publicId = (String) uploadResult.get("public_id");

            // Salvează informația imaginii în baza de date
            User user = userOptional.get();
            Imagine userImage = new Imagine();
            userImage.setUser(user);
            userImage.setNume(file.getOriginalFilename());


            String contentType = file.getContentType();
            if ("image/jpg".equals(contentType)) {
                contentType = "image/jpeg";
            }
            userImage.setTip(contentType);
            userImage.setImageUrl(imageUrl);
            userImage.setCloudinaryPublicId(publicId);
            imagineRepository.save(userImage);
            System.out.println("Uploading image for user: " + id);

            return ResponseEntity.ok(imageUrl); // returnează URL-ul imaginii
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Upload failed.");
        }
    }


   /* @PostMapping("/upload")
    public ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile file) {

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
                        image.getId(),              // ID-ul imaginii
                        image.getImageUrl(),             // URL-ul imaginii din cloud
                        image.getNume(),            // Numele imaginii
                        image.getTip(),      // Tipul imaginii (opțional)
                        image.getImageUrl()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok().body(imagineDTOList);
    }




  @DeleteMapping("/{userId}/imagine/{imageId}")
  public ResponseEntity<?> deleteImage(@PathVariable Long userId, @PathVariable Long imageId) {
      try {
          // Verifică dacă imaginea există și aparține utilizatorului
          Optional<Imagine> imagineOptional = imagineRepository.findByUserIdAndId(userId, imageId);

          if (imagineOptional.isEmpty()) {
              return ResponseEntity
                      .status(HttpStatus.NOT_FOUND)
                      .body("Imaginea nu a fost găsită sau nu aparține acestui utilizator.");
          }

          Imagine imagine = imagineOptional.get();

          // Șterge imaginea din Cloudinary
          if (imagine.getCloudinaryPublicId() != null && !imagine.getCloudinaryPublicId().isEmpty()) {
              Map result = cloudinary.uploader().destroy(imagine.getCloudinaryPublicId(), ObjectUtils.emptyMap());
              System.out.println("Cloudinary delete result: " + result);
          }

          // Șterge din baza de date
          imagineRepository.delete(imagine);

          return ResponseEntity.ok("Imaginea a fost ștearsă cu succes din Cloudinary și din baza de date.");
      } catch (Exception e) {
          return ResponseEntity
                  .status(HttpStatus.INTERNAL_SERVER_ERROR)
                  .body("Eroare la ștergerea imaginii: " + e.getMessage());
      }
  }



   @GetMapping("/{id}/imagine/{imageId}")
   public ResponseEntity<String> getImageUrl(@PathVariable Long id, @PathVariable Long imageId) {
       Optional<Imagine> imagine = imagineService.findByUserIdAndId(id, imageId);

       if (imagine.isEmpty()) {
           return ResponseEntity.notFound().build();
       }

       String imageUrl = imagine.get().getImageUrl(); // URL-ul de la Cloudinary
       return ResponseEntity.ok(imagine.get().getImageUrl());
   }






}

