package com.coworkhub.business.service;

import com.coworkhub.dto.ConsumptionResponse;
import com.coworkhub.dto.MemberRequest;
import com.coworkhub.dto.MemberResponse;
import com.coworkhub.dto.MemberUpdateRequest;
import com.coworkhub.persistence.enums.MemberStatus;

import java.time.YearMonth;
import java.util.List;

public interface MemberService {
    List<MemberResponse> findAll(String nombre, MemberStatus estado);
    MemberResponse findById(Long id);
    MemberResponse create(MemberRequest request);
    MemberResponse update(Long id, MemberUpdateRequest request);
    MemberResponse suspend(Long id);
    MemberResponse activate(Long id);
    void delete(Long id);
    ConsumptionResponse consumption(Long id, YearMonth mes);
}
