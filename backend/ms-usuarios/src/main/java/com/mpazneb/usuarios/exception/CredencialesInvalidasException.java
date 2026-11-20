package com.mpazneb.usuarios.exception;

/**
 * Correo o contraseña incorrectos. Se responde con 401.
 * El mensaje no dice cuál de los dos falló, para no dar pistas a un atacante.
 */
public class CredencialesInvalidasException extends RuntimeException {

    public CredencialesInvalidasException() {
        super("Correo o contraseña incorrectos.");
    }
}
