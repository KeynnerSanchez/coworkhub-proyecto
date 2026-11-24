package com.coworkhub.presentation;

import com.coworkhub.business.service.AvailabilityService;
import com.coworkhub.dto.RoomResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/disponibilidad")
@Tag(name = "Disponibilidad", description = "Salas libres en una fecha y rango de horas")
public class AvailabilityController {

    private final AvailabilityService availabilityService;

    public AvailabilityController(AvailabilityService availabilityService) {
        this.availabilityService = availabilityService;
    }

    @Operation(summary = "Buscar salas disponibles",
            description = "Parametros: sedeId, fecha (yyyy-MM-dd), horaInicio y horaFin (HH:mm). "
                    + "Opcionales: capacidadMinima y equipamientoIds (la sala debe tener todos). Cualquier usuario autenticado.")
    @GetMapping
    public List<RoomResponse> buscar(
            @RequestParam(name = "sedeId") Long sedeId,
            @RequestParam(name = "fecha") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam(name = "horaInicio") @DateTimeFormat(pattern = "HH:mm") LocalTime horaInicio,
            @RequestParam(name = "horaFin") @DateTimeFormat(pattern = "HH:mm") LocalTime horaFin,
            @RequestParam(name = "capacidadMinima", required = false) Integer capacidadMinima,
            @RequestParam(name = "equipamientoIds", required = false) List<Long> equipamientoIds) {
        return availabilityService.findAvailable(sedeId, fecha, horaInicio, horaFin, capacidadMinima, equipamientoIds);
    }
}
