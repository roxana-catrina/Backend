package Licenta.Licenta.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProgramareDTO {
    private Long userId;
    private String pacientNume;
    private String pacientPrenume;
    private String pacientCnp;
    private LocalDateTime dataProgramare;
    private Integer durataMinute;
    private String tipConsultatie;
    private String detalii;
}
