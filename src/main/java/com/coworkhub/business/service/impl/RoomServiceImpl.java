package com.coworkhub.business.service.impl;

import com.coworkhub.business.exception.RecursoNoEncontradoException;
import com.coworkhub.business.exception.ReglaNegocioException;
import com.coworkhub.business.mapper.Mapeador;
import com.coworkhub.business.service.RoomService;
import com.coworkhub.dto.RoomRequest;
import com.coworkhub.dto.RoomResponse;
import com.coworkhub.persistence.entity.Equipment;
import com.coworkhub.persistence.entity.Location;
import com.coworkhub.persistence.entity.Room;
import com.coworkhub.persistence.repository.BookingRepository;
import com.coworkhub.persistence.repository.EquipmentRepository;
import com.coworkhub.persistence.repository.LocationRepository;
import com.coworkhub.persistence.repository.RoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final LocationRepository locationRepository;
    private final EquipmentRepository equipmentRepository;
    private final BookingRepository bookingRepository;

    public RoomServiceImpl(RoomRepository roomRepository, LocationRepository locationRepository,
                           EquipmentRepository equipmentRepository, BookingRepository bookingRepository) {
        this.roomRepository = roomRepository;
        this.locationRepository = locationRepository;
        this.equipmentRepository = equipmentRepository;
        this.bookingRepository = bookingRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomResponse> findAll(Long sedeId) {
        List<Room> salas = (sedeId == null) ? roomRepository.findAll() : roomRepository.findByLocationId(sedeId);
        return salas.stream().map(Mapeador::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RoomResponse findById(Long id) {
        return Mapeador.toResponse(buscar(id));
    }

    @Override
    @Transactional
    public RoomResponse create(RoomRequest request) {
        Location location = buscarSede(request.sedeId());
        if (roomRepository.existsByLocationIdAndName(location.getId(), request.name())) {
            throw new ReglaNegocioException("RN-11", "Ya existe una sala con ese nombre en la sede");
        }
        Room room = new Room();
        copiar(request, room, location);
        return Mapeador.toResponse(roomRepository.save(room));
    }

    @Override
    @Transactional
    public RoomResponse update(Long id, RoomRequest request) {
        Room room = buscar(id);
        Location location = buscarSede(request.sedeId());
        if (roomRepository.existsByLocationIdAndNameAndIdNot(location.getId(), request.name(), id)) {
            throw new ReglaNegocioException("RN-11", "Ya existe una sala con ese nombre en la sede");
        }
        copiar(request, room, location);
        return Mapeador.toResponse(roomRepository.save(room));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Room room = buscar(id);
        // RN-12: no se borra una sala que tenga reservas
        if (bookingRepository.existsByRoomId(id)) {
            throw new ReglaNegocioException("RN-12", "No se puede eliminar la sala porque tiene reservas");
        }
        roomRepository.delete(room);
    }

    private Room buscar(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Sala no encontrada con id " + id));
    }

    private Location buscarSede(Long id) {
        return locationRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Sede no encontrada con id " + id));
    }

    private void copiar(RoomRequest request, Room room, Location location) {
        room.setName(request.name());
        room.setType(request.type());
        room.setCapacity(request.capacity());
        room.setHourlyRate(request.hourlyRate());
        // si no mandan "active", la sala queda activa
        room.setActive(request.active() == null || request.active());
        room.setLocation(location);

        Set<Long> ids = request.equipamientoIds() == null ? new HashSet<>() : request.equipamientoIds();
        List<Equipment> encontrados = equipmentRepository.findAllById(ids);
        if (encontrados.size() != ids.size()) {
            throw new RecursoNoEncontradoException("Alguno de los equipamientos no existe");
        }
        room.getEquipment().clear();
        room.getEquipment().addAll(encontrados);
    }
}
