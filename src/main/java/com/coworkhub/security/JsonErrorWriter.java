package com.coworkhub.security;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;

// Escribe el cuerpo de error JSON a mano (los errores de seguridad ocurren antes de llegar a un controlador)
public final class JsonErrorWriter {

    private JsonErrorWriter() {
    }

    public static void escribir(HttpServletResponse response, int estado, String codigo, String mensaje, Clock clock)
            throws IOException {
        response.setStatus(estado);
        response.setContentType("application/json");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        String json = "{\"error\":\"" + mensaje + "\",\"codigo\":\"" + codigo + "\",\"timestamp\":\""
                + Instant.now(clock) + "\"}";
        response.getWriter().write(json);
    }
}
