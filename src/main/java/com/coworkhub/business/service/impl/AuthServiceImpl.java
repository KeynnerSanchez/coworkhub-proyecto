package com.coworkhub.business.service.impl;

import com.coworkhub.business.exception.CredencialesInvalidasException;
import com.coworkhub.business.service.AuthService;
import com.coworkhub.dto.LoginRequest;
import com.coworkhub.dto.LoginResponse;
import com.coworkhub.dto.PerfilResponse;
import com.coworkhub.persistence.entity.Member;
import com.coworkhub.persistence.entity.UserAccount;
import com.coworkhub.persistence.repository.MemberRepository;
import com.coworkhub.persistence.repository.UserAccountRepository;
import com.coworkhub.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserAccountRepository accountRepository;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthServiceImpl(UserAccountRepository accountRepository, MemberRepository memberRepository,
                           PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.accountRepository = accountRepository;
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        UserAccount account = accountRepository.findByUsername(request.username())
                .orElseThrow(CredencialesInvalidasException::new);
        if (!passwordEncoder.matches(request.password(), account.getPassword())) {
            throw new CredencialesInvalidasException();
        }
        // si el usuario es un miembro, el token lleva el id del miembro
        Long miembroId = memberRepository.findByAccountId(account.getId()).map(Member::getId).orElse(null);
        String token = jwtService.generar(account.getUsername(), account.getRole().name(), miembroId);
        return new LoginResponse(token, "Bearer", jwtService.getExpiracionSegundos());
    }

    @Override
    public PerfilResponse perfil(String usuario, String rol, Long miembroId) {
        return new PerfilResponse(usuario, rol, miembroId);
    }
}
