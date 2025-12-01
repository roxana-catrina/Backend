package Licenta.Licenta.Service;

import Licenta.Licenta.Dto.MesajDTO;
import Licenta.Licenta.Model.Mesaj;
import Licenta.Licenta.Model.User;
import Licenta.Licenta.Repository.MesajRepository;
import Licenta.Licenta.Repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public MesajDTO trimiteMesaj(String expeditorId, String destinatarId, String continut) {
        User expeditor = userRepository.findById(expeditorId)
                .orElseThrow(() -> new RuntimeException("Expeditor not found"));
        User destinatar = userRepository.findById(destinatarId)
                .orElseThrow(() -> new RuntimeException("Destinatar not found"));

        Mesaj mesaj = new Mesaj();
        mesaj.setExpeditorId(expeditorId);
        mesaj.setDestinatarId(destinatarId);
        mesaj.setContinut(continut);
        mesaj.setCitit(false);
        mesaj.onCreate(); // Set timestamp

        Mesaj savedMesaj = mesajRepository.save(mesaj);

        // Trimite notificare prin WebSocket
        MesajDTO mesajDTO = convertToDTO(savedMesaj, expeditor, destinatar);
        messagingTemplate.convertAndSendToUser(
                destinatarId,
                "/queue/messages",
                mesajDTO
        );

        // Creează notificare în baza de date
        notificareService.creeazaNotificareMesaj(destinatar, savedMesaj, expeditor);

        return mesajDTO;
    }

    // Obține istoricul conversației
    public List<MesajDTO> getConversation(String user1Id, String user2Id) {
        List<Mesaj> mesaje = mesajRepository.findConversation(user1Id, user2Id);
        return mesaje.stream()
                .map(mesaj -> {
                    User expeditor = userRepository.findById(mesaj.getExpeditorId()).orElse(null);
                    User destinatar = userRepository.findById(mesaj.getDestinatarId()).orElse(null);
                    return convertToDTO(mesaj, expeditor, destinatar);
                })
                .collect(Collectors.toList());
    }

    // Marchează mesajele ca citite
    public void marcheazaCaCitite(String userId, String expeditorId) {
        List<Mesaj> mesaje = mesajRepository.findByDestinatarIdAndExpeditorIdAndCitit(userId, expeditorId, false);
        LocalDateTime now = LocalDateTime.now();

        mesaje.forEach(mesaj -> {
            mesaj.setCitit(true);
            mesaj.setDataCitire(now);
        });

        mesajRepository.saveAll(mesaje);

        // Notifică expeditorul că mesajele au fost citite
        messagingTemplate.convertAndSendToUser(
                expeditorId,
                "/queue/read-receipts",
                Map.of("userId", userId, "count", mesaje.size())
        );
    }

    // Numără mesaje necitite
    public Long countUnreadMessages(String userId) {
        return mesajRepository.countByDestinatarIdAndCitit(userId, false);
    }

    // Conversații recente - get all messages for a user
    public List<MesajDTO> getRecentConversations(String userId) {
        List<Mesaj> mesaje = mesajRepository.findByDestinatarIdOrderByDataTrimitereDesc(userId);
        return mesaje.stream()
                .map(mesaj -> {
                    User expeditor = userRepository.findById(mesaj.getExpeditorId()).orElse(null);
                    User destinatar = userRepository.findById(mesaj.getDestinatarId()).orElse(null);
                    return convertToDTO(mesaj, expeditor, destinatar);
                })
                .collect(Collectors.toList());
    }

    private MesajDTO convertToDTO(Mesaj mesaj, User expeditor, User destinatar) {
        MesajDTO dto = new MesajDTO();
        dto.setId(mesaj.getId());
        dto.setExpeditorId(mesaj.getExpeditorId());
        if (expeditor != null) {
            dto.setExpeditorNume(expeditor.getNume());
            dto.setExpeditorPrenume(expeditor.getPrenume());
        }
        dto.setDestinatarId(mesaj.getDestinatarId());
        if (destinatar != null) {
            dto.setDestinatarNume(destinatar.getNume());
            dto.setDestinatarPrenume(destinatar.getPrenume());
        }
        dto.setContinut(mesaj.getContinut());
        dto.setDataTrimitere(mesaj.getDataTrimitere());
        dto.setCitit(mesaj.getCitit());
        dto.setDataCitire(mesaj.getDataCitire());
        return dto;
    }
}
