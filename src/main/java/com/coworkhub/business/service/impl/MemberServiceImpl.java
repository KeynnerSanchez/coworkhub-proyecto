package com.coworkhub.business.service.impl;

import com.coworkhub.business.calculo.ConsumoHoras;
import com.coworkhub.business.exception.RecursoNoEncontradoException;
import com.coworkhub.business.exception.ReglaNegocioException;
import com.coworkhub.business.mapper.Mapeador;
import com.coworkhub.business.service.MemberService;
import com.coworkhub.dto.ConsumptionResponse;
import com.coworkhub.dto.MemberRequest;
import com.coworkhub.dto.MemberResponse;
import com.coworkhub.dto.MemberUpdateRequest;
import com.coworkhub.persistence.entity.Member;
import com.coworkhub.persistence.entity.MembershipPlan;
import com.coworkhub.persistence.entity.UserAccount;
import com.coworkhub.persistence.enums.MemberStatus;
import com.coworkhub.persistence.enums.Role;
import com.coworkhub.persistence.repository.BookingRepository;
import com.coworkhub.persistence.repository.MemberRepository;
import com.coworkhub.persistence.repository.MembershipPlanRepository;
import com.coworkhub.persistence.repository.UserAccountRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

@Service
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final MembershipPlanRepository planRepository;
    private final UserAccountRepository accountRepository;
    private final BookingRepository bookingRepository;
    private final ConsumoHoras consumoHoras;
    private final PasswordEncoder passwordEncoder;

    public MemberServiceImpl(MemberRepository memberRepository, MembershipPlanRepository planRepository,
                             UserAccountRepository accountRepository, BookingRepository bookingRepository,
                             ConsumoHoras consumoHoras, PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.planRepository = planRepository;
        this.accountRepository = accountRepository;
        this.bookingRepository = bookingRepository;
        this.consumoHoras = consumoHoras;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public List<MemberResponse> findAll(String nombre, MemberStatus estado) {
        boolean hayNombre = nombre != null && !nombre.isBlank();
        List<Member> miembros;
        if (hayNombre && estado != null) {
            miembros = memberRepository.findByFullNameContainingIgnoreCaseAndStatus(nombre, estado);
        } else if (hayNombre) {
            miembros = memberRepository.findByFullNameContainingIgnoreCase(nombre);
        } else if (estado != null) {
            miembros = memberRepository.findByStatus(estado);
        } else {
            miembros = memberRepository.findAll();
        }
        return miembros.stream().map(Mapeador::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public MemberResponse findById(Long id) {
        return Mapeador.toResponse(buscar(id));
    }

    @Override
    @Transactional
    public MemberResponse create(MemberRequest request) {
        // RN-11: documento, correo y usuario son unicos
        if (memberRepository.existsByDocumentNumber(request.documentNumber())) {
            throw new ReglaNegocioException("RN-11", "Ya existe un miembro con ese documento");
        }
        if (memberRepository.existsByEmail(request.email())) {
            throw new ReglaNegocioException("RN-11", "Ya existe un miembro con ese correo");
        }
        if (accountRepository.existsByUsername(request.username())) {
            throw new ReglaNegocioException("RN-11", "Ya existe un usuario con ese nombre");
        }
        MembershipPlan plan = buscarPlan(request.planId());

        // se crea el usuario (rol MEMBER) junto con el miembro
        UserAccount account = new UserAccount();
        account.setUsername(request.username());
        account.setPassword(passwordEncoder.encode(request.password()));
        account.setRole(Role.MEMBER);

        Member member = new Member();
        member.setDocumentNumber(request.documentNumber());
        member.setFullName(request.fullName());
        member.setEmail(request.email());
        member.setStatus(MemberStatus.ACTIVE);
        member.setPlan(plan);
        member.setAccount(account);
        return Mapeador.toResponse(memberRepository.save(member));
    }

    @Override
    @Transactional
    public MemberResponse update(Long id, MemberUpdateRequest request) {
        Member member = buscar(id);
        if (memberRepository.existsByEmailAndIdNot(request.email(), id)) {
            throw new ReglaNegocioException("RN-11", "Ya existe un miembro con ese correo");
        }
        member.setFullName(request.fullName());
        member.setEmail(request.email());
        member.setPlan(buscarPlan(request.planId()));
        return Mapeador.toResponse(memberRepository.save(member));
    }

    @Override
    @Transactional
    public MemberResponse suspend(Long id) {
        Member member = buscar(id);
        member.setStatus(MemberStatus.SUSPENDED);
        return Mapeador.toResponse(memberRepository.save(member));
    }

    @Override
    @Transactional
    public MemberResponse activate(Long id) {
        Member member = buscar(id);
        member.setStatus(MemberStatus.ACTIVE);
        return Mapeador.toResponse(memberRepository.save(member));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Member member = buscar(id);
        if (bookingRepository.existsByMemberId(id)) {
            throw new ReglaNegocioException("RN-12", "No se puede eliminar el miembro porque tiene reservas");
        }
        memberRepository.delete(member);
    }

    @Override
    @Transactional(readOnly = true)
    public ConsumptionResponse consumption(Long id, YearMonth mes) {
        Member member = buscar(id);
        YearMonth mesConsulta = (mes == null) ? YearMonth.now() : mes;

        BigDecimal incluidas = BigDecimal.valueOf(member.getPlan().getIncludedHoursPerMonth());
        BigDecimal usadas = consumoHoras.horasUsadas(id, mesConsulta);
        BigDecimal restantes = incluidas.subtract(usadas).max(BigDecimal.ZERO);
        BigDecimal excedentes = usadas.subtract(incluidas).max(BigDecimal.ZERO);

        return new ConsumptionResponse(id, mesConsulta.toString(), incluidas.setScale(2), usadas, restantes, excedentes);
    }

    private Member buscar(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Miembro no encontrado con id " + id));
    }

    private MembershipPlan buscarPlan(Long id) {
        return planRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Plan no encontrado con id " + id));
    }
}
