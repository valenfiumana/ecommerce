package com.uade.tpo.ecommerce.dto;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Cuerpo JSON uniforme para errores de la API: {@link com.uade.tpo.ecommerce.exception.GlobalExceptionHandler}
 * y, para 401/403, {@link com.uade.tpo.ecommerce.security.RestSecurityErrorHandler} (mismos campos, distinto origen).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponseDTO {

    /** Momento UTC en que ocurrió el error */
    private Instant timestamp;
    /** Código HTTP (ej. 404, 400) */
    private int status;
    /** Tipo de error en una sola frase (ej. "Not Found") */
    private String error;
    /** Mensaje descriptivo para el cliente */
    private String message;
}
