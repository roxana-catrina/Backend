package Licenta.Licenta.Model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;

@Document(collection = "tari")
@Data
public class Tara {
    @Id
    private String id;

    private String nume;

    private String cod;

    private String prefix;
}
