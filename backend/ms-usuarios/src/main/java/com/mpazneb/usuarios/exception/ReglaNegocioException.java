package com.mpazneb.usuarios.exception;

/** Se lanza cuando la operación rompe una regla del negocio. Se responde con 409. */
public class ReglaNegocioException extends RuntimeException {

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
