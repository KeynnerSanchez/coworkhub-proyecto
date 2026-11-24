package com.coworkhub.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MemberUpdateRequest(
        @NotBlank(message = "El nombre completo es obligatorio") String fullName,
        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo no tiene un formato valido") String email,
        @NotNull(message = "El plan es obligatorio") Long planId) {
}
