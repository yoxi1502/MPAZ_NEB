package com.mpazneb.usuarios.exception;

/** La clave es correcta, pero la cuenta está desactivada. Se responde con 403. */
public class CuentaDesactivadaException extends RuntimeException {

    public CuentaDesactivadaException() {
        super("Esta cuenta está desactivada. Habla con la UTP.");
    }
}
