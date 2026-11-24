package com.coworkhub.business.exception;

// Usuario o contrasena incorrectos (responde 401)
public class CredencialesInvalidasException extends RuntimeException {

    public CredencialesInvalidasException() {
        super("Usuario o contrasena incorrectos");
    }
}
