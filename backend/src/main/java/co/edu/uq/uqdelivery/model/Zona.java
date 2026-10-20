package co.edu.uq.uqdelivery.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "zonas")
@Getter @Setter @NoArgsConstructor
public class Zona {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    private double latitud;
    private double longitud;
    private boolean activa = true;

    public Zona(String nombre, double latitud, double longitud, boolean activa) {
        this.nombre = nombre;
        this.latitud = latitud;
        this.longitud = longitud;
        this.activa = activa;
    }
}
