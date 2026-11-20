package com.mpazneb.actividades.dto;

import com.mpazneb.actividades.model.Dificultad;
import com.mpazneb.actividades.model.EstadoActividad;
import com.mpazneb.actividades.model.Modulo;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Datos que envía la app para crear o editar una actividad.
 * Las reglas son las mismas que valida la app en Validadores.kt,
 * así el servidor no confía solo en el cliente.
 */
public record ActividadRequest(

        @NotBlank(message = "Escribe el nombre de la actividad.")
        @Size(min = 4, max = 60, message = "El nombre debe tener entre 4 y 60 caracteres.")
        String nombre,

        @NotNull(message = "Elige el módulo.")
        Modulo modulo,

        @NotBlank(message = "Elige el curso.")
        @Size(max = 30, message = "El curso admite hasta 30 caracteres.")
        String curso,

        @NotBlank(message = "Indica el contenido que se trabaja.")
        @Size(max = 60, message = "El contenido admite hasta 60 caracteres.")
        String contenido,

        @NotBlank(message = "Escribe el objetivo pedagógico.")
        @Size(min = 10, max = 300, message = "El objetivo debe tener entre 10 y 300 caracteres.")
        String objetivo,

        @NotNull(message = "Elige la dificultad.")
        Dificultad dificultad,

        @NotBlank(message = "Escribe las instrucciones.")
        @Size(min = 10, max = 300, message = "Las instrucciones deben tener entre 10 y 300 caracteres.")
        String instrucciones,

        @NotNull(message = "Indica la cantidad de preguntas.")
        @Min(value = 1, message = "Debe tener al menos 1 pregunta.")
        @Max(value = 10, message = "Admite hasta 10 preguntas.")
        Integer cantidadPreguntas,

        @NotNull(message = "Indica los intentos permitidos.")
        @Min(value = 1, message = "Debe permitir al menos 1 intento.")
        @Max(value = 5, message = "Admite hasta 5 intentos.")
        Integer intentosPermitidos,

        @NotNull(message = "Elige el estado.")
        EstadoActividad estado
) {
}
