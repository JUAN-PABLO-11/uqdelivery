package co.edu.uq.uqdelivery.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "usuarios")
@Getter @Setter @NoArgsConstructor
public class Usuario {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, unique = true, length = 150)
    private String correo;

    @Column(nullable = false)
    private String passwordHash;

    // columnDefinition: se guarda como VARCHAR (no ENUM de MySQL) para poder agregar valores después
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(20)")
    private Rol rol;

    private boolean activo = true;
    private int intentosFallidos = 0;      // HU-01
    private LocalDateTime bloqueadoHasta;  // HU-01
    private boolean disponible = true;     // solo aplica a repartidores (HU-06)

    public Usuario(String nombre, String correo, String passwordHash, Rol rol) {
        this.nombre = nombre;
        this.correo = correo;
        this.passwordHash = passwordHash;
        this.rol = rol;
    }
}
