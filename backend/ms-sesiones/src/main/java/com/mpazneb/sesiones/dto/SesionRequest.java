package com.mpazneb.sesiones.dto;

import com.mpazneb.sesiones.model.Modulo;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Datos para crear una sesión. El código y el estado los define el servidor. */
public record SesionRequest(

        @NotNull(message = "Elige el módulo.")
        Modulo modulo,

        @NotBlank(message = "Selecciona un nivel o curso.")
        @Size(max = 30, message = "El curso admite hasta 30 caracteres.")
        String curso,

        @NotBlank(message = "Selecciona un estudiante.")
        @Size(max = 60, message = "El estudiante admite hasta 60 caracteres.")
        String estudiante,

        @NotBlank(message = "Falta la asistente responsable.")
        @Size(max = 60, message = "La asistente admite hasta 60 caracteres.")
        String asistente,

        @NotBlank(message = "Falta la actividad asignada.")
        @Size(max = 120, message = "El contenido admite hasta 120 caracteres.")
        String contenido,

        Long actividadId,

        /** Opcional: si viene, limita cuántos resultados acepta la sesión. */
        @Min(value = 1, message = "Debe permitir al menos 1 intento.")
        @Max(value = 5, message = "Admite hasta 5 intentos.")
        Integer intentosPermitidos
) {
}
