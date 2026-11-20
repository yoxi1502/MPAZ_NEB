package com.mpazneb.usuarios.controller;

import com.mpazneb.usuarios.dto.LoginRequest;
import com.mpazneb.usuarios.dto.UsuarioResponse;
import com.mpazneb.usuarios.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * POST /api/auth/login → 200 con la cuenta, 401 si la clave no coincide,
 * 403 si la cuenta está desactivada.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UsuarioService service;

    @PostMapping("/login")
    public UsuarioResponse login(@Valid @RequestBody LoginRequest request) {
        return service.login(request);
    }
}
