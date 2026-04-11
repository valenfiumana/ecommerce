package com.uade.tpo.ecommerce.exception;

/**
 * Excepción de dominio para argumentos de negocio o validación que no cumplen reglas (400).
 * Ejemplos: precio negativo, email duplicado en registro, rangos inválidos, reglas que no son
 * "recurso inexistente" (eso corresponde a {@link ResourceNotFoundException}).
 */
public class ArgumentInvalidException extends RuntimeException {

    public ArgumentInvalidException(String message) {
        super(message);
    }

    public ArgumentInvalidException(String message, Throwable cause) {
        super(message, cause);
    }
}
