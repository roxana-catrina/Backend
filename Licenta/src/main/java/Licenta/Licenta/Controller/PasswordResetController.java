package Licenta.Licenta.Controller;


import Licenta.Licenta.Dto.PasswordResetRequest;
import Licenta.Licenta.Dto.PasswordResetVerifyRequest;
import Licenta.Licenta.Service.PasswordResetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/password-reset")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class PasswordResetController {

    @Autowired
    private PasswordResetService passwordResetService;

    @PostMapping("/send-code")
    public ResponseEntity<Map<String, String>> sendVerificationCode(@RequestBody PasswordResetRequest request) {
        Map<String, String> response = new HashMap<>();

        try {
            // Validare input
            if (request == null || request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                response.put("message", "Email-ul este obligatoriu!");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            System.out.println("Attempting to send verification code to: " + request.getEmail());
            passwordResetService.sendVerificationCode(request.getEmail());
            response.put("message", "Codul de verificare a fost trimis pe email!");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Error in sendVerificationCode: " + e.getMessage());
            e.printStackTrace();
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PostMapping("/verify-and-reset")
    public ResponseEntity<Map<String, String>> verifyAndResetPassword(@RequestBody PasswordResetVerifyRequest request) {
        Map<String, String> response = new HashMap<>();

        try {
            passwordResetService.verifyAndResetPassword(
                    request.getEmail(),
                    request.getCode(),
                    request.getNewPassword()
            );
            response.put("message", "Parola a fost schimbată cu succes!");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
}
