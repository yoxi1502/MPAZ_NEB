package com.mpazneb.actividades.repository;

import com.mpazneb.actividades.model.Actividad;
import com.mpazneb.actividades.model.EstadoActividad;
import com.mpazneb.actividades.model.Modulo;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Acceso a la tabla "actividades". JpaRepository ya trae save, findById,
 * findAll, deleteById, etc.; aquí solo se agregan las consultas propias.
 */
public interface ActividadRepository extends JpaRepository<Actividad, Long> {

    /** Filtros opcionales: si un parámetro es null, no se filtra por él. */
    @Query("""
            SELECT a FROM Actividad a
            WHERE (:modulo IS NULL OR a.modulo = :modulo)
              AND (:curso IS NULL OR a.curso = :curso)
              AND (:estado IS NULL OR a.estado = :estado)
            ORDER BY a.modulo, a.curso, a.nombre
            """)
    List<Actividad> buscar(@Param("modulo") Modulo modulo,
                           @Param("curso") String curso,
                           @Param("estado") EstadoActividad estado);

    /** Evita dos actividades con el mismo nombre en el mismo módulo y curso. */
    boolean existsByNombreIgnoreCaseAndModuloAndCurso(String nombre, Modulo modulo, String curso);

    boolean existsByNombreIgnoreCaseAndModuloAndCursoAndIdNot(String nombre, Modulo modulo,
                                                               String curso, Long id);
}
