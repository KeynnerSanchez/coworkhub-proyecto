package com.coworkhub.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

public record LocationRequest(
        @Schema(example = "CoWorkHub Sur")
        @NotBlank(message = "El nombre es obligatorio") String name,
        @Schema(example = "Cali")
        @NotBlank(message = "La ciudad es obligatoria") String city,
        @Schema(example = "Avenida 6 # 20-10")
        @NotBlank(message = "La direccion es obligatoria") String address,
        @Schema(example = "07:00:00")
        @NotNull(message = "La hora de apertura es obligatoria") LocalTime openingTime,
        @Schema(example = "21:00:00")
        @NotNull(message = "La hora de cierre es obligatoria") LocalTime closingTime) {
}
