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
 * Respuestas JSON para fallos de seguridad que ocurren <b>en la cadena de filtros</b>, antes de llegar al {@code @RestController}.
 * No pasan por {@link com.uade.tpo.ecommerce.exception.GlobalExceptionHandler}.
 * <ul>
 *   <li>{@link AuthenticationEntryPoint#commence} → <b>401</b>: no hay autenticación válida (sin token, token inválido
 *       en ruta protegida, sesión expirada, etc.).</li>
 *   <li>{@link AccessDeniedHandler#handle} → <b>403</b>: el usuario está autenticado pero la regla
 *       {@code hasRole} / {@code authenticated} / método security no autoriza la acción.</li>
 * </ul>
 * El JSON replica los campos de {@link com.uade.tpo.ecommerce.dto.ErrorResponseDTO} para el mismo contrato que el resto de la API.
 */
@Component
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
