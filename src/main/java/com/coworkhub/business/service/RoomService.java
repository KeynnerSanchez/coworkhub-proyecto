package com.coworkhub.business.service;

import com.coworkhub.dto.RoomRequest;
import com.coworkhub.dto.RoomResponse;

import java.util.List;

public interface RoomService {
    List<RoomResponse> findAll(Long sedeId);
    RoomResponse findById(Long id);
    RoomResponse create(RoomRequest request);
    RoomResponse update(Long id, RoomRequest request);
    void delete(Long id);
}
