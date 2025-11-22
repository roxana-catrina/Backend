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

    public List<NotificareDTO> getNotificariByUserId(Long userId) {
        return notificareRepository.findByUserId(userId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<NotificareDTO> getNotificariNecititeByUserId(Long userId) {
        return notificareRepository.findUnreadByUserId(userId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Long countNotificariNecitite(Long userId) {
        return notificareRepository.countUnreadByUserId(userId);
    }

    public void marcheazaCaCitita(Long notificareId) {
        Notificare notificare = notificareRepository.findById(notificareId)
                .orElseThrow(() -> new RuntimeException("Notificare not found"));
        notificare.setCitit(true);
        notificareRepository.save(notificare);
    }

    public void marcheazaToateCaCitite(Long userId) {
        notificareRepository.markAllAsReadByUserId(userId);
    }

    public void stergeNotificare(Long notificareId) {
        notificareRepository.deleteById(notificareId);
    }

    public void stergeToateNotificarileUserId(Long userId) {
        List<Notificare> notificari = notificareRepository.findByUserId(userId);
        notificareRepository.deleteAll(notificari);
    }

    // Șterge notificări mai vechi de 30 zile
    public void stergeNotificariVechi() {
        LocalDateTime dataLimita = LocalDateTime.now().minusDays(30);
        notificareRepository.deleteOldNotifications(dataLimita);
    }

    private NotificareDTO convertToDTO(Notificare notificare) {
        NotificareDTO dto = new NotificareDTO();
        dto.setId(notificare.getId());
        dto.setUserId(notificare.getUser().getId());
        dto.setTip(notificare.getTip());
        dto.setContinut(notificare.getContinut());
        dto.setCitit(notificare.getCitit());
        dto.setDataCreare(notificare.getDataCreare());

        if (notificare.getMesaj() != null) {
            dto.setMesajId(notificare.getMesaj().getId());
            dto.setExpeditorId(notificare.getMesaj().getExpeditor().getId());
            dto.setExpeditorNume(notificare.getMesaj().getExpeditor().getNume());
            dto.setExpeditorPrenume(notificare.getMesaj().getExpeditor().getPrenume());
        }

        return dto;
    }



    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public void creeazaNotificareMesaj(User destinatar, Mesaj mesaj) {
        Notificare notificare = new Notificare();
        notificare.setUser(destinatar);
        notificare.setTip("MESAJ_NOU");
        notificare.setMesaj(mesaj);
        notificare.setContinut("Mesaj nou de la " +
                mesaj.getExpeditor().getPrenume() + " " +
                mesaj.getExpeditor().getNume());
        notificare.setCitit(false);

        notificareRepository.save(notificare);

        // Trimite notificare în timp real
        messagingTemplate.convertAndSendToUser(
                destinatar.getId().toString(),
                "/queue/notifications",
                Map.of(
                        "tip", "MESAJ_NOU",
                        "continut", notificare.getContinut(),
                        "mesajId", mesaj.getId(),
                        "expeditorId", mesaj.getExpeditor().getId()
                )
        );
    }

}