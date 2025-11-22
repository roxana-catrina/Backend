package Licenta.Licenta.Model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "mesaje")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Mesaj {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "expeditor_id", nullable = false)
    private User expeditor;

    @ManyToOne
    @JoinColumn(name = "destinatar_id", nullable = false)
    private User destinatar;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String continut;

    @Column(name = "data_trimitere")
    private LocalDateTime dataTrimitere;

    @Column(nullable = false)
    private Boolean citit = false;

    @Column(name = "data_citire")
    private LocalDateTime dataCitire;

    @PrePersist
    protected void onCreate() {
        dataTrimitere = LocalDateTime.now();
    }
}
