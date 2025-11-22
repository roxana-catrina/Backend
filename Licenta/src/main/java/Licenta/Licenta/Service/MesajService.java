package Licenta.Licenta.Service;

import Licenta.Licenta.Dto.MesajDTO;
import Licenta.Licenta.Model.Mesaj;
import Licenta.Licenta.Model.User;
import Licenta.Licenta.Repository.MesajRepository;
import Licenta.Licenta.Repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class MesajService {

    @Autowired
    private MesajRepository mesajRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private NotificareService notificareService;

    // Trimite mesaj
    public MesajDTO trimiteMesaj(Long expeditorId, Long destinatarId, String continut) {
        User expeditor = userRepository.findById(expeditorId)
                .orElseThrow(() -> new RuntimeException("Expeditor not found"));
        User destinatar = userRepository.findById(destinatarId)
                .orElseThrow(() -> new RuntimeException("Destinatar not found"));

        Mesaj mesaj = new Mesaj();
        mesaj.setExpeditor(expeditor);
        mesaj.setDestinatar(destinatar);
        mesaj.setContinut(continut);
        mesaj.setCitit(false);

        Mesaj savedMesaj = mesajRepository.save(mesaj);

        // Trimite notificare prin WebSocket
        MesajDTO mesajDTO = convertToDTO(savedMesaj);
        messagingTemplate.convertAndSendToUser(
                destinatarId.toString(),
                "/queue/messages",
                mesajDTO
        );

        // Creează notificare în baza de date
        notificareService.creeazaNotificareMesaj(destinatar, savedMesaj);

        return mesajDTO;
    }

    // Obține istoricul conversației
    public List<MesajDTO> getConversation(Long user1Id, Long user2Id) {
        List<Mesaj> mesaje = mesajRepository.findConversation(user1Id, user2Id);
        return mesaje.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Marchează mesajele ca citite
    public void marcheazaCaCitite(Long userId, Long expeditorId) {
        List<Mesaj> mesaje = mesajRepository.findUnreadMessagesFrom(userId, expeditorId);
        LocalDateTime now = LocalDateTime.now();

        mesaje.forEach(mesaj -> {
            mesaj.setCitit(true);
            mesaj.setDataCitire(now);
        });

        mesajRepository.saveAll(mesaje);

        // Notifică expeditorul că mesajele au fost citite
        messagingTemplate.convertAndSendToUser(
                expeditorId.toString(),
                "/queue/read-receipts",
                Map.of("userId", userId, "count", mesaje.size())
        );
    }

    // Numără mesaje necitite
    public Long countUnreadMessages(Long userId) {
        return mesajRepository.countUnreadMessages(userId);
    }

    // Conversații recente
    public List<MesajDTO> getRecentConversations(Long userId) {
        List<Mesaj> mesaje = mesajRepository.findRecentConversations(userId);
        return mesaje.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private MesajDTO convertToDTO(Mesaj mesaj) {
        MesajDTO dto = new MesajDTO();
        dto.setId(mesaj.getId());
        dto.setExpeditorId(mesaj.getExpeditor().getId());
        dto.setExpeditorNume(mesaj.getExpeditor().getNume());
        dto.setExpeditorPrenume(mesaj.getExpeditor().getPrenume());
        dto.setDestinatarId(mesaj.getDestinatar().getId());
        dto.setDestinatarNume(mesaj.getDestinatar().getNume());
        dto.setDestinatarPrenume(mesaj.getDestinatar().getPrenume());
        dto.setContinut(mesaj.getContinut());
        dto.setDataTrimitere(mesaj.getDataTrimitere());
        dto.setCitit(mesaj.getCitit());
        dto.setDataCitire(mesaj.getDataCitire());
        return dto;
    }
}
