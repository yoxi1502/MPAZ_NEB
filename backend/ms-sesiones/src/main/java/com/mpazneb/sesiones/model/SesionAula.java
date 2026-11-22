package com.mpazneb.sesiones.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidad JPA: una sesión de práctica que la asistente asigna a un estudiante ficticio.
 * Una sesión tiene muchos resultados (relación uno a muchos con ResultadoIntento).
 */
@Entity
@Table(name = "sesiones_aula")
@Getter
@Setter
@NoArgsConstructor
public class SesionAula {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Código que escribe el estudiante, por ejemplo MPAZ-AB12CD. */
    @Column(nullable = false, unique = true, length = 11)
    private String codigo;

    @Column(nullable = false, length = 60)
    private String estudiante;

    @Column(nullable = false, length = 60)
    private String asistente;

    @Column(nullable = false, length = 30)
    private String curso;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Modulo modulo;

    @Column(nullable = false, length = 120)
    private String contenido;

    /** Id de la actividad en ms-actividades (referencia entre microservicios). */
    private Long actividadId;

    /** Intentos que permite la actividad asignada. Null = sin límite (sesiones antiguas). */
    private Integer intentosPermitidos;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoSesion estado;

    @Column(length = 500)
    private String observacion;

    @Column(nullable = false)
    private Instant creadaEn;

    private Instant finalizadaEn;

    @OneToMany(mappedBy = "sesion", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("fecha ASC")
    private List<ResultadoIntento> resultados = new ArrayList<>();

    @PrePersist
    void alCrear() {
        if (creadaEn == null) {
            creadaEn = Instant.now();
        }
    }

    /** Mantiene los dos lados de la relación sincronizados. */
    public void agregarResultado(ResultadoIntento resultado) {
        resultado.setSesion(this);
        resultados.add(resultado);
    }

    public boolean estaAbierta() {
        return estado != EstadoSesion.FINALIZADA;
    }

    /** True si la sesión tiene límite y ya se registraron todos los intentos permitidos. */
    public boolean sinIntentosDisponibles() {
        return intentosPermitidos != null && resultados.size() >= intentosPermitidos;
    }
}
