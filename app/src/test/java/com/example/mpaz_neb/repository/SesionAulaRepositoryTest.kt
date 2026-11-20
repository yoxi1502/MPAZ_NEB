package com.example.mpaz_neb.repository

import com.example.mpaz_neb.data.local.SesionDispositivoStore
import com.example.mpaz_neb.data.model.IngresoDto
import com.example.mpaz_neb.data.model.ObservacionDto
import com.example.mpaz_neb.data.model.ResultadoRequestDto
import com.example.mpaz_neb.data.remote.ErrorServidor
import com.example.mpaz_neb.data.remote.SesionesApi
import com.example.mpaz_neb.model.EstadoSesion
import com.example.mpaz_neb.model.ModuloActividad
import com.example.mpaz_neb.util.DatosPrueba.ASISTENTE
import com.example.mpaz_neb.util.DatosPrueba.CURSO
import com.example.mpaz_neb.util.DatosPrueba.ESTUDIANTE
import com.example.mpaz_neb.util.DatosPrueba.sesionDto
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.fail
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

/** Prueba las sesiones con la API y el almacenamiento del dispositivo simulados. */
class SesionAulaRepositoryTest {

    private val api = mockk<SesionesApi>()
    private val dispositivo = mockk<SesionDispositivoStore>(relaxed = true)
    private val repository = SesionAulaRepository(dispositivo, api)

    private fun error404() = HttpException(Response.error<Any>(404, """{"mensaje":"No existe"}""".toResponseBody()))

    @Test
    fun `sin sesion guardada no se llama al servidor`() = runTest {
        coEvery { dispositivo.obtenerSesionActualId() } returns null

        assertNull(repository.obtenerSesionActual())
        coVerify(exactly = 0) { api.obtener(any()) }
    }

    @Test
    fun `la sesion actual se lee desde el servidor`() = runTest {
        coEvery { dispositivo.obtenerSesionActualId() } returns 10
        coEvery { api.obtener(10) } returns sesionDto(id = 10)

        val sesion = repository.obtenerSesionActual()

        assertEquals("MPAZ-AB12CD", sesion?.codigo)
        assertEquals(1, sesion?.resultados?.size)
    }

    @Test
    fun `si el servidor ya no tiene la sesion se olvida el id guardado`() = runTest {
        coEvery { dispositivo.obtenerSesionActualId() } returns 10
        coEvery { api.obtener(10) } throws error404()

        assertNull(repository.obtenerSesionActual())
        coVerify { dispositivo.limpiar() }
    }

    @Test
    fun `otros errores del servidor se propagan`() = runTest {
        coEvery { dispositivo.obtenerSesionActualId() } returns 10
        coEvery { api.obtener(10) } throws HttpException(Response.error<Any>(500, "".toResponseBody()))

        try {
            repository.obtenerSesionActual()
            fail("Debía lanzar ErrorServidor")
        } catch (e: ErrorServidor) {
            assertEquals(500, e.codigoHttp)
        }
    }

    @Test
    fun `crear sesion guarda su id en el dispositivo`() = runTest {
        coEvery { api.crear(any()) } returns sesionDto(id = 33)

        val creada = repository.crearSesion(ModuloActividad.LECTURA, CURSO, ESTUDIANTE, ASISTENTE, "Lectura", 1L)

        assertEquals(33L, creada.id)
        coVerify { dispositivo.guardarSesionActual(33) }
    }

    @Test(expected = IllegalArgumentException::class)
    fun `crear sesion con un curso fuera de la lista falla antes de llamar al servidor`() = runTest {
        repository.crearSesion(ModuloActividad.LECTURA, "Curso inventado", ESTUDIANTE, ASISTENTE, "Lectura", 1L)
    }

    @Test
    fun `ingresar normaliza el codigo y recuerda la sesion`() = runTest {
        coEvery { api.ingresar(IngresoDto("MPAZ-AB12CD", null)) } returns sesionDto(id = 10, estado = EstadoSesion.EN_CURSO)

        val sesion = repository.ingresarConCodigo(" mpaz-ab12cd ")

        assertEquals(EstadoSesion.EN_CURSO, sesion.estado)
        coVerify { dispositivo.guardarSesionActual(10) }
    }

    @Test
    fun `registrar resultado envia el id del intento`() = runTest {
        coEvery { api.registrarResultado(10, ResultadoRequestDto("intento-1", 2, 3)) } returns
                sesionDto(estado = EstadoSesion.COMPLETADA)

        val sesion = repository.registrarResultado(10, "intento-1", 2, 3)

        assertEquals(EstadoSesion.COMPLETADA, sesion.estado)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `un resultado con mas aciertos que preguntas no se envia`() = runTest {
        repository.registrarResultado(10, "intento-1", 4, 3)
    }

    @Test
    fun `guardar observacion envia el texto sin espacios`() = runTest {
        coEvery { api.actualizarObservacion(10, ObservacionDto("Leyó con apoyo de la asistente.")) } returns sesionDto()

        repository.guardarObservacion(10, "  Leyó con apoyo de la asistente.  ")

        coVerify { api.actualizarObservacion(10, ObservacionDto("Leyó con apoyo de la asistente.")) }
    }

    @Test
    fun `listar y finalizar usan sus endpoints`() = runTest {
        coEvery { api.listar(ESTUDIANTE) } returns listOf(sesionDto())
        coEvery { api.finalizar(10) } returns sesionDto(estado = EstadoSesion.FINALIZADA)

        assertEquals(1, repository.listar(ESTUDIANTE).size)
        assertEquals(EstadoSesion.FINALIZADA, repository.finalizarSesion(10).estado)
    }

    @Test
    fun `eliminar la sesion actual tambien la olvida en el dispositivo`() = runTest {
        coEvery { api.eliminar(10) } returns Response.success(204, Unit)
        coEvery { dispositivo.obtenerSesionActualId() } returns 10

        repository.eliminar(10)

        coVerify { dispositivo.limpiar() }
    }
}
