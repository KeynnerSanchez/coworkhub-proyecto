package com.coworkhub.business.service;

import com.coworkhub.dto.LoginRequest;
import com.coworkhub.dto.LoginResponse;
import com.coworkhub.dto.PerfilResponse;

public interface AuthService {
    LoginResponse login(LoginRequest request);
    PerfilResponse perfil(String usuario, String rol, Long miembroId);
}
