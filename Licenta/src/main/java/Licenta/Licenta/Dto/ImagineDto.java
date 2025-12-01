package Licenta.Licenta.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ImagineDto {
    private String id;
    private String pacientId;
    private String imageUrl;
    private String nume;
    private String tip;
    private String cloudinaryPublicId;
    private Boolean areTumoare;
    private String tipTumoare;
    private Integer confidenta; // 0-100
    private Date dataAnalizei;
    private String statusAnaliza; // "neanalizata", "in_procesare", "finalizata", "eroare"

    // Observații
    private String observatii;
    private Date dataIncarcare;
    private Date dataModificare;


}
