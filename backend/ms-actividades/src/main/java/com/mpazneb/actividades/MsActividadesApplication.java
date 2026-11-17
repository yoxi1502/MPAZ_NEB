package com.mpazneb.actividades;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Punto de entrada del microservicio del catálogo de actividades (puerto 8081). */
@SpringBootApplication
public class MsActividadesApplication {

    public static void main(String[] args) {
        SpringApplication.run(MsActividadesApplication.class, args);
    }
}
