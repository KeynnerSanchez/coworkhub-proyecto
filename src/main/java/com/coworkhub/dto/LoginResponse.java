package com.coworkhub.dto;

public record LoginResponse(String token, String tipo, long expiraEnSegundos) {
}
