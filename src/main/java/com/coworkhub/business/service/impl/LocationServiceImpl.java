package com.coworkhub.business.service.impl;

import com.coworkhub.business.exception.RecursoNoEncontradoException;
import com.coworkhub.business.exception.ReglaNegocioException;
import com.coworkhub.business.exception.SolicitudInvalidaException;
import com.coworkhub.business.mapper.Mapeador;
import com.coworkhub.business.service.LocationService;
import com.coworkhub.dto.LocationRequest;
import com.coworkhub.dto.LocationResponse;
import com.coworkhub.persistence.entity.Location;
import com.coworkhub.persistence.repository.LocationRepository;
import com.coworkhub.persistence.repository.RoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LocationServiceImpl implements LocationService {

    private final LocationRepository locationRepository;
    private final RoomRepository roomRepository;

    public LocationServiceImpl(LocationRepository locationRepository, RoomRepository roomRepository) {
        this.locationRepository = locationRepository;
        this.roomRepository = roomRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<LocationResponse> findAll() {
        return locationRepository.findAll().stream().map(Mapeador::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public LocationResponse findById(Long id) {
        return Mapeador.toResponse(buscar(id));
    }

    @Override
    @Transactional
    public LocationResponse create(LocationRequest request) {
        validarHorario(request);
        Location location = new Location();
        copiar(request, location);
        return Mapeador.toResponse(locationRepository.save(location));
    }

    @Override
    @Transactional
    public LocationResponse update(Long id, LocationRequest request) {
        validarHorario(request);
        Location location = buscar(id);
        copiar(request, location);
        return Mapeador.toResponse(locationRepository.save(location));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Location location = buscar(id);
        // RN-12: no se borra una sede que tenga salas
        if (roomRepository.existsByLocationId(id)) {
            throw new ReglaNegocioException("RN-12", "No se puede eliminar la sede porque tiene salas");
        }
        locationRepository.delete(location);
    }

    private Location buscar(Long id) {
        return locationRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Sede no encontrada con id " + id));
    }

    private void validarHorario(LocationRequest request) {
        if (!request.closingTime().isAfter(request.openingTime())) {
            throw new SolicitudInvalidaException("La hora de cierre debe ser mayor que la hora de apertura");
        }
    }

    private void copiar(LocationRequest request, Location location) {
        location.setName(request.name());
        location.setCity(request.city());
        location.setAddress(request.address());
        location.setOpeningTime(request.openingTime());
        location.setClosingTime(request.closingTime());
    }
}
