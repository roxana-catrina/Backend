package Licenta.Licenta.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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

}
