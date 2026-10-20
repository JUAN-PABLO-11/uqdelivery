package co.edu.uq.uqdelivery.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "envios")
@Getter @Setter @NoArgsConstructor
public class Envio {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** UQD-000123. Se asigna después del primer save usando el id (ver EnvioService en HU-04). */
    @Column(unique = true, length = 20)
    private String codigo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Usuario cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zona_origen_id")
    private Zona zonaOrigen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zona_destino_id")
    private Zona zonaDestino;

    private String remitente;
    private String destinatario;
    private String direccionOrigen;
    private String direccionDestino;

    private double pesoKg;
    private double altoCm;
    private double anchoCm;
    private double largoCm;
    private double distanciaKm;

    @Column(precision = 12, scale = 2)
    private BigDecimal valorTotal;

    /** No modificar directamente: usar EstadoService.cambiarEstado(). */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(20)")
    private EstadoEnvio estado = EstadoEnvio.REGISTRADO;

    private LocalDateTime creadoEn = LocalDateTime.now();
}
