package com.mpazneb.actividades.config;

import com.mpazneb.actividades.model.Actividad;
import com.mpazneb.actividades.model.Dificultad;
import com.mpazneb.actividades.model.EstadoActividad;
import com.mpazneb.actividades.model.Modulo;
import com.mpazneb.actividades.repository.ActividadRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Al encender el microservicio por primera vez (tabla vacía) carga dos
 * actividades de ejemplo, una por módulo, con datos ficticios.
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "mpaz.datos-iniciales.habilitado", havingValue = "true", matchIfMissing = true)
public class DatosIniciales implements CommandLineRunner {

    private final ActividadRepository repository;

    @Override
    public void run(String... args) {
        if (repository.count() > 0) {
            return;
        }
        repository.saveAll(List.of(
                crear("Una tarde en el parque", Modulo.LECTURA, "2.° Año Básico",
                        "Comprensión lectora",
                        "Responder preguntas explícitas, implícitas y de vocabulario de un texto breve.",
                        "Lee la historia con calma, elige una alternativa y comprueba tu respuesta."),
                crear("Sumas y restas hasta 10", Modulo.MATEMATICA, "1.° Año Básico",
                        "Adición y sustracción",
                        "Resolver sumas y restas simples con números hasta 10.",
                        "Resuelve la operación, elige una respuesta y comprueba.")
        ));
    }

    private Actividad crear(String nombre, Modulo modulo, String curso, String contenido,
                            String objetivo, String instrucciones) {
        Actividad actividad = new Actividad();
        actividad.setNombre(nombre);
        actividad.setModulo(modulo);
        actividad.setCurso(curso);
        actividad.setContenido(contenido);
        actividad.setObjetivo(objetivo);
        actividad.setDificultad(Dificultad.INICIAL);
        actividad.setInstrucciones(instrucciones);
        actividad.setCantidadPreguntas(3);
        actividad.setIntentosPermitidos(2);
        actividad.setEstado(EstadoActividad.DISPONIBLE);
        return actividad;
    }
}
