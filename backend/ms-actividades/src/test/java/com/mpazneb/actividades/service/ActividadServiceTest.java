package com.mpazneb.actividades.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mpazneb.actividades.dto.ActividadRequest;
import com.mpazneb.actividades.dto.ActividadResponse;
import com.mpazneb.actividades.exception.RecursoNoEncontradoException;
import com.mpazneb.actividades.exception.ReglaNegocioException;
import com.mpazneb.actividades.model.Actividad;
import com.mpazneb.actividades.model.Dificultad;
import com.mpazneb.actividades.model.EstadoActividad;
import com.mpazneb.actividades.model.Modulo;
import com.mpazneb.actividades.repository.ActividadRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Prueba la lógica del servicio simulando (mock) el repositorio: no usa base de datos. */
@ExtendWith(MockitoExtension.class)
class ActividadServiceTest {

    @Mock
    private ActividadRepository repository;

    @InjectMocks
    private ActividadService service;

    @Test
    void crearGuardaLaActividadCuandoNoEstaRepetida() {
        when(repository.existsByNombreIgnoreCaseAndModuloAndCurso(
                "Sumas hasta 20", Modulo.MATEMATICA, "2.° Año Básico")).thenReturn(false);
        when(repository.save(any(Actividad.class))).thenAnswer(invocacion -> {
            Actividad actividad = invocacion.getArgument(0);
            actividad.setId(7L);
            return actividad;
        });

        ActividadResponse creada = service.crear(request("  Sumas hasta 20  "));

        assertThat(creada.id()).isEqualTo(7L);
        assertThat(creada.nombre()).isEqualTo("Sumas hasta 20");
        assertThat(creada.estado()).isEqualTo(EstadoActividad.DISPONIBLE);
    }

    @Test
    void crearRechazaUnNombreRepetidoEnElMismoCurso() {
        when(repository.existsByNombreIgnoreCaseAndModuloAndCurso(
                "Sumas hasta 20", Modulo.MATEMATICA, "2.° Año Básico")).thenReturn(true);

        assertThatThrownBy(() -> service.crear(request("Sumas hasta 20")))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("Ya existe");
        verify(repository, never()).save(any());
    }

    @Test
    void obtenerLanzaNoEncontradoSiElIdNoExiste() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.obtener(99L))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining("99");
    }

    @Test
    void actualizarCambiaLosDatosDeLaActividad() {
        Actividad existente = actividad(3L, "Nombre antiguo");
        when(repository.findById(3L)).thenReturn(Optional.of(existente));
        when(repository.existsByNombreIgnoreCaseAndModuloAndCursoAndIdNot(
                "Sumas hasta 20", Modulo.MATEMATICA, "2.° Año Básico", 3L)).thenReturn(false);
        when(repository.save(existente)).thenReturn(existente);

        ActividadResponse actualizada = service.actualizar(3L, request("Sumas hasta 20"));

        assertThat(actualizada.nombre()).isEqualTo("Sumas hasta 20");
        assertThat(actualizada.cantidadPreguntas()).isEqualTo(5);
    }

    @Test
    void actualizarRechazaElNombreDeOtraActividad() {
        when(repository.findById(3L)).thenReturn(Optional.of(actividad(3L, "Nombre antiguo")));
        when(repository.existsByNombreIgnoreCaseAndModuloAndCursoAndIdNot(
                "Sumas hasta 20", Modulo.MATEMATICA, "2.° Año Básico", 3L)).thenReturn(true);

        assertThatThrownBy(() -> service.actualizar(3L, request("Sumas hasta 20")))
                .isInstanceOf(ReglaNegocioException.class);
    }

    @Test
    void cambiarEstadoSoloModificaElEstado() {
        Actividad existente = actividad(4L, "Lectura breve");
        when(repository.findById(4L)).thenReturn(Optional.of(existente));
        when(repository.save(existente)).thenReturn(existente);

        ActividadResponse respuesta = service.cambiarEstado(4L, EstadoActividad.DESACTIVADA);

        assertThat(respuesta.estado()).isEqualTo(EstadoActividad.DESACTIVADA);
        assertThat(respuesta.nombre()).isEqualTo("Lectura breve");
    }

    @Test
    void eliminarBorraLaActividadExistente() {
        Actividad existente = actividad(5L, "Lectura breve");
        when(repository.findById(5L)).thenReturn(Optional.of(existente));

        service.eliminar(5L);

        verify(repository).delete(existente);
    }

    @Test
    void eliminarLanzaNoEncontradoSiNoExiste() {
        when(repository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.eliminar(5L))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void listarIgnoraElFiltroDeCursoVacio() {
        when(repository.buscar(Modulo.LECTURA, null, null))
                .thenReturn(List.of(actividad(1L, "Una tarde en el parque")));

        List<ActividadResponse> lista = service.listar(Modulo.LECTURA, "  ", null);

        assertThat(lista).extracting(ActividadResponse::nombre)
                .containsExactly("Una tarde en el parque");
    }

    private ActividadRequest request(String nombre) {
        return new ActividadRequest(nombre, Modulo.MATEMATICA, "2.° Año Básico",
                "Adición", "Resolver sumas simples hasta 20.", Dificultad.INTERMEDIA,
                "Resuelve cada suma y comprueba.", 5, 2, EstadoActividad.DISPONIBLE);
    }

    private Actividad actividad(Long id, String nombre) {
        Actividad actividad = new Actividad();
        actividad.setId(id);
        actividad.setNombre(nombre);
        actividad.setModulo(Modulo.LECTURA);
        actividad.setCurso("2.° Año Básico");
        actividad.setContenido("Comprensión lectora");
        actividad.setObjetivo("Responder preguntas de un texto breve.");
        actividad.setDificultad(Dificultad.INICIAL);
        actividad.setInstrucciones("Lee con calma y responde.");
        actividad.setCantidadPreguntas(3);
        actividad.setIntentosPermitidos(2);
        actividad.setEstado(EstadoActividad.DISPONIBLE);
        return actividad;
    }
}
