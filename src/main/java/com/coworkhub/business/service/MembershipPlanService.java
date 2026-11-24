package com.coworkhub.business.service;

import com.coworkhub.dto.MembershipPlanRequest;
import com.coworkhub.dto.MembershipPlanResponse;

import java.util.List;

public interface MembershipPlanService {
    List<MembershipPlanResponse> findAll();
    MembershipPlanResponse findById(Long id);
    MembershipPlanResponse create(MembershipPlanRequest request);
    MembershipPlanResponse update(Long id, MembershipPlanRequest request);
    void delete(Long id);
}
