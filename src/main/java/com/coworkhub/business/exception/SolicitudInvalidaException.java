package com.coworkhub.business.exception;

// Se lanza cuando la peticion esta mal armada (responde 400)
public class SolicitudInvalidaException extends RuntimeException {

    public SolicitudInvalidaException(String mensaje) {
        super(mensaje);
    }
}
