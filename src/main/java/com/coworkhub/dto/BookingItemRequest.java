package com.coworkhub.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record BookingItemRequest(
        @Schema(example = "1")
        @NotNull(message = "El servicio es obligatorio") Long servicioId,
        @Schema(example = "2")
        @NotNull(message = "La cantidad es obligatoria")
        @Min(value = 1, message = "La cantidad debe ser al menos 1") Integer quantity) {
}
