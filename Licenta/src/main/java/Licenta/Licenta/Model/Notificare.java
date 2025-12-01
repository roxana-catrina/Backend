package Licenta.Licenta.Model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Document(collection = "notificari")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notificare {
    @Id
    private String id;

    private String userId; // Reference to User ID

    private String tip; // "MESAJ_NOU", "MESAJ_CITIT"

    private String mesajId; // Reference to Mesaj ID

    private String continut;

    private Boolean citit = false;

    private LocalDateTime dataCreare;

    public void onCreate() {
        if (dataCreare == null) {
            dataCreare = LocalDateTime.now();
        }
    }
}