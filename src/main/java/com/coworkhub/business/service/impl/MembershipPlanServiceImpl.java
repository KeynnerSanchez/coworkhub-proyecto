package com.coworkhub.business.service.impl;

import com.coworkhub.business.exception.RecursoNoEncontradoException;
import com.coworkhub.business.exception.ReglaNegocioException;
import com.coworkhub.business.mapper.Mapeador;
import com.coworkhub.business.service.MembershipPlanService;
import com.coworkhub.dto.MembershipPlanRequest;
import com.coworkhub.dto.MembershipPlanResponse;
import com.coworkhub.persistence.entity.MembershipPlan;
import com.coworkhub.persistence.repository.MemberRepository;
import com.coworkhub.persistence.repository.MembershipPlanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MembershipPlanServiceImpl implements MembershipPlanService {

    private final MembershipPlanRepository planRepository;
    private final MemberRepository memberRepository;

    public MembershipPlanServiceImpl(MembershipPlanRepository planRepository, MemberRepository memberRepository) {
        this.planRepository = planRepository;
        this.memberRepository = memberRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<MembershipPlanResponse> findAll() {
        return planRepository.findAll().stream().map(Mapeador::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public MembershipPlanResponse findById(Long id) {
        return Mapeador.toResponse(buscar(id));
    }

    @Override
    @Transactional
    public MembershipPlanResponse create(MembershipPlanRequest request) {
        if (planRepository.existsByName(request.name())) {
            throw new ReglaNegocioException("RN-11", "Ya existe un plan con ese nombre");
        }
        MembershipPlan plan = new MembershipPlan();
        copiar(request, plan);
        return Mapeador.toResponse(planRepository.save(plan));
    }

    @Override
    @Transactional
    public MembershipPlanResponse update(Long id, MembershipPlanRequest request) {
        MembershipPlan plan = buscar(id);
        if (planRepository.existsByNameAndIdNot(request.name(), id)) {
            throw new ReglaNegocioException("RN-11", "Ya existe un plan con ese nombre");
        }
        copiar(request, plan);
        return Mapeador.toResponse(planRepository.save(plan));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        MembershipPlan plan = buscar(id);
        if (memberRepository.existsByPlanId(id)) {
            throw new ReglaNegocioException("RN-12", "No se puede eliminar el plan porque tiene miembros");
        }
        planRepository.delete(plan);
    }

    private MembershipPlan buscar(Long id) {
        return planRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Plan no encontrado con id " + id));
    }

    private void copiar(MembershipPlanRequest request, MembershipPlan plan) {
        plan.setName(request.name());
        plan.setIncludedHoursPerMonth(request.includedHoursPerMonth());
        plan.setOverageDiscount(request.overageDiscount());
        plan.setMaxActiveBookings(request.maxActiveBookings());
    }
}
