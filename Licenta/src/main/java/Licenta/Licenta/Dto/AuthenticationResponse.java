package Licenta.Licenta.Dto;

import lombok.Data;

@Data
public class AuthenticationResponse {
    private String jwt;

    private String id;
    private String nume;
}
