package Licenta.Licenta.Dto;

import Licenta.Licenta.Model.Sex;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PacientDto {
    private String id;
    private String numePacient;
    private String prenumePacient;
    private Sex sex;
    private String detalii;
    private LocalDate dataNasterii;
    private String cnp;
    private String numarTelefon;
    private String istoricMedical;
    private List<ImagineDto> imagini;
}

