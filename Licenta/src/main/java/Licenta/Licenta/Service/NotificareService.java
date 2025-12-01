package Licenta.Licenta.Service;

import Licenta.Licenta.Dto.NotificareDTO;
import Licenta.Licenta.Model.Mesaj;
import Licenta.Licenta.Model.Notificare;
import Licenta.Licenta.Model.User;
import Licenta.Licenta.Repository.NotificareRepository;
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
public class NotificareService {

    @Autowired
    private NotificareRepository notificareRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public List<NotificareDTO> getNotificariByUserId(String userId) {
        return notificareRepository.findByUserIdOrderByDataCreareDesc(userId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<NotificareDTO> getNotificariNecititeByUserId(String userId) {
        return notificareRepository.findByUserIdAndCititOrderByDataCreareDesc(userId, false)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Long countNotificariNecitite(String userId) {
        return notificareRepository.countByUserIdAndCitit(userId, false);
    }

    public void marcheazaCaCitita(String notificareId) {
        Notificare notificare = notificareRepository.findById(notificareId)
                .orElseThrow(() -> new RuntimeException("Notificare not found"));
        notificare.setCitit(true);
        notificareRepository.save(notificare);
    }

    public void marcheazaToateCaCitite(String userId) {
        List<Notificare> notificari = notificareRepository.findByUserIdAndCititOrderByDataCreareDesc(userId, false);
        notificari.forEach(n -> n.setCitit(true));
        notificareRepository.saveAll(notificari);
    }

    public void stergeNotificare(String notificareId) {
        notificareRepository.deleteById(notificareId);
    }

    public void stergeToateNotificarileUserId(String userId) {
        List<Notificare> notificari = notificareRepository.findByUserIdOrderByDataCreareDesc(userId);
        notificareRepository.deleteAll(notificari);
    }

    // Șterge notificări mai vechi de 30 zile
    public void stergeNotificariVechi() {
        LocalDateTime dataLimita = LocalDateTime.now().minusDays(30);
        notificareRepository.deleteByUserIdAndCititAndDataCreareBefore(null, true, dataLimita);
    }

    private NotificareDTO convertToDTO(Notificare notificare) {
        NotificareDTO dto = new NotificareDTO();
        dto.setId(notificare.getId());
        dto.setUserId(notificare.getUserId());
        dto.setTip(notificare.getTip());
        dto.setContinut(notificare.getContinut());
        dto.setCitit(notificare.getCitit());
        dto.setDataCreare(notificare.getDataCreare());

        if (notificare.getMesajId() != null) {
            dto.setMesajId(notificare.getMesajId());
            // Note: expeditorId, expeditorNume, expeditorPrenume would need to be fetched separately if needed
        }

        return dto;
    }

    public void creeazaNotificareMesaj(User destinatar, Mesaj mesaj, User expeditor) {
        Notificare notificare = new Notificare();
        notificare.setUserId(destinatar.getId());
        notificare.setTip("MESAJ_NOU");
        notificare.setMesajId(mesaj.getId());
        notificare.setContinut("Mesaj nou de la " +
                expeditor.getPrenume() + " " +
                expeditor.getNume());
        notificare.setCitit(false);
        notificare.onCreate(); // Set timestamp

        notificareRepository.save(notificare);

        // Trimite notificare în timp real
        messagingTemplate.convertAndSendToUser(
                destinatar.getId(),
                "/queue/notifications",
                Map.of(
                        "tip", "MESAJ_NOU",
                        "continut", notificare.getContinut(),
                        "mesajId", mesaj.getId(),
                        "expeditorId", expeditor.getId()
                )
        );
    }

}