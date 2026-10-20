package co.edu.uq.uqdelivery.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** Cada asignación o reasignación de un envío. Solo una por envío tiene activa = true. */
@Entity
@Table(name = "asignaciones")
@Getter @Setter @NoArgsConstructor
public class Asignacion {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "envio_id", nullable = false)
    private Envio envio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repartidor_id", nullable = false)
    private Usuario repartidor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asignado_por", nullable = false)
    private Usuario asignadoPor;

    private String motivo;
    private boolean activa = true;
    private LocalDateTime creadoEn = LocalDateTime.now();

    public Asignacion(Envio envio, Usuario repartidor, Usuario asignadoPor, String motivo) {
        this.envio = envio;
        this.repartidor = repartidor;
        this.asignadoPor = asignadoPor;
        this.motivo = motivo;
    }
}
