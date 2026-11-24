package com.coworkhub.business.exception;

// Se lanza cuando se rompe una regla de negocio (responde 409). Guarda el codigo RN-xx
public class ReglaNegocioException extends RuntimeException {

    private final String codigo;

    public ReglaNegocioException(String codigo, String mensaje) {
        super(mensaje);
        this.codigo = codigo;
    }

    public String getCodigo() {
        return codigo;
    }
}
