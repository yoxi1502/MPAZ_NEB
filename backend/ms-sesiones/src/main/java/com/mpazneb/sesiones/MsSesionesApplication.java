package com.mpazneb.sesiones;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Punto de entrada del microservicio de sesiones de aula y resultados (puerto 8082). */
@SpringBootApplication
public class MsSesionesApplication {

    public static void main(String[] args) {
        SpringApplication.run(MsSesionesApplication.class, args);
    }
}
