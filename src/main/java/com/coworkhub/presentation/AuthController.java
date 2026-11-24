package com.coworkhub.presentation;

import com.coworkhub.business.service.AuthService;
import com.coworkhub.dto.LoginRequest;
import com.coworkhub.dto.LoginResponse;
import com.coworkhub.dto.PerfilResponse;
import com.coworkhub.security.UsuarioAutenticado;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticacion", description = "Login y perfil del usuario")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "Iniciar sesion", description = "Devuelve un token JWT. Es publico: no necesita token.")
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @Operation(summary = "Ver mi perfil", description = "Devuelve usuario, rol y miembroId del token. Cualquier usuario autenticado.")
    @GetMapping("/perfil")
    public PerfilResponse perfil(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return authService.perfil(usuario.getUsername(), usuario.getRole(), usuario.getMiembroId());
    }
}
