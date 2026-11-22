package com.mpazneb.sesiones.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mpazneb.sesiones.dto.IngresoRequest;
import com.mpazneb.sesiones.dto.ResultadoRequest;
import com.mpazneb.sesiones.dto.SesionRequest;
import com.mpazneb.sesiones.dto.SesionResponse;
import com.mpazneb.sesiones.exception.RecursoNoEncontradoException;
import com.mpazneb.sesiones.exception.ReglaNegocioException;
import com.mpazneb.sesiones.model.EstadoSesion;
import com.mpazneb.sesiones.model.Modulo;
import com.mpazneb.sesiones.model.ResultadoIntento;
import com.mpazneb.sesiones.model.SesionAula;
import com.mpazneb.sesiones.repository.SesionAulaRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Prueba las reglas de las sesiones con el repositorio y el generador de códigos simulados. */
@ExtendWith(MockitoExtension.class)
class SesionServiceTest {

    @Mock
    private SesionAulaRepository repository;

    @Mock
    private GeneradorCodigo generadorCodigo;

    @InjectMocks
    private SesionService service;

    private final SesionRequest request = new SesionRequest(Modulo.LECTURA, "2.° Año Básico",
            "Estudiante 01 - Aula A", "Asistente Aula A", "Una tarde en el parque · Inicial", 1L, 2);

    @Test
    void crearGeneraUnCodigoYDejaLaSesionCreada() {
        when(repository.existsByAsistenteAndEstadoNot("Asistente Aula A", EstadoSesion.FINALIZADA))
                .thenReturn(false);
        when(generadorCodigo.generar()).thenReturn("MPAZ-AAAAAA");
        when(repository.existsByCodigo("MPAZ-AAAAAA")).thenReturn(false);
        when(repository.save(any(SesionAula.class))).thenAnswer(i -> conId(i.getArgument(0), 1L));

        SesionResponse creada = service.crear(request);

        assertThat(creada.codigo()).isEqualTo("MPAZ-AAAAAA");
        assertThat(creada.estado()).isEqualTo(EstadoSesion.CREADA);
        assertThat(creada.resultados()).isEmpty();
        assertThat(creada.intentosPermitidos()).isEqualTo(2);
    }

    @Test
    void crearReintentaSiElCodigoYaExiste() {
        when(repository.existsByAsistenteAndEstadoNot(any(), any())).thenReturn(false);
        when(generadorCodigo.generar()).thenReturn("MPAZ-REPETI", "MPAZ-NUEVO2");
        when(repository.existsByCodigo("MPAZ-REPETI")).thenReturn(true);
        when(repository.existsByCodigo("MPAZ-NUEVO2")).thenReturn(false);
        when(repository.save(any(SesionAula.class))).thenAnswer(i -> conId(i.getArgument(0), 2L));

        assertThat(service.crear(request).codigo()).isEqualTo("MPAZ-NUEVO2");
    }

    @Test
    void crearRechazaSiLaAsistenteTieneUnaSesionAbierta() {
        when(repository.existsByAsistenteAndEstadoNot("Asistente Aula A", EstadoSesion.FINALIZADA))
                .thenReturn(true);

        assertThatThrownBy(() -> service.crear(request))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("Finaliza la sesión actual");
        verify(repository, never()).save(any());
    }

    @Test
    void ingresarConCodigoValidoPasaLaSesionAEnCurso() {
        SesionAula sesion = sesion(EstadoSesion.CREADA);
        when(repository.findByCodigo("MPAZ-AB12CD")).thenReturn(Optional.of(sesion));
        when(repository.save(sesion)).thenReturn(sesion);

        SesionResponse respuesta = service.ingresar(new IngresoRequest("MPAZ-AB12CD", null));

        assertThat(respuesta.estado()).isEqualTo(EstadoSesion.EN_CURSO);
    }

