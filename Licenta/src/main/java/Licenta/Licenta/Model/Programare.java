package Licenta.Licenta.Model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "programari")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Programare {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Imagine pacient;

    private String pacientNume;
    private String pacientPrenume;
    private String pacientCnp;

    @Column(nullable = false)
    private LocalDateTime dataProgramare;

    private Integer durataMinute = 30;
    private String tipConsultatie;

    @Enumerated(EnumType.STRING)
    private StatusProgramare status = StatusProgramare.PROGRAMAT;

    @Column(columnDefinition = "TEXT")
    private String detalii;
}