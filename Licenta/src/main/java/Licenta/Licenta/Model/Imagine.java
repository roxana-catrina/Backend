package Licenta.Licenta.Model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;

@Data
@Document(collection = "imagini")
public class Imagine {
    @Id
    private String id;

    private String pacientId; // Reference to Pacient ID

    private String nume;

    private String tip;

    private String imageUrl;

    private String cloudinaryPublicId;

}


