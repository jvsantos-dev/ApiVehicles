package com.ford.vehicleapi.exception;

/** Conflito com o estado atual dos recursos (ex.: duplicidade) -> HTTP 409. */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
