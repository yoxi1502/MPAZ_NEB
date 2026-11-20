package com.mpazneb.sesiones.dto;

import java.time.Instant;

/** Resultado de un intento dentro de la sesión. */
public record ResultadoResponse(
        String intentoId,
        Integer aciertos,
        Integer total,
        Instant fecha
) {
}
