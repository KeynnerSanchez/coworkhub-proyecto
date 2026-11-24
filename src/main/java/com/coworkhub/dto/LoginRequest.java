package com.coworkhub.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @Schema(example = "admin")
        @JsonAlias({"usuario", "user"})
        @NotBlank(message = "El usuario es obligatorio") String username,

        @Schema(example = "Admin123")
        @JsonAlias({"contrasena", "clave"})
        @NotBlank(message = "La contrasena es obligatoria") String password) {
}
