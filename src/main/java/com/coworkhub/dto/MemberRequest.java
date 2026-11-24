package com.coworkhub.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MemberRequest(
        @Schema(example = "6060606060")
        @NotBlank(message = "El documento es obligatorio") String documentNumber,
        @Schema(example = "Pedro Ramirez")
        @NotBlank(message = "El nombre completo es obligatorio") String fullName,
        @Schema(example = "pedro@correo.com")
        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo no tiene un formato valido") String email,
        @Schema(example = "1")
        @NotNull(message = "El plan es obligatorio") Long planId,
        @Schema(example = "pedro")
        @NotBlank(message = "El usuario es obligatorio") String username,
        @Schema(example = "Pedro1234")
        @NotBlank(message = "La contrasena es obligatoria")
        @Size(min = 6, message = "La contrasena debe tener al menos 6 caracteres") String password) {
}
