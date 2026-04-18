package Licenta.Licenta.Service;


import Licenta.Licenta.Model.PasswordResetToken;
import Licenta.Licenta.Model.User;
import Licenta.Licenta.Repository.PasswordResetTokenRepository;
import Licenta.Licenta.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class PasswordResetService {

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final int CODE_LENGTH = 6;
    private static final int EXPIRY_MINUTES = 15;

    @Transactional
    public void sendVerificationCode(String email) throws Exception {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (!userOpt.isPresent()) {
            throw new Exception("Nu exista niciun cont cu acest email!");
        }
        try {
            tokenRepository.deleteByEmail(email);

        } catch (Exception e) {
            System.err.println("Eroare token: " + e.getMessage());
        }
        String code = generateVerificationCode();

        PasswordResetToken token = new PasswordResetToken(
                email,
                code,
                LocalDateTime.now().plusMinutes(EXPIRY_MINUTES)
        );
        tokenRepository.save(token);
        try {
            emailService.sendVerificationCode(email, code);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Eroare la trimiterea email-ului: " + e.getMessage());
        }
    }

    @Transactional
    public void verifyAndResetPassword(String email, String code, String newPassword) throws Exception {
        // Găsește token-ul
        Optional<PasswordResetToken> tokenOpt = tokenRepository.findByEmailAndCodeAndUsedFalse(email, code);

        if (!tokenOpt.isPresent()) {
            throw new Exception("Cod invalid sau expirat!");
        }

        PasswordResetToken token = tokenOpt.get();

        // Verifică dacă token-ul este expirat
        if (token.isExpired()) {
            throw new Exception("Codul a expirat! Solicită un cod nou.");
        }

        // Găsește utilizatorul
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (!userOpt.isPresent()) {
            throw new Exception("Utilizatorul nu există!");
        }

        User user = userOpt.get();

        // Actualizează parola
        user.setParola(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Marchează token-ul ca folosit
        token.setUsed(true);
        tokenRepository.save(token);
    }

    private String generateVerificationCode() {
        SecureRandom random = new SecureRandom();
        int code = random.nextInt(999999);
        return String.format("%06d", code);
    }

    @Transactional
    public void cleanupExpiredTokens() {
        tokenRepository.deleteByExpiryDateBefore(LocalDateTime.now());
    }

    /**
     * Metoda pentru a obține ultimul cod generat pentru un email (doar pentru testing!)
     * NU trebuie folosită în producție!
     */
    public String getLastCodeForEmail(String email) {
        Optional<PasswordResetToken> tokenOpt = tokenRepository.findTopByEmailOrderByExpiryDateDesc(email);
        if (tokenOpt.isPresent()) {
            return tokenOpt.get().getCode();
        }
        return null;
    }
}