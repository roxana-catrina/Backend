package Licenta.Licenta.Model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Data
@Entity
@Table(name="imagini")

public class Imagine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    private byte[] imagine;

    @ManyToOne
    @JoinColumn(name="user_id")
    private User user;

    @Column(name="nume")
    private String nume;

    @Column(name="tip")
    private String tip;

    @Column(name="image_url")
    private String imageUrl;

     @Column(name="cloudinary_public_id")
    private String cloudinaryPublicId;

     @Column(name="nume_pacient")
     private String numePacient;
    @Column(name="prenume_pacient")
     private String prenumePacient;
    @Enumerated(EnumType.STRING)
    @Column(name="sex")
     private Sex sex;
    @Column(name="detalii")
     private String detalii;
    @Column(name="data_nasterii")
     private LocalDate dataNasterii;
    @Column(name="cnp")
     private String cnp;
    @Column(name="numar_telefon")
     private String numarTelefon;
    @Column(name="istoric_medical")
    private String istoricMedical;
}


