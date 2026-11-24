package com.coworkhub.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Clock;

@Configuration
public class AppConfig {

    // Reloj inyectable: en las pruebas se puede fijar la hora
    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }

    // Las contrasenas se guardan con BCrypt
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
