package com.coworkhub.dto;

import com.coworkhub.persistence.enums.RoomType;

import java.math.BigDecimal;
import java.util.List;

public record RoomResponse(Long id, String name, RoomType type, int capacity, BigDecimal hourlyRate,
                           boolean active, Long sedeId, String sedeName, List<EquipmentResponse> equipamientos) {
}
