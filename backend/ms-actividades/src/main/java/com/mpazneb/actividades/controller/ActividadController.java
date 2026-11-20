package com.mpazneb.actividades.controller;

import com.mpazneb.actividades.dto.ActividadRequest;
import com.mpazneb.actividades.dto.ActividadResponse;
import com.mpazneb.actividades.dto.CambioEstadoRequest;
import com.mpazneb.actividades.model.EstadoActividad;
import com.mpazneb.actividades.model.Modulo;
import com.mpazneb.actividades.service.ActividadService;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints REST del catálogo de actividades.
 *
 * GET    /api/actividades              lista (filtros opcionales: modulo, curso, estado)
 * GET    /api/actividades/{id}         una actividad
 * POST   /api/actividades              crea
 * PUT    /api/actividades/{id}         edita
 * PATCH  /api/actividades/{id}/estado  activa o desactiva
 * DELETE /api/actividades/{id}         elimina
 */
@RestController
@RequestMapping("/api/actividades")
@RequiredArgsConstructor
public class ActividadController {

    private final ActividadService service;

    @GetMapping
    public List<ActividadResponse> listar(
            @RequestParam(required = false) Modulo modulo,
            @RequestParam(required = false) String curso,
            @RequestParam(required = false) EstadoActividad estado) {
        return service.listar(modulo, curso, estado);
    }

    @GetMapping("/{id}")
    public ActividadResponse obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @PostMapping
    public ResponseEntity<ActividadResponse> crear(@Valid @RequestBody ActividadRequest request) {
        ActividadResponse creada = service.crear(request);
        return ResponseEntity.created(URI.create("/api/actividades/" + creada.id())).body(creada);
    }

    @PutMapping("/{id}")
    public ActividadResponse actualizar(@PathVariable Long id,
                                        @Valid @RequestBody ActividadRequest request) {
        return service.actualizar(id, request);
    }

    @PatchMapping("/{id}/estado")
    public ActividadResponse cambiarEstado(@PathVariable Long id,
                                           @Valid @RequestBody CambioEstadoRequest request) {
        return service.cambiarEstado(id, request.estado());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
