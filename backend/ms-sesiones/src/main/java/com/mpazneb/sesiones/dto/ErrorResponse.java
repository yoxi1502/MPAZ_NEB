package com.mpazneb.sesiones.dto;

import java.time.Instant;
import java.util.Map;

/**
 * Formato único de error que devuelve el microservicio.
 * La app lee "mensaje" para mostrarlo y "campos" para los errores de validación.
 */
public record ErrorResponse(
        Instant fecha,
        int estado,
        String error,
        String mensaje,
        Map<String, String> campos
) {

    public static ErrorResponse de(int estado, String error, String mensaje) {
        return new ErrorResponse(Instant.now(), estado, error, mensaje, Map.of());
    }

    public static ErrorResponse deValidacion(String mensaje, Map<String, String> campos) {
        return new ErrorResponse(Instant.now(), 400, "Bad Request", mensaje, campos);
    }
}
