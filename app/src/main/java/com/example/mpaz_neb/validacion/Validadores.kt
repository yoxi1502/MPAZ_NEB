package com.example.mpaz_neb.validacion

/**
 * Reglas de validación centralizadas de la aplicación.
 *
 * Son funciones puras (sin dependencias de Android ni de la interfaz):
 * reciben el valor ingresado y devuelven el mensaje de error que debe
 * mostrarse bajo el campo, o null si el valor es válido.
 * Los ViewModels las usan para actualizar el estado y las pantallas solo
 * muestran el resultado, por lo que la lógica queda desacoplada de la UI.
 */
object Validadores {

    const val CLAVE_MIN = 6
    const val OBSERVACION_MIN = 10
    const val OBSERVACION_MAX = 500
    const val CODIGO_LARGO = 11

    private val REGEX_CORREO =
        Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    private val REGEX_CODIGO_SESION = Regex("^MPAZ-[A-Z0-9]{6}$")

    // Detecta textos con forma de RUT chileno (12.345.678-9 o 12345678-K)
    private val REGEX_RUT = Regex("\\d{1,2}\\.?\\d{3}\\.?\\d{3}-[\\dkK]")

    fun validarCorreo(correo: String): String? {
        val limpio = correo.trim()
        return when {
            limpio.isBlank() -> "Escribe tu correo."
            !REGEX_CORREO.matches(limpio) -> "Ingresa un correo válido (ej: asistente@mpaz.cl)."
            else -> null
        }
    }

    fun validarClave(clave: String): String? = when {
        clave.isBlank() -> "Escribe tu contraseña."
        clave.length < CLAVE_MIN -> "La contraseña debe tener al menos $CLAVE_MIN caracteres."
        else -> null
    }

    /** Normaliza lo que escribe el estudiante: mayúsculas, sin espacios y largo máximo. */
    fun normalizarCodigo(codigo: String): String =
        codigo.uppercase()
            .filter { it.isLetterOrDigit() || it == '-' }
            .take(CODIGO_LARGO)

    fun validarCodigoSesion(codigo: String): String? {
        val limpio = normalizarCodigo(codigo)
        return when {
            limpio.isBlank() -> "Escribe el código que te entregó tu profesora."
            !REGEX_CODIGO_SESION.matches(limpio) -> "El código se escribe así: MPAZ-ABC123."
            else -> null
        }
    }

    fun validarObservacion(observacion: String): String? {
        val limpia = observacion.trim()
        return when {
            limpia.isBlank() -> "Escribe una observación antes de guardar."
            limpia.length < OBSERVACION_MIN -> "Escribe al menos $OBSERVACION_MIN caracteres."
            limpia.length > OBSERVACION_MAX -> "La observación admite hasta $OBSERVACION_MAX caracteres."
            REGEX_RUT.containsMatchIn(limpia) -> "No incluyas RUT ni otros datos personales reales."
            else -> null
        }
    }

    // ---- Formulario de actividad (perfil docente / UTP) ----

    const val NOMBRE_MAX = 60
    const val TEXTO_LARGO_MAX = 300
    const val PREGUNTAS_MAX = 10
    const val INTENTOS_MAX = 5

    fun validarNombreActividad(nombre: String): String? {
        val limpio = nombre.trim()
        return when {
            limpio.isBlank() -> "Escribe el nombre de la actividad."
            limpio.length < 4 -> "El nombre debe tener al menos 4 caracteres."
            limpio.length > NOMBRE_MAX -> "Máximo $NOMBRE_MAX caracteres."
            else -> null
        }
    }

    fun validarContenido(contenido: String): String? {
        val limpio = contenido.trim()
        return when {
            limpio.isBlank() -> "Indica el contenido que se trabaja."
            limpio.length > NOMBRE_MAX -> "Máximo $NOMBRE_MAX caracteres."
            else -> null
        }
    }

    fun validarTextoPedagogico(texto: String, campo: String): String? {
        val limpio = texto.trim()
        return when {
            limpio.isBlank() -> "Escribe $campo."
            limpio.length < 10 -> "Describe $campo con al menos 10 caracteres."
            limpio.length > TEXTO_LARGO_MAX -> "Máximo $TEXTO_LARGO_MAX caracteres."
            else -> null
        }
    }

    fun validarEnteroEnRango(valor: String, minimo: Int, maximo: Int): String? {
        val numero = valor.trim().toIntOrNull()
        return when {
            valor.isBlank() -> "Ingresa un número."
            numero == null -> "Solo se permiten números."
            numero !in minimo..maximo -> "Debe estar entre $minimo y $maximo."
            else -> null
        }
    }
}
