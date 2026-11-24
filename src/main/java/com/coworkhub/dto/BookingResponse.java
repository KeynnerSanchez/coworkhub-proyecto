package com.coworkhub.dto;

import com.coworkhub.persistence.enums.BookingStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record BookingResponse(Long id, Long miembroId, String miembroNombre, Long salaId, String salaNombre,
                              LocalDateTime startTime, LocalDateTime endTime, int attendees, BookingStatus status,
                              BigDecimal roomCost, BigDecimal servicesCost, BigDecimal totalCost,
                              BigDecimal cancellationFee, List<BookingItemResponse> services) {
}
