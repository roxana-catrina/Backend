package Licenta.Licenta.Dto;

import Licenta.Licenta.Model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Base64;

@Data
@AllArgsConstructor
public class ImagineDto {
    private Long id;
    private String imagine;
    private String nume;
    private String tip;
}
