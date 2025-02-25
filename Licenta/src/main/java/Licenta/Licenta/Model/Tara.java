package Licenta.Licenta.Model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name="tari")
public class Tara {
    @Id@GeneratedValue(strategy = GenerationType.IDENTITY)
    private  Long  id;

    @Column(name="nume")
    private String nume;

    @Column(name="cod")
    private String cod;

    @Column(name="prefix")
    private String prefix;
}
