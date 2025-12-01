package Licenta.Licenta.Model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Data
@Document(collection = "utilizatori")
public class User implements Serializable {
    @Id
    private String id;

    private String email;

    private String parola;

    private String prenume;

    private String nume;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate data_nasterii;

    private String sex;

    private String numar_telefon;

    private String tara;

    @JsonIgnore
    private List<String> imageIds = new ArrayList<>(); // References to Imagine IDs

    private String profilePhotoUrl;

}
