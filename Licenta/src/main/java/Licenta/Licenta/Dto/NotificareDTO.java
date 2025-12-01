package Licenta.Licenta.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificareDTO {
    private String id;
    private String userId;
    private String tip;
    private String mesajId;
    private String continut;
    private Boolean citit;
    private LocalDateTime dataCreare;

    // Info despre expeditor (pentru notificări de mesaje)
    private String expeditorId;
    private String expeditorNume;
    private String expeditorPrenume;
}
