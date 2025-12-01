package Licenta.Licenta.Controller;

import Licenta.Licenta.Dto.NotificareDTO;
import Licenta.Licenta.Service.NotificareService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notificari")
@CrossOrigin(origins = "http://localhost:4200")
public class NotificareController {

    @Autowired
    private NotificareService notificareService;

    // Obține toate notificările pentru un utilizator
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<NotificareDTO>> getNotificari(@PathVariable String userId) {
        List<NotificareDTO> notificari = notificareService.getNotificariByUserId(userId);
        return ResponseEntity.ok(notificari);
    }

    // Obține notificările necitite
    @GetMapping("/user/{userId}/necitite")
    public ResponseEntity<List<NotificareDTO>> getNotificariNecitite(@PathVariable String userId) {
        List<NotificareDTO> notificari = notificareService.getNotificariNecititeByUserId(userId);
        return ResponseEntity.ok(notificari);
    }

    // Numără notificările necitite
    @GetMapping("/user/{userId}/count")
    public ResponseEntity<Long> countNotificariNecitite(@PathVariable String userId) {
        Long count = notificareService.countNotificariNecitite(userId);
        return ResponseEntity.ok(count);
    }

    // Marchează o notificare ca citită
    @PutMapping("/{notificareId}/citeste")
    public ResponseEntity<Void> marcheazaCaCitita(@PathVariable String notificareId) {
        notificareService.marcheazaCaCitita(notificareId);
        return ResponseEntity.ok().build();
    }

    // Marchează toate notificările ca citite
    @PutMapping("/user/{userId}/citeste-toate")
    public ResponseEntity<Void> marcheazaToateCaCitite(@PathVariable String userId) {
        notificareService.marcheazaToateCaCitite(userId);
        return ResponseEntity.ok().build();
    }

    // Șterge o notificare
    @DeleteMapping("/{notificareId}")
    public ResponseEntity<Void> stergeNotificare(@PathVariable String notificareId) {
        notificareService.stergeNotificare(notificareId);
        return ResponseEntity.ok().build();
    }

    // Șterge toate notificările pentru un utilizator
    @DeleteMapping("/user/{userId}")
    public ResponseEntity<Void> stergeToateNotificarile(@PathVariable String userId) {
        notificareService.stergeToateNotificarileUserId(userId);
        return ResponseEntity.ok().build();
    }
}
