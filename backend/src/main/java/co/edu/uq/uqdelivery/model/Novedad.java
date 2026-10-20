package co.edu.uq.uqdelivery.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "novedades")
@Getter @Setter @NoArgsConstructor
public class Novedad {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "envio_id", nullable = false)
    private Envio envio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repartidor_id", nullable = false)
    private Usuario repartidor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(30)")
    private TipoNovedad tipo;

    @Column(nullable = false, length = 500)
    private String descripcion;

    private Double latitud;
    private Double longitud;
    private LocalDateTime creadoEn = LocalDateTime.now();
}
