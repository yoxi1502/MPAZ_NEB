package com.mpazneb.actividades.exception;

/** Se lanza cuando el id pedido no existe. Se responde con 404. */
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
