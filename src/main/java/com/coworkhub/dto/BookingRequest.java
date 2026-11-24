package com.coworkhub.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

public record BookingRequest(
        @Schema(example = "1")
        @NotNull(message = "El miembro es obligatorio") Long miembroId,
        @Schema(example = "2")
        @NotNull(message = "La sala es obligatoria") Long salaId,
        @Schema(example = "2030-06-03T10:00:00")
        @NotNull(message = "La hora de inicio es obligatoria") LocalDateTime startTime,
        @Schema(example = "2030-06-03T12:00:00")
        @NotNull(message = "La hora de fin es obligatoria") LocalDateTime endTime,
        @Schema(example = "3")
        @NotNull(message = "El numero de asistentes es obligatorio")
        @Min(value = 1, message = "Debe haber al menos 1 asistente") Integer attendees,
        @Valid List<BookingItemRequest> services) {
}
