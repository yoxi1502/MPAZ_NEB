package com.mpazneb.sesiones.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Respuestas finales que envía la app cuando el estudiante termina la actividad. */
public record ResultadoRequest(

        @NotBlank(message = "Falta el id del intento.")
        @Size(max = 40, message = "El id del intento admite hasta 40 caracteres.")
        String intentoId,

        @NotNull(message = "Faltan los aciertos.")
        @Min(value = 0, message = "Los aciertos no pueden ser negativos.")
        Integer aciertos,

        @NotNull(message = "Falta el total de preguntas.")
        @Min(value = 1, message = "Debe haber al menos 1 pregunta.")
        @Max(value = 10, message = "Admite hasta 10 preguntas.")
        Integer total
) {

    /** Regla entre dos campos: no puede haber más aciertos que preguntas. */
    @AssertTrue(message = "Los aciertos no pueden superar el total de preguntas.")
    public boolean isAciertosValidos() {
        return aciertos == null || total == null || aciertos <= total;
    }
}
