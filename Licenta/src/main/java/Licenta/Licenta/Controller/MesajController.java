package Licenta.Licenta.Controller;

import Licenta.Licenta.Dto.MesajDTO;
import Licenta.Licenta.Service.MesajService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mesaje")
@CrossOrigin(origins = "http://localhost:4200")
public class MesajController {

    @Autowired
    private MesajService mesajService;

    // Trimite mesaj
    @PostMapping("/trimite")
    public ResponseEntity<?> trimiteMesaj(@RequestBody MesajRequest request) {
        System.out.println("\n=========================================");
        System.out.println("=== MESAJ CONTROLLER - TRIMITE MESAJ ===");
        System.out.println("=========================================");
        System.out.println("Request primit: " + request);
        System.out.println("expeditorId: '" + request.getExpeditorId() + "' (length: " +
            (request.getExpeditorId() != null ? request.getExpeditorId().length() : "null") + ")");
        System.out.println("destinatarId: '" + request.getDestinatarId() + "' (length: " +
            (request.getDestinatarId() != null ? request.getDestinatarId().length() : "null") + ")");
        System.out.println("continut: '" + request.getContinut() + "'");

        // Validare rapidă
        if (request.getExpeditorId() == null || request.getExpeditorId().trim().isEmpty()) {
            System.err.println("❌ EROARE: expeditorId este NULL sau EMPTY!");
            return ResponseEntity.badRequest().body(Map.of(
                "error", "expeditorId este obligatoriu și nu poate fi gol",
                "expeditorId", request.getExpeditorId()
            ));
        }

        if (request.getDestinatarId() == null || request.getDestinatarId().trim().isEmpty()) {
            System.err.println("❌ EROARE: destinatarId este NULL sau EMPTY!");
            return ResponseEntity.badRequest().body(Map.of(
                "error", "destinatarId este obligatoriu și nu poate fi gol",
                "destinatarId", request.getDestinatarId()
            ));
        }


        try {
            MesajDTO mesaj = mesajService.trimiteMesaj(
                    request.getExpeditorId(),
                    request.getDestinatarId(),
                    request.getContinut(),
                    request.getTip(),
                    request.getPacientId(),
                    request.getPacientNume(),
                    request.getPacientPrenume(),
                    request.getPacientCnp(),
                    request.getPacientDataNasterii(),
                    request.getPacientSex(),
                    request.getPacientNumarTelefon(),
                    request.getPacientIstoricMedical(),
                    request.getPacientDetalii(),
                    request.getPacientNumarImagini()
            );
            System.out.println("✅ Mesaj trimis cu succes!");
            System.out.println("=========================================\n");
            return ResponseEntity.ok(mesaj);
        } catch (RuntimeException e) {
            System.err.println("\n❌❌❌ EROARE la trimiterea mesajului ❌❌❌");
            System.err.println("Mesaj eroare: " + e.getMessage());
            System.err.println("expeditorId care a cauzat eroarea: '" + request.getExpeditorId() + "'");
            System.err.println("destinatarId care a cauzat eroarea: '" + request.getDestinatarId() + "'");
            e.printStackTrace();
            System.err.println("=========================================\n");

            // Returnează eroare clară către frontend
            return ResponseEntity.status(400).body(Map.of(
                "error", e.getMessage(),
                "expeditorId", request.getExpeditorId() != null ? request.getExpeditorId() : "null",
                "destinatarId", request.getDestinatarId() != null ? request.getDestinatarId() : "null",
                "hint", "Verifică că utilizatorul cu acest ID există în baza de date MongoDB"
            ));
        }
    }

    // Obține conversația între doi utilizatori
    @GetMapping("/conversatie/{user1Id}/{user2Id}")
    public ResponseEntity<List<MesajDTO>> getConversation(
            @PathVariable String user1Id,
            @PathVariable String user2Id) {
        List<MesajDTO> mesaje = mesajService.getConversation(user1Id, user2Id);
        return ResponseEntity.ok(mesaje);
    }

    // Marchează mesaje ca citite
    @PutMapping("/citeste/{userId}/{expeditorId}")
    public ResponseEntity<Void> marcheazaCaCitite(
            @PathVariable String userId,
            @PathVariable String expeditorId) {
        mesajService.marcheazaCaCitite(userId, expeditorId);
        return ResponseEntity.ok().build();
    }

    // Numără mesaje necitite
    @GetMapping("/necitite/{userId}")
    public ResponseEntity<Long> countUnreadMessages(@PathVariable String userId) {
        Long count = mesajService.countUnreadMessages(userId);
        return ResponseEntity.ok(count);
    }

    // Conversații recente
    @GetMapping("/recente/{userId}")
    public ResponseEntity<List<MesajDTO>> getRecentConversations(@PathVariable String userId) {
        List<MesajDTO> conversations = mesajService.getRecentConversations(userId);
        return ResponseEntity.ok(conversations);
    }
}

@Data
class MesajRequest {
    private String expeditorId;
    private String destinatarId;
    private String continut;
    private String tip;
    private String pacientId;
    private String pacientNume;
    private String pacientPrenume;
    private String pacientCnp;
    private String pacientDataNasterii;
    private String pacientSex;
    private String pacientNumarTelefon;
    private String pacientIstoricMedical;
    private String pacientDetalii;
    private Integer pacientNumarImagini;
}