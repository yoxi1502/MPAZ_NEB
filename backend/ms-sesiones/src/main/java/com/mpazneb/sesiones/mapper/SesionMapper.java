package com.mpazneb.sesiones.mapper;

import com.mpazneb.sesiones.dto.ResultadoResponse;
import com.mpazneb.sesiones.dto.SesionRequest;
import com.mpazneb.sesiones.dto.SesionResponse;
import com.mpazneb.sesiones.model.EstadoSesion;
import com.mpazneb.sesiones.model.ResultadoIntento;
import com.mpazneb.sesiones.model.SesionAula;

/** Convierte entre las entidades (base de datos) y los DTO (JSON de la API). */
public final class SesionMapper {

    private SesionMapper() {
    }

    /** Crea la entidad con el código ya generado y el estado inicial CREADA. */
    public static SesionAula aEntidad(SesionRequest request, String codigo) {
        SesionAula sesion = new SesionAula();
        sesion.setCodigo(codigo);
        sesion.setModulo(request.modulo());
        sesion.setCurso(request.curso().trim());
        sesion.setEstudiante(request.estudiante().trim());
        sesion.setAsistente(request.asistente().trim());
        sesion.setContenido(request.contenido().trim());
        sesion.setActividadId(request.actividadId());
        sesion.setEstado(EstadoSesion.CREADA);
        sesion.setObservacion("");
        return sesion;
    }

    public static SesionResponse aResponse(SesionAula sesion) {
        return new SesionResponse(
                sesion.getId(),
                sesion.getCodigo(),
                sesion.getEstudiante(),
                sesion.getAsistente(),
                sesion.getCurso(),
                sesion.getModulo(),
                sesion.getContenido(),
                sesion.getActividadId(),
                sesion.getEstado(),
                sesion.getObservacion() == null ? "" : sesion.getObservacion(),
                sesion.getCreadaEn(),
                sesion.getFinalizadaEn(),
                sesion.getResultados().stream().map(SesionMapper::aResponse).toList()
        );
    }

    public static ResultadoResponse aResponse(ResultadoIntento resultado) {
        return new ResultadoResponse(
                resultado.getIntentoId(),
                resultado.getAciertos(),
                resultado.getTotal(),
                resultado.getFecha()
        );
    }
}
