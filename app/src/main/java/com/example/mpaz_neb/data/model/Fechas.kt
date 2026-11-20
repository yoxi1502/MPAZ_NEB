package com.example.mpaz_neb.data.model

import java.time.Instant
import java.time.format.DateTimeParseException

/**
 * Los microservicios envían las fechas en formato ISO-8601 (2026-11-16T22:10:00Z).
 * La app las guarda como milisegundos (Long), igual que antes con DataStore.
 */
internal fun isoAMilis(fecha: String?): Long? =
    try {
        fecha?.let { Instant.parse(it).toEpochMilli() }
    } catch (e: DateTimeParseException) {
        null
    }
