package com.ford.vehicleapi.exception;

/** Serviço externo indisponível ou com resposta inesperada -> HTTP 502. */
public class ExternalServiceException extends RuntimeException {

    public ExternalServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
