package com.example.mpaz_neb.repository

import com.example.mpaz_neb.data.model.ActividadRequestDto
import com.example.mpaz_neb.data.model.CambioEstadoDto
import com.example.mpaz_neb.data.remote.ActividadesApi
import com.example.mpaz_neb.data.remote.ErrorServidor
import com.example.mpaz_neb.data.remote.MENSAJE_SIN_CONEXION
import com.example.mpaz_neb.model.EstadoActividad
import com.example.mpaz_neb.util.DatosPrueba.actividad
import com.example.mpaz_neb.util.DatosPrueba.actividadDto
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.io.IOException
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

/** Prueba el repositorio con la interfaz de Retrofit simulada: no necesita el servidor. */
class ActividadRepositoryTest {

    private val api = mockk<ActividadesApi>()
    private val repository = ActividadRepository(api)

    @Test
    fun `listar convierte el JSON al modelo de la app`() = runTest {
        coEvery { api.listar(null, null, EstadoActividad.DISPONIBLE) } returns listOf(actividadDto(id = 5))

        val lista = repository.listar(estado = EstadoActividad.DISPONIBLE)

        assertEquals(5L, lista.single().id)
        assertTrue(lista.single().actualizadaEn > 0)
    }

    @Test
    fun `guardar una actividad sin id hace POST`() = runTest {
        coEvery { api.crear(any()) } returns actividadDto(id = 9)

        val creada = repository.guardar(actividad(id = null))

        assertEquals(9L, creada.id)
        coVerify(exactly = 1) { api.crear(any<ActividadRequestDto>()) }
        coVerify(exactly = 0) { api.actualizar(any(), any()) }
    }

    @Test
    fun `guardar una actividad con id hace PUT`() = runTest {
        coEvery { api.actualizar(4, any()) } returns actividadDto(id = 4)

        repository.guardar(actividad(id = 4))

        coVerify(exactly = 1) { api.actualizar(4, any()) }
    }

    @Test
    fun `cambiarEstado envia solo el nuevo estado`() = runTest {
        coEvery { api.cambiarEstado(4, CambioEstadoDto(EstadoActividad.DESACTIVADA)) } returns
                actividadDto(id = 4, estado = EstadoActividad.DESACTIVADA)

        val actualizada = repository.cambiarEstado(4, EstadoActividad.DESACTIVADA)

        assertEquals(EstadoActividad.DESACTIVADA, actualizada.estado)
    }

    @Test
    fun `eliminar acepta la respuesta 204`() = runTest {
        coEvery { api.eliminar(4) } returns Response.success(204, Unit)

        repository.eliminar(4)

        coVerify { api.eliminar(4) }
    }

    @Test
    fun `eliminar con 404 lanza ErrorServidor con el mensaje del microservicio`() = runTest {
        val cuerpo = """{"estado":404,"mensaje":"No se encontró la actividad con id 4."}"""
        coEvery { api.eliminar(4) } returns Response.error(404, cuerpo.toResponseBody())

        try {
            repository.eliminar(4)
            fail("Debía lanzar ErrorServidor")
        } catch (e: ErrorServidor) {
            assertEquals(404, e.codigoHttp)
            assertEquals("No se encontró la actividad con id 4.", e.message)
        }
    }

    @Test
    fun `sin conexion lanza ErrorServidor con el mensaje de conexion`() = runTest {
        coEvery { api.listar(any(), any(), any()) } throws IOException("Connection refused")

        try {
            repository.listar()
            fail("Debía lanzar ErrorServidor")
        } catch (e: ErrorServidor) {
            assertEquals(MENSAJE_SIN_CONEXION, e.message)
        }
    }

    @Test
    fun `un 409 de validacion muestra el primer campo con error`() = runTest {
        val cuerpo = """{"estado":400,"mensaje":"Revisa los datos enviados.","campos":{"nombre":"El nombre debe tener entre 4 y 60 caracteres."}}"""
        coEvery { api.crear(any()) } throws HttpException(Response.error<Any>(400, cuerpo.toResponseBody()))

        try {
            repository.guardar(actividad(id = null))
            fail("Debía lanzar ErrorServidor")
        } catch (e: ErrorServidor) {
            assertEquals("El nombre debe tener entre 4 y 60 caracteres.", e.message)
        }
    }
}
