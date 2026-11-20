package com.mpazneb.sesiones.dto;

import com.mpazneb.sesiones.model.Modulo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** Código que escribe el estudiante para entrar a su actividad. */
public record IngresoRequest(

        @NotBlank(message = "Escribe el código de tu sesión.")
        @Pattern(regexp = "^MPAZ-[A-Z0-9]{6}$", message = "El código se escribe así: MPAZ-ABC123.")
        String codigo,

        /** Opcional: si viene, el código debe ser de ese módulo. */
        Modulo modulo
) {
}
