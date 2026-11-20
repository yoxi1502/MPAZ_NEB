package com.mpazneb.sesiones.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Resultado de un intento del estudiante dentro de una sesión (tabla hija). */
@Entity
@Table(name = "resultados_intento",
        uniqueConstraints = @UniqueConstraint(columnNames = {"sesion_id", "intento_id"}))
@Getter
@Setter
@NoArgsConstructor
public class ResultadoIntento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Id que genera la app: evita guardar dos veces el mismo intento si se reintenta el envío. */
    @Column(name = "intento_id", nullable = false, length = 40)
    private String intentoId;

    @Column(nullable = false)
    private Integer aciertos;

    @Column(nullable = false)
    private Integer total;

    @Column(nullable = false)
    private Instant fecha;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sesion_id", nullable = false)
    private SesionAula sesion;

    public ResultadoIntento(String intentoId, Integer aciertos, Integer total) {
        this.intentoId = intentoId;
        this.aciertos = aciertos;
        this.total = total;
        this.fecha = Instant.now();
    }
}
