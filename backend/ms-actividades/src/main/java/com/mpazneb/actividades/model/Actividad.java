package com.mpazneb.actividades.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidad JPA: cada objeto es una fila de la tabla "actividades".
 * Representa una actividad del catálogo pedagógico (datos ficticios).
 */
@Entity
@Table(name = "actividades")
@Getter
@Setter
@NoArgsConstructor
public class Actividad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 60)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Modulo modulo;

    @Column(nullable = false, length = 30)
    private String curso;

    @Column(nullable = false, length = 60)
    private String contenido;

    @Column(nullable = false, length = 300)
    private String objetivo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Dificultad dificultad;

    @Column(nullable = false, length = 300)
    private String instrucciones;

    @Column(nullable = false)
    private Integer cantidadPreguntas;

    @Column(nullable = false)
    private Integer intentosPermitidos;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoActividad estado;

    @Column(nullable = false)
    private Instant actualizadaEn;

    /** Registra la fecha de la última modificación antes de insertar o actualizar. */
    @PrePersist
    @PreUpdate
    void marcarActualizacion() {
        actualizadaEn = Instant.now();
    }
}
