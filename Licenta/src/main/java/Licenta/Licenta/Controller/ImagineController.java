package Licenta.Licenta.Controller;

import Licenta.Licenta.Model.Imagine;
import Licenta.Licenta.Model.User;
import Licenta.Licenta.Repository.ImagineRepository;
import Licenta.Licenta.Repository.UserRepository;
import Licenta.Licenta.Service.ImagineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
//@RequestMapping("/user")
//@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/user")
public class ImagineController {
    @Autowired
    private ImagineRepository imagineRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ImagineService imagineService;

    /// incarcarea img
    @PostMapping("/{id}/imagine")
    public ResponseEntity<String> uploadImage(@PathVariable Long id, @RequestParam("file") MultipartFile file) throws IOException {
        Optional<User> userOptional = userRepository.findById(id);
        if (!userOptional.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        User user = userOptional.get();
        Imagine userImage = new  Imagine();
        userImage.setUser(user);
        userImage.setImagine(file.getBytes()); // Dacă salvezi în DB
        imagineRepository.save(userImage);
        System.out.println("Uploading image for user: " + id);
        return ResponseEntity.ok("Imagine încărcată cu succes!");
    }

    @GetMapping("/{id}/imagini")
    public ResponseEntity<List<byte[]>> getUserImages(@PathVariable Long id) {
        List<Imagine> imagini = imagineService.getAllImaginIByIdUser(id);
        if (imagini.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<byte[]> imageDataList = imagini.stream()
                .map(Imagine::getImagine)
                .collect(Collectors.toList());

        return ResponseEntity.ok().body(imageDataList);
    }

}
