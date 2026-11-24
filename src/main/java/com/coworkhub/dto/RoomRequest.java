package com.coworkhub.dto;

import com.coworkhub.persistence.enums.RoomType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.Set;

public record RoomRequest(
        @Schema(example = "Sala Pacifico")
        @NotBlank(message = "El nombre es obligatorio") String name,
        @Schema(example = "MEETING_ROOM")
        @NotNull(message = "El tipo es obligatorio") RoomType type,
        @Schema(example = "8")
        @NotNull(message = "La capacidad es obligatoria")
        @Min(value = 1, message = "La capacidad debe ser al menos 1") Integer capacity,
        @Schema(example = "35000")
        @NotNull(message = "La tarifa por hora es obligatoria")
        @DecimalMin(value = "0.01", message = "La tarifa debe ser mayor que 0") BigDecimal hourlyRate,
        @Schema(example = "true")
        Boolean active,
        @Schema(example = "1")
        @NotNull(message = "La sede es obligatoria") Long sedeId,
        @Schema(example = "[1, 2]")
        Set<Long> equipamientoIds) {
}
