package Licenta.Licenta.Model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Document(collection = "mesaje")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Mesaj {
    @Id
    private String id;

    private String expeditorId; // Reference to User ID

    private String destinatarId; // Reference to User ID

    private String continut;

    private LocalDateTime dataTrimitere;

    private Boolean citit = false;

    private LocalDateTime dataCitire;

    public void onCreate() {
        if (dataTrimitere == null) {
            dataTrimitere = LocalDateTime.now();
        }
    }


    private String tip = "text"; // "text", "pacient_partajat"


    private String pacientId; // ID-ul pacientului partajat (nullable)


    private String pacientNume;


    private String pacientPrenume;


    private String pacientCnp;


    private String pacientDataNasterii;


    private String pacientSex;



    private String pacientNumarTelefon;

    private String pacientIstoricMedical;

    private String pacientDetalii;

    private Integer pacientNumarImagini;
}
