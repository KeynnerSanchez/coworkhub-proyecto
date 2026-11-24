package com.coworkhub.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Clock;

// Responde 403 cuando el usuario esta autenticado pero no tiene permiso
@Component
public class RestAccessDeniedHandler implements AccessDeniedHandler {

    private final Clock clock;

    public RestAccessDeniedHandler(Clock clock) {
        this.clock = clock;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException e)
            throws IOException {
        JsonErrorWriter.escribir(response, 403, "ACCESO_DENEGADO", "No tienes permiso para esta accion", clock);
    }
}
