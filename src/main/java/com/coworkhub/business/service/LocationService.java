package com.coworkhub.business.service;

import com.coworkhub.dto.LocationRequest;
import com.coworkhub.dto.LocationResponse;

import java.util.List;

public interface LocationService {
    List<LocationResponse> findAll();
    LocationResponse findById(Long id);
    LocationResponse create(LocationRequest request);
    LocationResponse update(Long id, LocationRequest request);
    void delete(Long id);
}
