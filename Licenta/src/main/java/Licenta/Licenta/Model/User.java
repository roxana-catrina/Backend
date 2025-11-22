package Licenta.Licenta.Model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Data
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name="utilizatori")
public class User  implements   Serializable{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Long id;

    @Column(name="email")
    private  String email;

    @Column(name="parola")
    private String parola;

    @Column(name="prenume")
    private String prenume;

    @Column(name="nume")
    private String nume;

    @Column(name="data_nasterii")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate data_nasterii;

    @Column(name="sex")
    private String sex;

    @Column(name="numar_telefon")
    private String numar_telefon;

    @Column(name="tara")
    private String tara;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Imagine> images = new ArrayList<>();
    @Column(name = "profile_photo_url", length = 500)
    private String profilePhotoUrl;


}
