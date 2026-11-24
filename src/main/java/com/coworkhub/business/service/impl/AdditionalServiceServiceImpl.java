package com.coworkhub.business.service.impl;

import com.coworkhub.business.exception.RecursoNoEncontradoException;
import com.coworkhub.business.exception.ReglaNegocioException;
import com.coworkhub.business.mapper.Mapeador;
import com.coworkhub.business.service.AdditionalServiceService;
import com.coworkhub.dto.AdditionalServiceRequest;
import com.coworkhub.dto.AdditionalServiceResponse;
import com.coworkhub.persistence.entity.AdditionalService;
import com.coworkhub.persistence.repository.AdditionalServiceRepository;
import com.coworkhub.persistence.repository.BookingItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdditionalServiceServiceImpl implements AdditionalServiceService {

    private final AdditionalServiceRepository serviceRepository;
    private final BookingItemRepository itemRepository;

    public AdditionalServiceServiceImpl(AdditionalServiceRepository serviceRepository,
                                        BookingItemRepository itemRepository) {
        this.serviceRepository = serviceRepository;
        this.itemRepository = itemRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdditionalServiceResponse> findAll() {
        return serviceRepository.findAll().stream().map(Mapeador::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AdditionalServiceResponse findById(Long id) {
        return Mapeador.toResponse(buscar(id));
    }

    @Override
    @Transactional
    public AdditionalServiceResponse create(AdditionalServiceRequest request) {
        if (serviceRepository.existsByName(request.name())) {
            throw new ReglaNegocioException("RN-11", "Ya existe un servicio con ese nombre");
        }
        AdditionalService service = new AdditionalService();
        service.setName(request.name());
        service.setUnitPrice(request.unitPrice());
        return Mapeador.toResponse(serviceRepository.save(service));
    }

    @Override
    @Transactional
    public AdditionalServiceResponse update(Long id, AdditionalServiceRequest request) {
        AdditionalService service = buscar(id);
        if (serviceRepository.existsByNameAndIdNot(request.name(), id)) {
            throw new ReglaNegocioException("RN-11", "Ya existe un servicio con ese nombre");
        }
        service.setName(request.name());
        service.setUnitPrice(request.unitPrice());
        return Mapeador.toResponse(serviceRepository.save(service));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        AdditionalService service = buscar(id);
        if (itemRepository.existsByServiceId(id)) {
            throw new ReglaNegocioException("RN-12", "No se puede eliminar el servicio porque esta en reservas");
        }
        serviceRepository.delete(service);
    }

    private AdditionalService buscar(Long id) {
        return serviceRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Servicio adicional no encontrado con id " + id));
    }
}
