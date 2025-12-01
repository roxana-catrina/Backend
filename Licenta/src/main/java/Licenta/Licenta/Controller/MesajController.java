package Licenta.Licenta.Controller;

import Licenta.Licenta.Dto.MesajDTO;
import Licenta.Licenta.Service.MesajService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mesaje")
@CrossOrigin(origins = "http://localhost:4200")
public class MesajController {

    @Autowired
    private MesajService mesajService;

    // Trimite mesaj
    @PostMapping("/trimite")
    public ResponseEntity<MesajDTO> trimiteMesaj(@RequestBody MesajRequest request) {
        MesajDTO mesaj = mesajService.trimiteMesaj(
                request.getExpeditorId(),
                request.getDestinatarId(),
                request.getContinut()
        );
        return ResponseEntity.ok(mesaj);
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
}