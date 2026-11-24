package com.coworkhub.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

// Revisa al arrancar que la configuracion tenga sentido
@Component
public class ConfiguracionValidator {

    @Value("${app.jwt.secret}")
    private String secreto;

    @Value("${app.jwt.expiration-minutes}")
    private long minutos;

    @Value("${app.cancelacion.porcentaje}")
    private int porcentaje;

    @PostConstruct
    public void validar() {
        if (secreto == null || secreto.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("app.jwt.secret debe tener al menos 32 caracteres");
        }
        if (minutos <= 0) {
            throw new IllegalStateException("app.jwt.expiration-minutes debe ser mayor que 0");
        }
        if (porcentaje < 0 || porcentaje > 100) {
            throw new IllegalStateException("app.cancelacion.porcentaje debe estar entre 0 y 100");
        }
    }
}
