package com.uade.tpo.ecommerce.exception;

/**
 * Conflicto con el estado actual del recurso (HTTP 409), p. ej. email ya registrado.
 */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
