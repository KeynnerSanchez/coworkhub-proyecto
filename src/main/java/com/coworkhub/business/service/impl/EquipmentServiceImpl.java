package com.coworkhub.business.service.impl;

import com.coworkhub.business.exception.RecursoNoEncontradoException;
import com.coworkhub.business.exception.ReglaNegocioException;
import com.coworkhub.business.mapper.Mapeador;
import com.coworkhub.business.service.EquipmentService;
import com.coworkhub.dto.EquipmentRequest;
import com.coworkhub.dto.EquipmentResponse;
import com.coworkhub.persistence.entity.Equipment;
import com.coworkhub.persistence.repository.EquipmentRepository;
import com.coworkhub.persistence.repository.RoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EquipmentServiceImpl implements EquipmentService {

    private final EquipmentRepository equipmentRepository;
    private final RoomRepository roomRepository;

    public EquipmentServiceImpl(EquipmentRepository equipmentRepository, RoomRepository roomRepository) {
        this.equipmentRepository = equipmentRepository;
        this.roomRepository = roomRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EquipmentResponse> findAll() {
        return equipmentRepository.findAll().stream().map(Mapeador::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public EquipmentResponse findById(Long id) {
        return Mapeador.toResponse(buscar(id));
    }

    @Override
    @Transactional
    public EquipmentResponse create(EquipmentRequest request) {
        if (equipmentRepository.existsByName(request.name())) {
            throw new ReglaNegocioException("RN-11", "Ya existe un equipamiento con ese nombre");
        }
        Equipment equipment = new Equipment();
        equipment.setName(request.name());
        return Mapeador.toResponse(equipmentRepository.save(equipment));
    }

    @Override
    @Transactional
    public EquipmentResponse update(Long id, EquipmentRequest request) {
        Equipment equipment = buscar(id);
        if (equipmentRepository.existsByNameAndIdNot(request.name(), id)) {
            throw new ReglaNegocioException("RN-11", "Ya existe un equipamiento con ese nombre");
        }
        equipment.setName(request.name());
        return Mapeador.toResponse(equipmentRepository.save(equipment));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Equipment equipment = buscar(id);
        if (roomRepository.existsByEquipmentId(id)) {
            throw new ReglaNegocioException("RN-12", "No se puede eliminar el equipamiento porque lo usan salas");
        }
        equipmentRepository.delete(equipment);
    }

    private Equipment buscar(Long id) {
        return equipmentRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Equipamiento no encontrado con id " + id));
    }
}
