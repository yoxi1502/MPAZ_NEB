package com.mpazneb.sesiones.repository;

import com.mpazneb.sesiones.model.EstadoSesion;
import com.mpazneb.sesiones.model.SesionAula;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Acceso a la tabla "sesiones_aula". Los resultados se guardan en cascada con su sesión. */
public interface SesionAulaRepository extends JpaRepository<SesionAula, Long> {

    Optional<SesionAula> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);

    /** Sirve para la regla: una asistente no puede tener dos sesiones abiertas. */
    boolean existsByAsistenteAndEstadoNot(String asistente, EstadoSesion estado);

    /** Historial con filtros opcionales, de la más reciente a la más antigua. */
    @Query("""
            SELECT s FROM SesionAula s
            WHERE (:estudiante IS NULL OR s.estudiante = :estudiante)
              AND (:asistente IS NULL OR s.asistente = :asistente)
            ORDER BY s.creadaEn DESC
            """)
    List<SesionAula> buscar(@Param("estudiante") String estudiante,
                            @Param("asistente") String asistente);
}
