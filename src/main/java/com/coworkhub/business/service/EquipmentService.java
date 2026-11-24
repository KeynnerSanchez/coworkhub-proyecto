package com.coworkhub.business.service;

import com.coworkhub.dto.EquipmentRequest;
import com.coworkhub.dto.EquipmentResponse;

import java.util.List;

public interface EquipmentService {
    List<EquipmentResponse> findAll();
    EquipmentResponse findById(Long id);
    EquipmentResponse create(EquipmentRequest request);
    EquipmentResponse update(Long id, EquipmentRequest request);
    void delete(Long id);
}
