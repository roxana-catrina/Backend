package Licenta.Licenta.Service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendVerificationCode(String toEmail, String code) {
        try {
            System.out.println("EmailService: Preparing to send email to: " + toEmail);

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("noreply@photosolve.com");
            message.setTo(toEmail);
            message.setSubject("PhotoSolve - Cod de Resetare Parolă");
            message.setText(
                    "Bună ziua,\n\n" +
                            "Ați solicitat resetarea parolei pentru contul dumneavoastră PhotoSolve.\n\n" +
                            "Codul dumneavoastră de verificare este: " + code + "\n\n" +
                            "Acest cod este valabil 15 minute.\n\n" +
                            "Dacă nu ați solicitat resetarea parolei, vă rugăm să ignorați acest email.\n\n" +
                            "Cu respect,\n" +
                            "Echipa PhotoSolve"
            );

            System.out.println("EmailService: Sending email...");
            mailSender.send(message);
            System.out.println("EmailService: Email sent successfully!");
        } catch (Exception e) {
            System.err.println("EmailService: Error sending email: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}
