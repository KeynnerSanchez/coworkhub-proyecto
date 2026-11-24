package com.coworkhub.dto;

import java.math.BigDecimal;

public record ConsumptionResponse(Long miembroId, String mes, BigDecimal horasIncluidas, BigDecimal horasUsadas,
                                  BigDecimal horasRestantes, BigDecimal horasExcedentes) {
}
