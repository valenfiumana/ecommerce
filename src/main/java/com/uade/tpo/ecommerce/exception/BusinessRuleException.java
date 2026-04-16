package com.uade.tpo.ecommerce.exception;

// Algo de negocio no se cumple (ej: pedís más stock del que hay). Devuelve 400.
public class BusinessRuleException extends RuntimeException {

    // Mensaje que verá el cliente en el JSON de error.
    public BusinessRuleException(String message) {
        super(message);
    }

    // Variante con causa encadenada (útil para logs).
    public BusinessRuleException(String message, Throwable cause) {
        super(message, cause);
    }
}
