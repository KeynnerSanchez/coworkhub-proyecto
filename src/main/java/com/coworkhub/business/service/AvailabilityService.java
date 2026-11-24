package com.coworkhub.business.service;

import com.coworkhub.dto.RoomResponse;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface AvailabilityService {
    List<RoomResponse> findAvailable(Long sedeId, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin,
                                     Integer capacidadMinima, List<Long> equipamientoIds);
}
