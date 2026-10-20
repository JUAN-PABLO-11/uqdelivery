package co.edu.uq.uqdelivery.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "historial_estados")
@Getter @Setter @NoArgsConstructor
public class HistorialEstado {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "envio_id", nullable = false)
    private Envio envio;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(20)")
    private EstadoEnvio estadoAnterior;   // null cuando el envío se crea

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(20)")
    private EstadoEnvio estadoNuevo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id", nullable = false)
    private Usuario actor;

    private String detalle;
    private LocalDateTime creadoEn = LocalDateTime.now();

    public HistorialEstado(Envio envio, EstadoEnvio estadoAnterior, EstadoEnvio estadoNuevo, Usuario actor, String detalle) {
        this.envio = envio;
        this.estadoAnterior = estadoAnterior;
        this.estadoNuevo = estadoNuevo;
        this.actor = actor;
        this.detalle = detalle;
    }
}
