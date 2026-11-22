package com.mpazneb.sesiones.service;

import com.mpazneb.sesiones.dto.IngresoRequest;
import com.mpazneb.sesiones.dto.ResultadoRequest;
import com.mpazneb.sesiones.dto.SesionRequest;
import com.mpazneb.sesiones.dto.SesionResponse;
import com.mpazneb.sesiones.exception.RecursoNoEncontradoException;
import com.mpazneb.sesiones.exception.ReglaNegocioException;
import com.mpazneb.sesiones.mapper.SesionMapper;
import com.mpazneb.sesiones.model.EstadoSesion;
import com.mpazneb.sesiones.model.ResultadoIntento;
import com.mpazneb.sesiones.model.SesionAula;
import com.mpazneb.sesiones.repository.SesionAulaRepository;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Reglas de negocio de las sesiones de aula y sus resultados. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SesionService {

    private static final int MAX_REINTENTOS_CODIGO = 20;

    private final SesionAulaRepository repository;
    private final GeneradorCodigo generadorCodigo;

    public List<SesionResponse> listar(String estudiante, String asistente) {
        return repository.buscar(vacioANull(estudiante), vacioANull(asistente)).stream()
                .map(SesionMapper::aResponse)
                .toList();
    }

    public SesionResponse obtener(Long id) {
        return SesionMapper.aResponse(buscarPorId(id));
    }

    /** Crea la sesión con un código único. La asistente no puede tener otra sesión abierta. */
    @Transactional
    public SesionResponse crear(SesionRequest request) {
        if (repository.existsByAsistenteAndEstadoNot(request.asistente().trim(), EstadoSesion.FINALIZADA)) {
            throw new ReglaNegocioException("Finaliza la sesión actual antes de generar otra.");
        }
        SesionAula sesion = SesionMapper.aEntidad(request, generarCodigoUnico());
        return SesionMapper.aResponse(repository.save(sesion));
    }

    /** El estudiante entra con su código: la sesión pasa a EN_CURSO. */
    @Transactional
    public SesionResponse ingresar(IngresoRequest request) {
        SesionAula sesion = repository.findByCodigo(request.codigo())
                .filter(SesionAula::estaAbierta)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "El código no corresponde a una sesión abierta."));

        if (request.modulo() != null && sesion.getModulo() != request.modulo()) {
            throw new ReglaNegocioException("Este código es de otro módulo. Pulsa Comenzar actividad asignada.");
        }
        if (sesion.getEstado() == EstadoSesion.CREADA) {
            sesion.setEstado(EstadoSesion.EN_CURSO);
        }
        return SesionMapper.aResponse(repository.save(sesion));
    }

    /**
     * Guarda el resultado de un intento. Si la app reenvía el mismo intento
     * (por ejemplo, tras un corte de red) no se duplica. Si la sesión tiene
     * límite de intentos y ya se alcanzó, se rechaza con 409.
     */
    @Transactional
    public SesionResponse registrarResultado(Long id, ResultadoRequest request) {
        SesionAula sesion = buscarPorId(id);
        boolean yaRegistrado = sesion.getResultados().stream()
                .anyMatch(r -> r.getIntentoId().equals(request.intentoId()));
        if (yaRegistrado) {
            return SesionMapper.aResponse(sesion);
        }
        if (sesion.getEstado() != EstadoSesion.EN_CURSO && sesion.getEstado() != EstadoSesion.COMPLETADA) {
            throw new ReglaNegocioException("La sesión ya no admite respuestas.");
        }
        if (sesion.sinIntentosDisponibles()) {
            throw new ReglaNegocioException("Ya se usaron los " + sesion.getIntentosPermitidos()
                    + " intentos permitidos de esta actividad.");
        }
        sesion.agregarResultado(new ResultadoIntento(request.intentoId(), request.aciertos(), request.total()));
        sesion.setEstado(EstadoSesion.COMPLETADA);
        return SesionMapper.aResponse(repository.save(sesion));
    }

    @Transactional
    public SesionResponse actualizarObservacion(Long id, String observacion) {
        SesionAula sesion = buscarPorId(id);
        sesion.setObservacion(observacion.trim());
        return SesionMapper.aResponse(repository.save(sesion));
    }

    @Transactional
    public SesionResponse finalizar(Long id) {
        SesionAula sesion = buscarPorId(id);
        if (sesion.estaAbierta()) {
            sesion.setEstado(EstadoSesion.FINALIZADA);
            sesion.setFinalizadaEn(Instant.now());
        }
        return SesionMapper.aResponse(repository.save(sesion));
    }

    /** Solo se eliminan sesiones finalizadas, para no borrar una práctica en curso. */
    @Transactional
    public void eliminar(Long id) {
        SesionAula sesion = buscarPorId(id);
        if (sesion.estaAbierta()) {
            throw new ReglaNegocioException("Finaliza la sesión antes de eliminarla.");
        }
        repository.delete(sesion);
    }

    private SesionAula buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró la sesión con id " + id + "."));
    }

    private String generarCodigoUnico() {
        for (int intento = 0; intento < MAX_REINTENTOS_CODIGO; intento++) {
            String codigo = generadorCodigo.generar();
            if (!repository.existsByCodigo(codigo)) {
                return codigo;
            }
        }
        throw new IllegalStateException("No se pudo generar un código de sesión único.");
    }

    private static String vacioANull(String valor) {
        return (valor == null || valor.isBlank()) ? null : valor.trim();
    }
}
