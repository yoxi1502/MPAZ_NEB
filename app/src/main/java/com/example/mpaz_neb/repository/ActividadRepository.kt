package com.example.mpaz_neb.repository

import com.example.mpaz_neb.data.model.CambioEstadoDto
import com.example.mpaz_neb.data.model.aModelo
import com.example.mpaz_neb.data.model.aRequest
import com.example.mpaz_neb.data.remote.ActividadesApi
import com.example.mpaz_neb.data.remote.RetrofitInstance
import com.example.mpaz_neb.data.remote.exigirExito
import com.example.mpaz_neb.data.remote.llamarServidor
import com.example.mpaz_neb.model.Actividad
import com.example.mpaz_neb.model.EstadoActividad
import com.example.mpaz_neb.model.ModuloActividad

/**
 * Catálogo de actividades guardado en el microservicio ms-actividades.
 * Recibe la API por parámetro para poder reemplazarla por un mock en las pruebas.
 * Todos los errores de red llegan como ErrorServidor con un mensaje para mostrar.
 */
class ActividadRepository(
    private val api: ActividadesApi = RetrofitInstance.actividadesApi
) {

    suspend fun listar(
        modulo: ModuloActividad? = null,
        curso: String? = null,
        estado: EstadoActividad? = null
    ): List<Actividad> = llamarServidor {
        api.listar(modulo, curso, estado).map { it.aModelo() }
    }

    suspend fun obtener(id: Long): Actividad = llamarServidor {
        api.obtener(id).aModelo()
    }

    /** POST si la actividad es nueva (sin id), PUT si ya existe. */
    suspend fun guardar(actividad: Actividad): Actividad = llamarServidor {
        val id = actividad.id
        val respuesta = if (id == null) {
            api.crear(actividad.aRequest())
        } else {
            api.actualizar(id, actividad.aRequest())
        }
        respuesta.aModelo()
    }

    /** PATCH: activa o desactiva sin reenviar el resto de los datos. */
    suspend fun cambiarEstado(id: Long, estado: EstadoActividad): Actividad = llamarServidor {
        api.cambiarEstado(id, CambioEstadoDto(estado)).aModelo()
    }

    suspend fun eliminar(id: Long) = llamarServidor {
        api.eliminar(id).exigirExito()
    }
}
