package Licenta.Licenta.Model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "notificari")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notificare {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String tip; // "MESAJ_NOU", "MESAJ_CITIT"

    @ManyToOne
    @JoinColumn(name = "mesaj_id")
    private Mesaj mesaj;

    @Column(columnDefinition = "TEXT")
    private String continut;

    @Column(nullable = false)
    private Boolean citit = false;

    @Column(name = "data_creare")
    private LocalDateTime dataCreare;

    @PrePersist
    protected void onCreate() {
        dataCreare = LocalDateTime.now();
    }
}