package com.mpazneb.sesiones.model;

/**
 * Ciclo de vida de una sesión:
 * CREADA (código generado) → EN_CURSO (el estudiante ingresó) →
 * COMPLETADA (envió respuestas) → FINALIZADA (la asistente la cerró).
 */
public enum EstadoSesion {
    CREADA,
    EN_CURSO,
    COMPLETADA,
    FINALIZADA
}
