package com.coworkhub.business.exception;

// Se lanza cuando un id no existe (responde 404)
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
