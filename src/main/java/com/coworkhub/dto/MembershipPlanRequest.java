package com.coworkhub.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MembershipPlanRequest(
        @NotBlank(message = "El nombre es obligatorio") String name,
        @NotNull(message = "Las horas incluidas son obligatorias")
        @Min(value = 0, message = "Las horas incluidas no pueden ser negativas") Integer includedHoursPerMonth,
        @NotNull(message = "El descuento es obligatorio")
        @Min(value = 0, message = "El descuento minimo es 0")
        @Max(value = 100, message = "El descuento maximo es 100") Integer overageDiscount,
        @NotNull(message = "El maximo de reservas activas es obligatorio")
        @Min(value = 1, message = "El maximo de reservas activas debe ser al menos 1") Integer maxActiveBookings) {
}
