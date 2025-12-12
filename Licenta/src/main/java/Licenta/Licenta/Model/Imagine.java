package Licenta.Licenta.Model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;

import java.util.Date;
import java.util.Map;

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

    // Câmpuri noi pentru analiză
    private Boolean areTumoare;
    private String tipTumoare;
    private Integer confidenta; // 0-100
    private Date dataAnalizei;
    private String statusAnaliza; // "neanalizata", "in_procesare", "finalizata", "eroare"

    // Observații
    private String observatii;
    private Date dataIncarcare;
    private Date dataModificare;

    // ===== ADAUGĂ ACESTEA =====
    private Boolean isDicom = false;
    private Map<String, Object> dicomMetadata; // MongoDB stochează direct ca subdocument
    // ==========================

}


