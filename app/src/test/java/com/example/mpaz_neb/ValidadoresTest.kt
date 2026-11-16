package com.example.mpaz_neb

import com.example.mpaz_neb.validacion.Validadores
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

/** Pruebas unitarias de las reglas de validación (no requieren emulador). */
class ValidadoresTest {

    @Test
    fun correoVacioOInvalidoDevuelveError() {
        assertNotNull(Validadores.validarCorreo(""))
        assertNotNull(Validadores.validarCorreo("asistente@"))
        assertNull(Validadores.validarCorreo("asistente@mpaz.cl"))
    }

    @Test
    fun claveCortaDevuelveError() {
        assertNotNull(Validadores.validarClave("123"))
        assertNull(Validadores.validarClave("Asistente123"))
    }

    @Test
    fun codigoDeSesionSeNormalizaYValida() {
        assertEquals("MPAZ-AB12CD", Validadores.normalizarCodigo(" mpaz-ab12cd "))
        assertNull(Validadores.validarCodigoSesion("mpaz-ab12cd"))
        assertNotNull(Validadores.validarCodigoSesion("MPAZ-402"))
        assertNotNull(Validadores.validarCodigoSesion(""))
    }

    @Test
    fun observacionRechazaTextoCortoYRut() {
        assertNotNull(Validadores.validarObservacion("corto"))
        assertNotNull(Validadores.validarObservacion("El estudiante 12.345.678-9 avanzó bien"))
        assertNull(Validadores.validarObservacion("Necesitó apoyo para ubicar la idea principal."))
    }

    @Test
    fun comunicadoValidaTituloYContenido() {
        assertNotNull(Validadores.validarTituloComunicado("Hola"))
        assertNull(Validadores.validarTituloComunicado("Reunión de apoderados"))
        assertNotNull(Validadores.validarCuerpoComunicado("Corto"))
        assertNotNull(Validadores.validarCuerpoComunicado("Avisar a la familia del RUT 12345678-K"))
        assertNull(Validadores.validarCuerpoComunicado("El jueves se practica lectura en la biblioteca."))
    }
}
