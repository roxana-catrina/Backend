package Licenta.Licenta.Controller;

import Licenta.Licenta.Dto.AuthenticationRequest;
import Licenta.Licenta.Dto.AuthenticationResponse;
import Licenta.Licenta.Model.User;
import Licenta.Licenta.Repository.UserRepository;
import Licenta.Licenta.Service.CustomUserDetailsService;
import Licenta.Licenta.Utils.JwtUtil;
import Licenta.Licenta.Service.UserService;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.*;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {
    @Autowired
    private UserDetailsService userDetailsService;

    private final UserService userService;
    @Autowired
    private CustomUserDetailsService customUserDetailsService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private Cloudinary cloudinary;



    @PostMapping("/user")
    public User postUser(@RequestBody User user){
        try{
            user.setNume(user.getNume().toUpperCase());
            return userService.postUser(user);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Eroare la salvarea utilizatorului. Detalii: " + e.getMessage(), e);
        }
    }
    @GetMapping("/users")
    public List<User> getAllUsers(){
        return  userService.getAllUsers();
    }
    @GetMapping("/user/{id}")
    public ResponseEntity<User>  getUserById(@PathVariable Long id){
        User user = new User();
        user=userService.getUserById(id);
        if(user!=null)
            return ResponseEntity.ok(user);
        else
            return ResponseEntity.notFound().build();
    }

    @PutMapping("/user/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User user) {
        // Găsim utilizatorul în baza de date
        User existingUser = userService.getUserById(id);

        if (existingUser == null) {
            return ResponseEntity.notFound().build(); // 404 Not Found
        }

        // Verificăm dacă email-ul trimis este diferit și există deja în baza de date
        if (!existingUser.getEmail().equals(user.getEmail()) && userService.existsByEmail(user.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null); // 409 Conflict
        }
        user.setNume(user.getNume().toUpperCase());
        // Actualizăm datele utilizatorului
        existingUser.setNume(user.getNume());
        existingUser.setPrenume(user.getPrenume());
        existingUser.setEmail(user.getEmail());
        existingUser.setData_nasterii(user.getData_nasterii());
        existingUser.setSex(user.getSex());
        existingUser.setTara(user.getTara());
        existingUser.setNumar_telefon(user.getNumar_telefon());

        // Salvăm utilizatorul actualizat (use the correct id variable)
        User updatedUser = userService.updateUser(id, existingUser);
        return ResponseEntity.ok(updatedUser);
    }
    @DeleteMapping("/user/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        User existingUser= userService.getUserById(id);
        if(existingUser!=null){
            userService.deleteUser(id);
            return ResponseEntity.ok().build();
        }else{
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/authenticate")
    public ResponseEntity<?> authenticate(@RequestBody AuthenticationRequest authenticationRequest) {
        Optional<User> optionalUser = userRepository.findByEmail(authenticationRequest.getEmail());
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Collections.singletonMap("message", "Utilizatorul nu există"));
        }

        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    authenticationRequest.getEmail(),
                    authenticationRequest.getParola()
            ));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Collections.singletonMap("message", "Parola este incorectă"));
        }

        User user = optionalUser.get();
        final String token = jwtUtil.generateToken(userDetailsService.loadUserByUsername(user.getEmail()));

        AuthenticationResponse authenticationResponse = new AuthenticationResponse();
        authenticationResponse.setJwt(token);
        authenticationResponse.setId(user.getId());
        authenticationResponse.setNume(user.getNume());

        return ResponseEntity.ok(authenticationResponse);
    }

    @PostMapping("/user/{userId}/profile-photo")
    public ResponseEntity<?> uploadProfilePhoto(
            @PathVariable Long userId,
            @RequestParam("profilePhoto") MultipartFile file) {

        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("File is empty");
            }

            // Validare tip fișier
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest().body("Only image files are allowed");
            }

            User user = userService.getUserById(userId);
            if (user == null) {
                return ResponseEntity.notFound().build();
            }

            // Delete old photo from Cloudinary if exists
            if (user.getProfilePhotoUrl() != null && user.getProfilePhotoUrl().contains("cloudinary")) {
                try {
                    // Extract public_id from URL if possible
                    String oldUrl = user.getProfilePhotoUrl();
                    if (oldUrl.contains("/upload/")) {
                        String publicId = oldUrl.substring(oldUrl.indexOf("/upload/") + 8);
                        publicId = publicId.substring(0, publicId.lastIndexOf("."));
                        cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
                    }
                } catch (Exception e) {
                    // Log but don't fail if old photo deletion fails
                    System.err.println("Failed to delete old profile photo: " + e.getMessage());
                }
            }

            // Upload to Cloudinary
            File tempFile = File.createTempFile("profile-", file.getOriginalFilename());
            file.transferTo(tempFile);

            @SuppressWarnings("unchecked")
            Map<String, Object> uploadResult = cloudinary.uploader().upload(tempFile, ObjectUtils.emptyMap());
            String imageUrl = (String) uploadResult.get("secure_url");
            String publicId = (String) uploadResult.get("public_id");

            // Delete temp file
            if (!tempFile.delete()) {
                System.err.println("Failed to delete temp file: " + tempFile.getPath());
            }

            // Save Cloudinary URL to database
            user.setProfilePhotoUrl(imageUrl);
            userService.updateUser(userId, user);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Profile photo uploaded successfully");
            response.put("photoUrl", imageUrl);
            response.put("publicId", publicId);

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error uploading file: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error uploading to Cloudinary: " + e.getMessage());
        }
    }
    @DeleteMapping("/user/{userId}/profile-photo")
    public ResponseEntity<?> deleteProfilePhoto(@PathVariable Long userId) {
        try {
            User user = userService.getUserById(userId);

            if (user == null) {
                return ResponseEntity.notFound().build();
            }

            if (user.getProfilePhotoUrl() != null && user.getProfilePhotoUrl().contains("cloudinary")) {
                try {
                    // Extract public_id from Cloudinary URL
                    String photoUrl = user.getProfilePhotoUrl();
                    if (photoUrl.contains("/upload/")) {
                        String publicId = photoUrl.substring(photoUrl.indexOf("/upload/") + 8);
                        publicId = publicId.substring(0, publicId.lastIndexOf("."));
                        cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
                    }
                } catch (Exception e) {
                    System.err.println("Failed to delete photo from Cloudinary: " + e.getMessage());
                }
            }

            // Remove URL from database
            user.setProfilePhotoUrl(null);
            userService.updateUser(userId, user);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Profile photo deleted successfully");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting profile photo: " + e.getMessage());
        }
    }

    @GetMapping("/user/{userId}/profile-photo")
    public ResponseEntity<?> getProfilePhoto(@PathVariable Long userId) {
        try {
            User user = userService.getUserById(userId);

            if (user == null || user.getProfilePhotoUrl() == null) {
                return ResponseEntity.notFound().build();
            }

            String photoUrl = user.getProfilePhotoUrl();

            // If it's a Cloudinary URL (starts with http/https), return it as JSON
            if (photoUrl.startsWith("http://") || photoUrl.startsWith("https://")) {
                Map<String, String> response = new HashMap<>();
                response.put("photoUrl", photoUrl);
                return ResponseEntity.ok(response);
            }

            // Handle legacy local file paths for backward compatibility
            Path filePath;
            if (photoUrl.startsWith("photos/")) {
                filePath = Paths.get(photoUrl);
            } else if (photoUrl.startsWith("/uploads/") || photoUrl.startsWith("uploads/")) {
                String cleanPath = photoUrl.startsWith("/") ? photoUrl.substring(1) : photoUrl;
                filePath = Paths.get(cleanPath);
            } else if (photoUrl.contains("/")) {
                String fileName = photoUrl.substring(photoUrl.lastIndexOf("/") + 1);
                filePath = Paths.get("uploads/profile-photos/" + fileName);
            } else {
                filePath = Paths.get("uploads/profile-photos/" + photoUrl);
            }

            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                String contentType = Files.probeContentType(filePath);
                MediaType mediaType = contentType != null ? MediaType.parseMediaType(contentType) : MediaType.IMAGE_JPEG;

                return ResponseEntity.ok()
                        .contentType(mediaType)
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