    @Test
    void ingresarConUnaSesionFinalizadaLanzaNoEncontrado() {
        when(repository.findByCodigo("MPAZ-AB12CD")).thenReturn(Optional.of(sesion(EstadoSesion.FINALIZADA)));

        assertThatThrownBy(() -> service.ingresar(new IngresoRequest("MPAZ-AB12CD", null)))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void ingresarConOtroModuloLanzaReglaDeNegocio() {
        when(repository.findByCodigo("MPAZ-AB12CD")).thenReturn(Optional.of(sesion(EstadoSesion.CREADA)));

        assertThatThrownBy(() -> service.ingresar(new IngresoRequest("MPAZ-AB12CD", Modulo.MATEMATICA)))
                .isInstanceOf(ReglaNegocioException.class);
    }

    @Test
    void registrarResultadoCompletaLaSesion() {
        SesionAula sesion = sesion(EstadoSesion.EN_CURSO);
        when(repository.findById(1L)).thenReturn(Optional.of(sesion));
        when(repository.save(sesion)).thenReturn(sesion);

        SesionResponse respuesta = service.registrarResultado(1L, new ResultadoRequest("intento-1", 2, 3));

        assertThat(respuesta.estado()).isEqualTo(EstadoSesion.COMPLETADA);
        assertThat(respuesta.resultados()).hasSize(1);
        assertThat(respuesta.resultados().getFirst().aciertos()).isEqualTo(2);
    }

    @Test
    void registrarElMismoIntentoDosVecesNoLoDuplica() {
        SesionAula sesion = sesion(EstadoSesion.COMPLETADA);
        sesion.agregarResultado(new ResultadoIntento("intento-1", 2, 3));
        when(repository.findById(1L)).thenReturn(Optional.of(sesion));

        SesionResponse respuesta = service.registrarResultado(1L, new ResultadoRequest("intento-1", 2, 3));

        assertThat(respuesta.resultados()).hasSize(1);
        verify(repository, never()).save(any());
    }

    @Test
    void registrarUnIntentoMasAllaDelLimiteLanzaReglaDeNegocio() {
        SesionAula sesion = sesion(EstadoSesion.COMPLETADA);
        sesion.setIntentosPermitidos(2);
        sesion.agregarResultado(new ResultadoIntento("intento-1", 2, 3));
        sesion.agregarResultado(new ResultadoIntento("intento-2", 3, 3));
        when(repository.findById(1L)).thenReturn(Optional.of(sesion));

        assertThatThrownBy(() -> service.registrarResultado(1L, new ResultadoRequest("intento-3", 1, 3)))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("2 intentos");
        verify(repository, never()).save(any());
    }

    @Test
    void reenviarElUltimoIntentoPermitidoNoLanzaError() {
        SesionAula sesion = sesion(EstadoSesion.COMPLETADA);
        sesion.setIntentosPermitidos(1);
        sesion.agregarResultado(new ResultadoIntento("intento-1", 2, 3));
        when(repository.findById(1L)).thenReturn(Optional.of(sesion));

        SesionResponse respuesta = service.registrarResultado(1L, new ResultadoRequest("intento-1", 2, 3));

        assertThat(respuesta.resultados()).hasSize(1);
    }

    @Test
    void registrarDentroDelLimiteGuardaElResultado() {
        SesionAula sesion = sesion(EstadoSesion.COMPLETADA);
        sesion.setIntentosPermitidos(2);
        sesion.agregarResultado(new ResultadoIntento("intento-1", 2, 3));
        when(repository.findById(1L)).thenReturn(Optional.of(sesion));
        when(repository.save(sesion)).thenReturn(sesion);

        SesionResponse respuesta = service.registrarResultado(1L, new ResultadoRequest("intento-2", 3, 3));

        assertThat(respuesta.resultados()).hasSize(2);
    }

    @Test
    void registrarResultadoEnSesionCreadaLanzaReglaDeNegocio() {
        when(repository.findById(1L)).thenReturn(Optional.of(sesion(EstadoSesion.CREADA)));

        assertThatThrownBy(() -> service.registrarResultado(1L, new ResultadoRequest("intento-1", 2, 3)))
                .isInstanceOf(ReglaNegocioException.class);
    }

    @Test
    void actualizarObservacionGuardaElTextoSinEspacios() {
        SesionAula sesion = sesion(EstadoSesion.COMPLETADA);
        when(repository.findById(1L)).thenReturn(Optional.of(sesion));
        when(repository.save(sesion)).thenReturn(sesion);

        SesionResponse respuesta = service.actualizarObservacion(1L, "  Necesitó apoyo en la pregunta 2.  ");

        assertThat(respuesta.observacion()).isEqualTo("Necesitó apoyo en la pregunta 2.");
    }

    @Test
    void finalizarCierraLaSesionYRegistraLaFecha() {
        SesionAula sesion = sesion(EstadoSesion.COMPLETADA);
        when(repository.findById(1L)).thenReturn(Optional.of(sesion));
        when(repository.save(sesion)).thenReturn(sesion);

        SesionResponse respuesta = service.finalizar(1L);

        assertThat(respuesta.estado()).isEqualTo(EstadoSesion.FINALIZADA);
        assertThat(respuesta.finalizadaEn()).isNotNull();
    }

    @Test
    void eliminarUnaSesionAbiertaLanzaReglaDeNegocio() {
        when(repository.findById(1L)).thenReturn(Optional.of(sesion(EstadoSesion.EN_CURSO)));

        assertThatThrownBy(() -> service.eliminar(1L)).isInstanceOf(ReglaNegocioException.class);
        verify(repository, never()).delete(any());
    }

    @Test
    void eliminarUnaSesionFinalizadaLaBorra() {
        SesionAula sesion = sesion(EstadoSesion.FINALIZADA);
        when(repository.findById(1L)).thenReturn(Optional.of(sesion));

        service.eliminar(1L);

        verify(repository).delete(sesion);
    }

    @Test
    void obtenerUnIdInexistenteLanzaNoEncontrado() {
        when(repository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.obtener(9L)).isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void listarConvierteLosFiltrosVaciosEnNull() {
        when(repository.buscar(null, "Asistente Aula A")).thenReturn(List.of(sesion(EstadoSesion.CREADA)));

        assertThat(service.listar(" ", "Asistente Aula A")).hasSize(1);
    }

    private SesionAula sesion(EstadoSesion estado) {
        SesionAula sesion = new SesionAula();
        sesion.setId(1L);
        sesion.setCodigo("MPAZ-AB12CD");
        sesion.setModulo(Modulo.LECTURA);
        sesion.setCurso("2.° Año Básico");
        sesion.setEstudiante("Estudiante 01 - Aula A");
        sesion.setAsistente("Asistente Aula A");
        sesion.setContenido("Una tarde en el parque · Inicial");
        sesion.setEstado(estado);
        sesion.setObservacion("");
        return sesion;
    }

    private SesionAula conId(SesionAula sesion, Long id) {
        sesion.setId(id);
        return sesion;
    }
}
