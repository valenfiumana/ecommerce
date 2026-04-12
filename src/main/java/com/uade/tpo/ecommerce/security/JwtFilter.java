package com.uade.tpo.ecommerce.security;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Filtro que valida el JWT en cada request y, si es válido, registra al usuario en el {@link SecurityContextHolder}.
 * <p>
 * No sustituye el login: solo <i>lee</i> el token que el cliente ya obtuvo en {@code POST /api/auth/login}.
 * Si el header {@code Authorization} falta, es inválido o está vencido, no se setea autenticación: la petición sigue
 * como usuario anónimo. Las rutas marcadas {@code permitAll} en {@code SecurityConfig} siguen accesibles; las
 * {@code authenticated} fallarán después con 401 JSON vía {@link com.uade.tpo.ecommerce.security.RestSecurityErrorHandler}.
 * </p>
 * <p>Se registra con {@code addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)} para ejecutarse
 * temprano en la cadena de Spring Security.</p>
 */
@Component
// Este filtro se ejecuta antes de llegar al controller (y antes del filtro usuario/contraseña por defecto de Spring).
// Fue configurado en SecurityConfig con: addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Este método se ejecuta en cada petición HTTP para verificar si existe un token JWT válido.
     * <p>Dónde interviene:</p>
     * <ul>
     *   <li>Spring Security pasa la request por una cadena de filtros; este es uno de los primeros.</li>
     *   <li>Si el token es válido, rellena {@link SecurityContextHolder} para que controladores y servicios vean al usuario.</li>
     *   <li>Si no hay token o es inválido, la cadena sigue: rutas públicas no requieren autenticación.</li>
     * </ul>
     * <p>Cadena típica hasta el controller: cliente HTTP → filtros Spring (incl. este JwtFilter) →
     * {@code DispatcherServlet} → tu {@code @RestController}.</p>
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 1. Obtiene el encabezado "Authorization" de la petición.
        // En el header suele venir algo como:
        //   Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJlbWFpbEBkb21pbmlvLmNvbSIsInJvbGVzIjoiUk9MRV9VU0VSIiwiaWF0Ijox... (payload firmado) ...signature
        // El prefijo "Bearer " (con espacio) es el estándar OAuth2/RFC 6750 para enviar access tokens.
        String header = request.getHeader("Authorization");

        // 2. Verifica si el encabezado existe y si comienza con "Bearer ".
        if (header != null && header.startsWith("Bearer ")) {
            // 3. Extrae la parte del JWT de la cabecera de autorización, eliminando el prefijo "Bearer " (7 caracteres).
            // Queda solo el string del token, por ejemplo: eyJhbGciOiJIUzI1NiJ9...
            String token = header.substring(7);

            // 4. Valida el token usando jwtUtil: firma, expiración, estructura. Si falla, no entramos al if interno.
            if (jwtUtil.validateToken(token)) {
                // 5. Si el token es válido, extrae el nombre de usuario (en nuestro caso el email) y los roles embebidos en el JWT.
                String username = jwtUtil.getUsername(token);
                Set<String> roles = jwtUtil.getRoles(token);

                // 6. Transforma el conjunto de roles (strings) en la lista de GrantedAuthority que Spring Security usa
                //    para hasRole("ADMIN"), hasAuthority, etc. Cada string del token debe coincidir con lo que espera SecurityConfig.
                var authorities = roles.stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

                // 7. Crea el objeto Authentication ya autenticado:
                //    - principal = email (username lógico)
                //    - credentials = null (la contraseña no viaja en cada request; la confianza viene del JWT verificado)
                //    - authorities = roles del token
                var auth = new UsernamePasswordAuthenticationToken(username, null, authorities);

                // 8. Registra la autenticación en el contexto thread-local de Spring Security para el resto de la petición.
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
            // Si el token es inválido o expiró: no se setea Authentication (usuario anónimo desde el punto de vista de Spring).
            // No cortamos la cadena aquí: las rutas permitAll siguen; las protegidas fallarán más adelante con 401 JSON.
        }

        // 9. Pasa la petición al siguiente filtro (y eventualmente al controller). Siempre se llama para no “cortar” la cadena.
        filterChain.doFilter(request, response);
    }
}
