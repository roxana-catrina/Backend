package Licenta.Licenta.Model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DBRef;
import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@Document(collection = "pacienti")
public class Pacient {
    @Id
    private String id;

    private String userId; // Reference to User ID

    private String numePacient;

    private String prenumePacient;

    private Sex sex;

    private String detalii;

    private LocalDate dataNasterii;

    private String cnp;

    private String numarTelefon;

    private String istoricMedical;

    // Images will be stored as embedded documents or references
    @DBRef
    private List<Imagine> imagini = new ArrayList<>();
}

