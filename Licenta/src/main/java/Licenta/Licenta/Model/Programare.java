package Licenta.Licenta.Model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Document(collection = "programari")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Programare {

    @Id
    private String id;

    private String pacientId; // Reference to Pacient ID

    private String pacientNume;
    private String pacientPrenume;
    private String pacientCnp;

    private LocalDateTime dataProgramare;

    private Integer durataMinute = 30;
    private String tipConsultatie;

    private StatusProgramare status = StatusProgramare.PROGRAMAT;

    private String detalii;
}