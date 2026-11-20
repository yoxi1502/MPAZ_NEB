package com.mpazneb.sesiones.service;

import java.security.SecureRandom;
import org.springframework.stereotype.Component;

/**
 * Genera códigos de sesión como MPAZ-AB12CD.
 * Está en su propia clase para poder reemplazarlo por un mock en las pruebas.
 */
@Component
public class GeneradorCodigo {

    // Sin 0, O, 1 ni I para que el estudiante no los confunda
    private static final String ALFABETO = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int LARGO = 6;

    private final SecureRandom random = new SecureRandom();

    public String generar() {
        StringBuilder codigo = new StringBuilder("MPAZ-");
        for (int i = 0; i < LARGO; i++) {
            codigo.append(ALFABETO.charAt(random.nextInt(ALFABETO.length())));
        }
        return codigo.toString();
    }
}
