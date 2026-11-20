package com.mpazneb.sesiones.controller;

import com.mpazneb.sesiones.dto.IngresoRequest;
import com.mpazneb.sesiones.dto.ObservacionRequest;
import com.mpazneb.sesiones.dto.ResultadoRequest;
import com.mpazneb.sesiones.dto.SesionRequest;
import com.mpazneb.sesiones.dto.SesionResponse;
import com.mpazneb.sesiones.service.SesionService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints REST de las sesiones de aula.
 *
 * GET    /api/sesiones                    historial (filtros opcionales: estudiante, asistente)
 * GET    /api/sesiones/{id}               una sesión con sus resultados
 * POST   /api/sesiones                    crea la sesión y genera el código
 * POST   /api/sesiones/ingreso            el estudiante entra con su código
 * POST   /api/sesiones/{id}/resultados    guarda el resultado de un intento
 * PATCH  /api/sesiones/{id}/observacion   guarda la observación pedagógica
 * PATCH  /api/sesiones/{id}/finalizar     cierra la sesión
 * DELETE /api/sesiones/{id}               elimina una sesión finalizada
 */
@RestController
@RequestMapping("/api/sesiones")
@RequiredArgsConstructor
public class SesionController {

    private final SesionService service;

    @GetMapping
    public List<SesionResponse> listar(@RequestParam(required = false) String estudiante,
                                       @RequestParam(required = false) String asistente) {
        return service.listar(estudiante, asistente);
    }

    @GetMapping("/{id}")
    public SesionResponse obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @PostMapping
    public ResponseEntity<SesionResponse> crear(@Valid @RequestBody SesionRequest request) {
        SesionResponse creada = service.crear(request);
        return ResponseEntity.created(URI.create("/api/sesiones/" + creada.id())).body(creada);
    }

    @PostMapping("/ingreso")
    public SesionResponse ingresar(@Valid @RequestBody IngresoRequest request) {
        return service.ingresar(request);
    }

    @PostMapping("/{id}/resultados")
    public SesionResponse registrarResultado(@PathVariable Long id,
                                             @Valid @RequestBody ResultadoRequest request) {
        return service.registrarResultado(id, request);
    }

    @PatchMapping("/{id}/observacion")
    public SesionResponse actualizarObservacion(@PathVariable Long id,
                                                @Valid @RequestBody ObservacionRequest request) {
        return service.actualizarObservacion(id, request.observacion());
    }

    @PatchMapping("/{id}/finalizar")
    public SesionResponse finalizar(@PathVariable Long id) {
        return service.finalizar(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
