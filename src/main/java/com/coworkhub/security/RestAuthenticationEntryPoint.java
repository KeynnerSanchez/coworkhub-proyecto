package com.coworkhub.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Clock;

// Responde 401 cuando no hay token o el token no sirve
@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final Clock clock;

    public RestAuthenticationEntryPoint(Clock clock) {
        this.clock = clock;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException e)
            throws IOException {
        JsonErrorWriter.escribir(response, 401, "NO_AUTENTICADO",
                "Falta el token o el token es invalido o esta vencido", clock);
    }
}
