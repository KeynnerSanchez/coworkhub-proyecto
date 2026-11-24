package com.coworkhub.dto;

import java.math.BigDecimal;

public record BookingItemResponse(Long servicioId, String servicioNombre, int quantity,
                                  BigDecimal appliedUnitPrice, BigDecimal subtotal) {
}
