package com.ford.vehicleapi.exception;

/** Recurso inexistente -> HTTP 404. */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String resource, Object id) {
        super("%s com id %s não encontrado".formatted(resource, id));
    }
}
