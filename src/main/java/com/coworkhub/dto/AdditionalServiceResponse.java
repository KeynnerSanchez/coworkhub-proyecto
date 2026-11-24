package com.coworkhub.dto;

import java.math.BigDecimal;

public record AdditionalServiceResponse(Long id, String name, BigDecimal unitPrice) {
}
