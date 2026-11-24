package com.coworkhub.business.service.impl;

import com.coworkhub.business.exception.RecursoNoEncontradoException;
import com.coworkhub.business.exception.SolicitudInvalidaException;
import com.coworkhub.business.mapper.Mapeador;
import com.coworkhub.business.service.AvailabilityService;
import com.coworkhub.dto.RoomResponse;
import com.coworkhub.persistence.entity.Equipment;
import com.coworkhub.persistence.entity.Room;
import com.coworkhub.persistence.enums.BookingStatus;
import com.coworkhub.persistence.repository.BookingRepository;
import com.coworkhub.persistence.repository.LocationRepository;
import com.coworkhub.persistence.repository.RoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AvailabilityServiceImpl implements AvailabilityService {

    private static final List<BookingStatus> ACTIVOS = List.of(BookingStatus.PENDING, BookingStatus.CONFIRMED);

    private final RoomRepository roomRepository;
    private final LocationRepository locationRepository;
    private final BookingRepository bookingRepository;

    public AvailabilityServiceImpl(RoomRepository roomRepository, LocationRepository locationRepository,
                                   BookingRepository bookingRepository) {
        this.roomRepository = roomRepository;
        this.locationRepository = locationRepository;
        this.bookingRepository = bookingRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomResponse> findAvailable(Long sedeId, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin,
                                            Integer capacidadMinima, List<Long> equipamientoIds) {
        if (!locationRepository.existsById(sedeId)) {
            throw new RecursoNoEncontradoException("Sede no encontrada con id " + sedeId);
        }
        if (!horaFin.isAfter(horaInicio)) {
            throw new SolicitudInvalidaException("La hora de fin debe ser mayor que la hora de inicio");
        }
        LocalDateTime inicio = LocalDateTime.of(fecha, horaInicio);
        LocalDateTime fin = LocalDateTime.of(fecha, horaFin);

        // una sola consulta con los ids de las salas ocupadas en ese rango
        Set<Long> ocupadas = new HashSet<>(bookingRepository.findBusyRoomIds(sedeId, ACTIVOS, inicio, fin));
        Set<Long> pedidos = (equipamientoIds == null) ? Set.of() : new HashSet<>(equipamientoIds);

        return roomRepository.findByLocationIdAndActiveTrue(sedeId).stream()
                .filter(sala -> !ocupadas.contains(sala.getId()))
                .filter(sala -> capacidadMinima == null || sala.getCapacity() >= capacidadMinima)
                .filter(sala -> tieneEquipamiento(sala, pedidos))
                .map(Mapeador::toResponse)
                .toList();
    }

    // la sala debe tener TODOS los equipamientos pedidos
    private boolean tieneEquipamiento(Room sala, Set<Long> pedidos) {
        Set<Long> tiene = sala.getEquipment().stream().map(Equipment::getId).collect(Collectors.toSet());
        return tiene.containsAll(pedidos);
    }
}
