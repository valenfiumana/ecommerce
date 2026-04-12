package com.uade.tpo.ecommerce.exception;

import java.time.Instant;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.uade.tpo.ecommerce.dto.ErrorResponseDTO;

// Buena práctica: centralizar el manejo de errores con @ControllerAdvice (evita try/catch repetido en cada controlador).
// @ControllerAdvice: esta clase aplica a todos los @RestController y convierte excepciones en ResponseEntity JSON.
@ControllerAdvice
public class GlobalExceptionHandler {

    private ResponseEntity<ErrorResponseDTO> build(HttpStatus status, String message) {
        ErrorResponseDTO body = ErrorResponseDTO.builder()
                .timestamp(Instant.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .build();
        return ResponseEntity.status(status).body(body);
    }

    /**
     * Recurso no encontrado (Producto, Usuario, Pedido, etc.) unificado en {@link ResourceNotFoundException}.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> manejarRecursoNoEncontrado(ResourceNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    /**
     * Argumentos inválidos de negocio (reglas que no son “no encontrado”).
     */
    @ExceptionHandler(ArgumentInvalidException.class)
    public ResponseEntity<ErrorResponseDTO> manejarArgumentoInvalidoNegocio(ArgumentInvalidException ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    /**
     * Conflicto de estado, p. ej. email ya registrado (registro duplicado). HTTP 409.
     */
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponseDTO> manejarConflicto(ConflictException ex) {
        return build(HttpStatus.CONFLICT, ex.getMessage());
    }

    /**
     * Login con email o contraseña incorrectos (Spring Security).
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponseDTO> manejarCredencialesInvalidas(BadCredentialsException ex) {
        return build(HttpStatus.UNAUTHORIZED, "Credenciales inválidas");
    }

    /**
     * Acceso denegado (HTTP 403): usuario autenticado pero sin permiso para la operación.
     * Cubre {@link AccessDeniedException} lanzada desde servicios (p. ej. producto de otro vendedor).
     * Nota: fallos 403 que ocurren solo en la cadena de filtros de Spring Security siguen yendo a
     * {@link com.uade.tpo.ecommerce.security.RestSecurityErrorHandler}; acá entramos cuando la excepción burbujea desde el controller/servicio.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDTO> manejarAccesoDenegado(AccessDeniedException ex) {
        String mensaje = ex.getMessage() != null && !ex.getMessage().isBlank()
                ? ex.getMessage()
                : "No autorizado para realizar esta operación";
        return build(HttpStatus.FORBIDDEN, mensaje);
    }

    /**
     * Errores de validación de bean (@Valid en controladores): junta los mensajes de cada campo inválido.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> manejarValidacionBean(MethodArgumentNotValidException ex) {
        String mensaje = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return build(HttpStatus.BAD_REQUEST, mensaje);
    }

    /**
     * JSON mal formado o tipos incompatibles (ej. enum o fecha irreconocible).
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseDTO> manejarCuerpoJsonInvalido(HttpMessageNotReadableException ex) {
        return build(HttpStatus.BAD_REQUEST, "Cuerpo de la solicitud inválido o mal formado");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDTO> manejarArgumentoInvalido(IllegalArgumentException ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> manejarErroresGenerales(Exception ex) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno: " + ex.getMessage());
    }
}
