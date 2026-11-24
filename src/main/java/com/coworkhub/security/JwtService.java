package com.coworkhub.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey clave;
    private final long minutos;
    private final Clock clock;

    public JwtService(@Value("${app.jwt.secret}") String secreto,
                      @Value("${app.jwt.expiration-minutes}") long minutos,
                      Clock clock) {
        this.clave = Keys.hmacShaKeyFor(secreto.getBytes(StandardCharsets.UTF_8));
        this.minutos = minutos;
        this.clock = clock;
    }

    public String generar(String username, String rol, Long miembroId) {
        Instant ahora = Instant.now(clock);
        return Jwts.builder()
                .subject(username)
                .claim("rol", rol)
                .claim("miembroId", miembroId)
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(ahora.plusSeconds(minutos * 60)))
                .signWith(clave)
                .compact();
    }

    // Lanza JwtException si el token esta alterado o vencido
    public Claims leer(String token) {
        return Jwts.parser()
                .verifyWith(clave)
                .clock(() -> Date.from(Instant.now(clock)))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public long getExpiracionSegundos() {
        return minutos * 60;
    }
}
