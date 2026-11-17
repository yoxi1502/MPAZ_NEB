package com.mpazneb.actividades.dto;

import com.mpazneb.actividades.model.EstadoActividad;
import jakarta.validation.constraints.NotNull;

/** Cuerpo del PATCH que activa o desactiva una actividad sin editar el resto. */
public record CambioEstadoRequest(

        @NotNull(message = "Indica el nuevo estado.")
        EstadoActividad estado
) {
}
