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
    public MesajDTO trimiteMesaj(String expeditorId, String destinatarId, String continut,
                                  String tip, String pacientId, String pacientNume,
                                  String pacientPrenume, String pacientCnp,
                                  String pacientDataNasterii, String pacientSex) {
        // Log pentru debugging
        System.out.println("=== TRIMITE MESAJ ===");
        System.out.println("expeditorId: '" + expeditorId + "' (type: " + (expeditorId != null ? expeditorId.getClass().getName() : "null") + ")");
        System.out.println("destinatarId: '" + destinatarId + "' (type: " + (destinatarId != null ? destinatarId.getClass().getName() : "null") + ")");
        System.out.println("continut: '" + continut + "'");

        // Validate input
        if (expeditorId == null || expeditorId.trim().isEmpty() || "null".equals(expeditorId) || "undefined".equals(expeditorId)) {
            throw new RuntimeException("Invalid expeditorId: " + expeditorId);
        }
        if (destinatarId == null || destinatarId.trim().isEmpty() || "null".equals(destinatarId) || "undefined".equals(destinatarId)) {
            throw new RuntimeException("Invalid destinatarId: " + destinatarId);
        }

        // Find users with better error messages
        User expeditor = userRepository.findById(expeditorId)
                .orElseThrow(() -> new RuntimeException("Expeditor not found with ID: '" + expeditorId + "'. Please check if this user exists in the database."));
        User destinatar = userRepository.findById(destinatarId)
                .orElseThrow(() -> new RuntimeException("Destinatar not found with ID: '" + destinatarId + "'. Please check if this user exists in the database."));

        System.out.println("Expeditor găsit: " + expeditor.getEmail());
        System.out.println("Destinatar găsit: " + destinatar.getEmail());

        Mesaj mesaj = new Mesaj();
        mesaj.setExpeditorId(expeditorId);
        mesaj.setDestinatarId(destinatarId);
        mesaj.setContinut(continut);
        mesaj.setCitit(false);
        mesaj.setTip(tip);
        mesaj.setPacientId(pacientId);
        mesaj.setPacientNume(pacientNume);
        mesaj.setPacientPrenume(pacientPrenume);
        mesaj.setPacientCnp(pacientCnp);
        mesaj.setPacientDataNasterii(pacientDataNasterii);
        mesaj.setPacientSex(pacientSex);
        mesaj.onCreate(); // Set timestamp

        Mesaj savedMesaj = mesajRepository.save(mesaj);
        System.out.println("Mesaj salvat cu ID: " + savedMesaj.getId());

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
        dto.setTip(mesaj.getTip());
        dto.setPacientId(mesaj.getPacientId());
        dto.setPacientNume(mesaj.getPacientNume());
        dto.setPacientPrenume(mesaj.getPacientPrenume());
        dto.setPacientCnp(mesaj.getPacientCnp());
        dto.setPacientDataNasterii(mesaj.getPacientDataNasterii());
        dto.setPacientSex(mesaj.getPacientSex());
        return dto;
    }
}
