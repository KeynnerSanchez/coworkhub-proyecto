package com.coworkhub.business.service;

import com.coworkhub.dto.AdditionalServiceRequest;
import com.coworkhub.dto.AdditionalServiceResponse;

import java.util.List;

public interface AdditionalServiceService {
    List<AdditionalServiceResponse> findAll();
    AdditionalServiceResponse findById(Long id);
    AdditionalServiceResponse create(AdditionalServiceRequest request);
    AdditionalServiceResponse update(Long id, AdditionalServiceRequest request);
    void delete(Long id);
}
