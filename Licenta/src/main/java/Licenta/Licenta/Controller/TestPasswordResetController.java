package Licenta.Licenta.Controller;

import Licenta.Licenta.Repository.UserRepository;
import Licenta.Licenta.Model.User;
import Licenta.Licenta.Service.PasswordResetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Controller temporar pentru debugging password reset
 * ȘTERGE DUPĂ CE REZOLVI PROBLEMA!
 */
@RestController
@RequestMapping("/api/test")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class TestPasswordResetController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private PasswordResetService passwordResetService;

    /**
     * Test 1: Verifică dacă user-ul există în baza de date
     */
    @GetMapping("/check-user/{email}")
    public ResponseEntity<Map<String, Object>> checkUser(@PathVariable String email) {
        Map<String, Object> response = new HashMap<>();

        try {
            Optional<User> userOpt = userRepository.findByEmail(email);

            if (userOpt.isPresent()) {
                User user = userOpt.get();
                response.put("exists", true);
                response.put("userId", user.getId());
                response.put("email", user.getEmail());
                response.put("nume", user.getNume());
                response.put("prenume", user.getPrenume());
                response.put("message", "✅ User găsit în baza de date!");
            } else {
                response.put("exists", false);
                response.put("message", "❌ User-ul NU există în baza de date!");
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", e.getMessage());
            response.put("message", "❌ Eroare la verificare: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Test 1.5: Obține ultimul cod de resetare parola pentru un email
     * ⚠️ DOAR PENTRU TESTING! NU folosi în producție!
     */
    @GetMapping("/password-reset-code/{email}")
    public ResponseEntity<Map<String, String>> getResetCode(@PathVariable String email) {
        Map<String, String> response = new HashMap<>();

        try {
            // Returnează ultimul cod generat pentru acest email
            String code = passwordResetService.getLastCodeForEmail(email);

            if (code != null) {
                response.put("code", code);
                response.put("email", email);
                response.put("message", "✅ Cod găsit!");
                return ResponseEntity.ok(response);
            } else {
                response.put("email", email);
                response.put("message", "❌ Nu există niciun cod generat pentru acest email!");
                return ResponseEntity.status(404).body(response);
            }
        } catch (Exception e) {
            response.put("error", e.getMessage());
            response.put("message", "❌ Eroare la obținerea codului: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Test 2: Testează conexiunea SMTP la Gmail
     */
    @GetMapping("/test-email/{toEmail}")
    public ResponseEntity<Map<String, String>> testEmail(@PathVariable String toEmail) {
        Map<String, String> response = new HashMap<>();

        try {
            System.out.println("🧪 TEST: Attempting to send test email to: " + toEmail);

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("noreply@photosolve.com");
            message.setTo(toEmail);
            message.setSubject("Test Email - PhotoSolve");
            message.setText("Acesta este un email de test. Dacă primești acest email, configurația Gmail funcționează corect!");

            mailSender.send(message);

            System.out.println("✅ TEST: Email sent successfully!");
            response.put("message", "✅ Email trimis cu succes! Verifică inbox-ul.");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ TEST: Email failed: " + e.getMessage());
            e.printStackTrace();
            response.put("message", "❌ Eroare la trimiterea email-ului: " + e.getMessage());
            return ResponseEntity.status(400).body(response);
        }
    }

    /**
     * Test 3: Info despre configurația curentă
     */
    @GetMapping("/config-info")
    public ResponseEntity<Map<String, String>> configInfo() {
        Map<String, String> response = new HashMap<>();

        response.put("message", "Verifică console-ul backend pentru detalii despre configurație");
        response.put("instruction", "Verifică fișierul application.properties pentru Gmail settings");

        System.out.println("=== GMAIL CONFIG INFO ===");
        System.out.println("spring.mail.host=smtp.gmail.com");
        System.out.println("spring.mail.port=587");
        System.out.println("⚠️ Verifică că spring.mail.password este App Password (16 caractere)!");
        System.out.println("========================");

        return ResponseEntity.ok(response);
    }
}

