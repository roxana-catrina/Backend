package Licenta.Licenta.Model;

import jakarta.persistence.*;
import lombok.Data;

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
}
