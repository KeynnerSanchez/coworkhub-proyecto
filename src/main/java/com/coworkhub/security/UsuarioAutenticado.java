package com.coworkhub.security;

// Datos del usuario que viene en el token. Se guarda en el SecurityContext durante la peticion
public class UsuarioAutenticado {

    private final String username;
    private final String role;
    private final Long miembroId; // null si es ADMIN o RECEPTIONIST

    public UsuarioAutenticado(String username, String role, Long miembroId) {
        this.username = username;
        this.role = role;
        this.miembroId = miembroId;
    }

    public String getUsername() { return username; }
    public String getRole() { return role; }
    public Long getMiembroId() { return miembroId; }
}
