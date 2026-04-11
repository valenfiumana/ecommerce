package com.uade.tpo.ecommerce.security;

import java.io.IOException;
import java.time.Instant;
import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Centraliza respuestas JSON para errores de seguridad (antes del controlador; no pasa por GlobalExceptionHandler).
 * <p>
 * {@link AuthenticationEntryPoint}: usuario no autenticado accede a ruta protegida → HTTP 401.<br>
 * {@link AccessDeniedHandler}: usuario autenticado pero sin rol/permiso suficiente → HTTP 403.
 * </p>
 * El cuerpo replica los campos de {@link com.uade.tpo.ecommerce.dto.ErrorResponseDTO} para que el cliente trate
 * todos los errores de API de forma uniforme.
 */
@Component
// Un solo bean implementa ambas interfaces y se registra dos veces en exceptionHandling(...) del SecurityFilterChain
public class RestSecurityErrorHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException authException) throws IOException, ServletException {
        String detail = authException != null && authException.getMessage() != null
                ? authException.getMessage()
                : "Credenciales ausentes o token no válido";
        writeErrorResponse(response, HttpStatus.UNAUTHORIZED, detail);
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
            AccessDeniedException accessDeniedException) throws IOException, ServletException {
        String detail = accessDeniedException != null && accessDeniedException.getMessage() != null
                ? accessDeniedException.getMessage()
                : "No autorizado para acceder a este recurso";
        writeErrorResponse(response, HttpStatus.FORBIDDEN, detail);
    }

    /**
     * Serializa manualmente el mismo contrato que ErrorResponseDTO (evita depender de ObjectMapper en este componente).
     */
    private static void writeErrorResponse(HttpServletResponse response, HttpStatus status, String message)
            throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        String safeMessage = jsonEscape(message);
        String safeError = jsonEscape(status.getReasonPhrase());
        String body = String.format(Locale.ROOT,
                "{\"timestamp\":\"%s\",\"status\":%d,\"error\":\"%s\",\"message\":\"%s\"}",
                Instant.now().toString(),
                status.value(),
                safeError,
                safeMessage);
        response.getWriter().write(body);
    }

    private static String jsonEscape(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", " ")
                .replace("\n", " ");
    }
}
