package co.edu.uq.uqdelivery.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "tarifas")
@Getter @Setter @NoArgsConstructor
public class Tarifa {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal base;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valorKm;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valorKg;

    private boolean vigente = true;

    public Tarifa(BigDecimal base, BigDecimal valorKm, BigDecimal valorKg) {
        this.base = base;
        this.valorKm = valorKm;
        this.valorKg = valorKg;
    }
}
