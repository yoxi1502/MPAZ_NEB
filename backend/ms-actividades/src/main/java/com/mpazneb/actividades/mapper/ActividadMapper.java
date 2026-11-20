package com.mpazneb.actividades.mapper;

import com.mpazneb.actividades.dto.ActividadRequest;
import com.mpazneb.actividades.dto.ActividadResponse;
import com.mpazneb.actividades.model.Actividad;

/** Convierte entre la entidad (base de datos) y los DTO (JSON de la API). */
public final class ActividadMapper {

    private ActividadMapper() {
    }

    public static ActividadResponse aResponse(Actividad actividad) {
        return new ActividadResponse(
                actividad.getId(),
                actividad.getNombre(),
                actividad.getModulo(),
                actividad.getCurso(),
                actividad.getContenido(),
                actividad.getObjetivo(),
                actividad.getDificultad(),
                actividad.getInstrucciones(),
                actividad.getCantidadPreguntas(),
                actividad.getIntentosPermitidos(),
                actividad.getEstado(),
                actividad.getActualizadaEn()
        );
    }

    public static Actividad aEntidad(ActividadRequest request) {
        Actividad actividad = new Actividad();
        copiarDatos(request, actividad);
        return actividad;
    }

    /** Copia los datos del formulario sobre una entidad existente (edición). */
    public static void copiarDatos(ActividadRequest request, Actividad destino) {
        destino.setNombre(request.nombre().trim());
        destino.setModulo(request.modulo());
        destino.setCurso(request.curso().trim());
        destino.setContenido(request.contenido().trim());
        destino.setObjetivo(request.objetivo().trim());
        destino.setDificultad(request.dificultad());
        destino.setInstrucciones(request.instrucciones().trim());
        destino.setCantidadPreguntas(request.cantidadPreguntas());
        destino.setIntentosPermitidos(request.intentosPermitidos());
        destino.setEstado(request.estado());
    }
}
