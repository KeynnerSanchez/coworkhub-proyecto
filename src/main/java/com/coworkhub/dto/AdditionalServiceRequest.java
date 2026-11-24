package com.coworkhub.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record AdditionalServiceRequest(
        @NotBlank(message = "El nombre es obligatorio") String name,
        @NotNull(message = "El precio unitario es obligatorio")
        @DecimalMin(value = "0.0", message = "El precio no puede ser negativo") BigDecimal unitPrice) {
}
