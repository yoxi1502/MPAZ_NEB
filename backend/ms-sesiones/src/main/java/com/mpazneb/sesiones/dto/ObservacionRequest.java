package com.mpazneb.sesiones.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.regex.Pattern;

/** Observación pedagógica de la asistente. No puede contener RUT ni datos personales. */
public record ObservacionRequest(

        @NotBlank(message = "Escribe una observación antes de guardar.")
        @Size(min = 10, max = 500, message = "La observación debe tener entre 10 y 500 caracteres.")
        String observacion
) {

    // Detecta textos con forma de RUT chileno (12.345.678-9 o 12345678-K)
    private static final Pattern RUT = Pattern.compile("\\d{1,2}\\.?\\d{3}\\.?\\d{3}-[\\dkK]");

    @AssertTrue(message = "No incluyas RUT ni otros datos personales reales.")
    public boolean isSinDatosPersonales() {
        return observacion == null || !RUT.matcher(observacion).find();
    }
}
