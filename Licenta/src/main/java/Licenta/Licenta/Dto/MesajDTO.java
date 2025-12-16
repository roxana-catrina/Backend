package Licenta.Licenta.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MesajDTO {
    private String id;
    private String expeditorId;
    private String expeditorNume;
    private String expeditorPrenume;
    private String destinatarId;
    private String destinatarNume;
    private String destinatarPrenume;
    private String continut;
    private LocalDateTime dataTrimitere;
    private Boolean citit;

    private LocalDateTime dataCitire;
    private String tip = "text";
    private String pacientId;
    private String pacientNume;
    private String pacientPrenume;
    private String pacientCnp;
    private String pacientDataNasterii;
    private String pacientSex;


    private String pacientNumarTelefon;
    private String pacientIstoricMedical;
    private String pacientDetalii;
    private Integer pacientNumarImagini;

    private String pacientImagini;
}
