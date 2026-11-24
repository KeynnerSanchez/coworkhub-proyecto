package com.coworkhub.dto;

import jakarta.validation.constraints.NotBlank;

public record EquipmentRequest(@NotBlank(message = "El nombre es obligatorio") String name) {
}
