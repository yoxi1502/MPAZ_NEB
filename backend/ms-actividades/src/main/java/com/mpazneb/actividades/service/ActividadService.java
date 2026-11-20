package com.mpazneb.actividades.service;

import com.mpazneb.actividades.dto.ActividadRequest;
import com.mpazneb.actividades.dto.ActividadResponse;
import com.mpazneb.actividades.exception.RecursoNoEncontradoException;
import com.mpazneb.actividades.exception.ReglaNegocioException;
import com.mpazneb.actividades.mapper.ActividadMapper;
import com.mpazneb.actividades.model.Actividad;
import com.mpazneb.actividades.model.EstadoActividad;
import com.mpazneb.actividades.model.Modulo;
import com.mpazneb.actividades.repository.ActividadRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Lógica de negocio del catálogo. El controlador solo recibe y responde;
 * aquí se aplican las reglas y se usa el repositorio para guardar.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ActividadService {

    private final ActividadRepository repository;

    public List<ActividadResponse> listar(Modulo modulo, String curso, EstadoActividad estado) {
        String cursoFiltro = (curso == null || curso.isBlank()) ? null : curso.trim();
        return repository.buscar(modulo, cursoFiltro, estado).stream()
                .map(ActividadMapper::aResponse)
                .toList();
    }

    public ActividadResponse obtener(Long id) {
        return ActividadMapper.aResponse(buscarPorId(id));
    }

    @Transactional
    public ActividadResponse crear(ActividadRequest request) {
        if (repository.existsByNombreIgnoreCaseAndModuloAndCurso(
                request.nombre().trim(), request.modulo(), request.curso().trim())) {
            throw new ReglaNegocioException(
                    "Ya existe una actividad con ese nombre para el mismo módulo y curso.");
        }
        Actividad guardada = repository.save(ActividadMapper.aEntidad(request));
        return ActividadMapper.aResponse(guardada);
    }

    @Transactional
    public ActividadResponse actualizar(Long id, ActividadRequest request) {
        Actividad actividad = buscarPorId(id);
        if (repository.existsByNombreIgnoreCaseAndModuloAndCursoAndIdNot(
                request.nombre().trim(), request.modulo(), request.curso().trim(), id)) {
            throw new ReglaNegocioException(
                    "Ya existe otra actividad con ese nombre para el mismo módulo y curso.");
        }
        ActividadMapper.copiarDatos(request, actividad);
        return ActividadMapper.aResponse(repository.save(actividad));
    }

    /** Activa o desactiva una actividad sin tocar el resto de sus datos. */
    @Transactional
    public ActividadResponse cambiarEstado(Long id, EstadoActividad estado) {
        Actividad actividad = buscarPorId(id);
        actividad.setEstado(estado);
        return ActividadMapper.aResponse(repository.save(actividad));
    }

    @Transactional
    public void eliminar(Long id) {
        repository.delete(buscarPorId(id));
    }

    private Actividad buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró la actividad con id " + id + "."));
    }
}
