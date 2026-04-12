package com.uade.tpo.ecommerce.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.uade.tpo.ecommerce.model.Role;
import com.uade.tpo.ecommerce.repository.UsuarioRepository;
import com.uade.tpo.ecommerce.security.JwtFilter;
import com.uade.tpo.ecommerce.security.RestSecurityErrorHandler;

import lombok.RequiredArgsConstructor;

/**
 * <p><b>Documentación extendida</b> (complementa los comentarios {@code //} entre anotaciones más abajo).</p>
 * <p>Resumen técnico: CORS para front en otro origen; {@link JwtFilter} antes del filtro usuario/contraseña;
 * reglas {@code permitAll} / {@code authenticated} / {@code hasRole}; {@link RestSecurityErrorHandler} para 401/403 en JSON.</p>
 * <p>Orden de {@code requestMatchers}: Spring aplica la <i>primera</i> regla que coincida (de arriba hacia abajo).</p>
 * <p>GET {@code /api/productos} es público; POST/PUT/DELETE requieren JWT. La autorización “dueño o admin”
 * se aplica en {@link com.uade.tpo.ecommerce.service.ProductoService} (no se confía en un {@code vendedorId} del body).</p>
 * <p>El carrito ({@code /api/cart}) pide JWT en todos los métodos.</p>
 * <p>Perfil: {@code GET/PATCH /api/usuarios/me} con JWT; listado global de usuarios sigue sin implementar.</p>
 */
// Indica que esta clase contiene configuraciones de Spring
@Configuration
// Habilita la seguridad web de Spring Security
@EnableWebSecurity

// Esto no es Spring Security: Genera un constructor con los campos final requeridos lombok 
@RequiredArgsConstructor
public class SecurityConfig {


    private final JwtFilter jwtFilter;
    // Inyección del repositorio de usuarios
    // pueden utilizar también @Autowired
    private final UsuarioRepository usuarioRepository;
    // Respuestas JSON 401/403 coherentes con ErrorResponseDTO (exceptionHandling del SecurityFilterChain)
    private final RestSecurityErrorHandler restSecurityErrorHandler;

    /**
     * Orígenes del front (SPA). Lista separada por comas; configurable con {@code app.cors.allowed-origins}.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${app.cors.allowed-origins:http://localhost:5173,http://localhost:3000}") String allowedOrigins) { // Especifica el origen permitido (URL de tu frontend)
        CorsConfiguration configuration = new CorsConfiguration();
        // Además: separamos por coma y trim para varios frontends (Vite, CRA, etc.); con allowCredentials(true) no se puede usar "*" como origen.
        List<String> origins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
        configuration.setAllowedOrigins(origins);
        // Además: OPTIONS incluido para el preflight CORS del navegador.
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        // Además: Authorization debe poder enviarse en cross-origin (JWT Bearer).
        configuration.setAllowedHeaders(List.of("*"));
        // Además: cabeceras que el JS del cliente puede leer en la respuesta si las necesita.
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(true);
        // Además: cache del preflight en el navegador (segundos).
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    // Cargar los datos del usuario desde tu sistema a través de UsuarioRepository
    //lo utiliza AuthenticationService. para buscar el email
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> usuarioRepository.findByEmail(username)
                //TODO: ssanchez - capturar con globalexceptionhanlder @ControllerAdivce
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    // Recibe las credenciales del usuario (a través del UsernamePasswordAuthenticationToken)
    // Usa el UserDetailsService para buscar el usuario en la base de datos
    // Usa el PasswordEncoder para verificar si la contraseña proporcionada coincide con la almacenada
    // Si todo es correcto, crea un token de autenticación; si no, lanza una excepción    
    // @Bean
    // public AuthenticationProvider authenticationProvider() {
    //     DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
    //     authProvider.setPasswordEncoder(passwordEncoder());
    //     authProvider.setUserDetailsService(userDetailsService());
    //     return authProvider;
    // }

    /**
     * AuthenticationManager es el componente central de autenticación en Spring Security.
     * 
     * Funcionamiento:
     * 1. Recibe un objeto Authentication (UsernamePasswordAuthenticationToken en nuestro caso)
     * 2. Delega la autenticación a una cadena de AuthenticationProvider configurados
     * 3. Por defecto, usa DaoAuthenticationProvider que:
     *    - Utiliza UserDetailsService para cargar el usuario de la base de datos
     *    - Emplea PasswordEncoder para verificar la contraseña
     *    - Compara las credenciales proporcionadas con las almacenadas
     * 
     * Proceso de autenticación:
     * - Entrada: Credenciales sin verificar (username/password)
     * - Proceso: Validación de credenciales
     * - Salida: Authentication completamente autenticado con authorities
     * 
     * Si la autenticación falla, lanza AuthenticationException
     * <p><b>Además:</b> en el login, {@code BadCredentialsException} puede mapearse a 401 vía {@code GlobalExceptionHandler}.</p>
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // Define el codificador de contraseñas que se usará para encriptar y verificar passwords
    // este encoder lo utiliza AuthenticationService.authenticate para verificar la pass, la encripta y compara con la pass de db
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Configura las reglas de seguridad para las diferentes rutas de la API
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, CorsConfigurationSource corsConfigurationSource)
            throws Exception {
        // http
        //         .csrf(csrf -> csrf.disable())
        //         .authorizeHttpRequests(auth -> auth
        //                 // .requestMatchers("/api/productos/**").permitAll()
        //                 .requestMatchers("/api/auth/**").permitAll()
        //                 .anyRequest().authenticated());

        // return http.build();

        // Además: activamos CORS con el bean de arriba (inyectado para usar la misma instancia en toda la cadena).
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(csrf -> csrf.disable())
                // Sin esto, Spring Security responde 401/403 con HTML o cuerpo vacío; así la API devuelve JSON como el resto de errores
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(restSecurityErrorHandler)
                        .accessDeniedHandler(restSecurityErrorHandler))
                .authorizeHttpRequests(auth -> auth
                        // Rutas públicas que no requieren autenticación
                        //el controller /api/auth puede ser solicitado por cualquier usuario
                        .requestMatchers("/api/auth/**").permitAll()
                        //el endpoint /api/productos con metodo get es público, cualquiera puede ver los productos
                        .requestMatchers(HttpMethod.GET, "/api/productos/**").permitAll()

                        // Rutas que requieren autenticación para modificar productos
                        //solo los usuarios autenticados pueden crear un producto
                        .requestMatchers(HttpMethod.POST, "/api/productos").authenticated()
                        //solo los usuarios autenticados pueden actualizar un producto
                        .requestMatchers(HttpMethod.PUT, "/api/productos/**").authenticated()
                        //solo los usuarios autenticados pueden eliminar un producto
                        .requestMatchers(HttpMethod.DELETE, "/api/productos/**").authenticated()

                        // Carrito: con token
                        .requestMatchers("/api/cart/**").authenticated()

                        // Perfil propio
                        .requestMatchers("/api/usuarios/me", "/api/usuarios/me/**").authenticated()

                        // Rutas exclusivas para administradores
                        //verifica que el usuario esté autenticado y tenga el rol ADMIN
                        .requestMatchers("/api/admin/**").hasRole(Role.ADMIN.name())

                        // Rutas de pedidos solo para usuarios autenticados
                        .requestMatchers("/api/pedidos/**").authenticated()

                        // Cualquier otra ruta requiere autenticación
                        // con esta linea abarca requiere que todos los endpoints esten autenticados
                        // no seía necesario post, put, delete /api/productos , api/pedidos
                        .anyRequest().authenticated())

                        // insertar un filtro personalizado (su JwtFilter) en la cadena de filtros
                        // se ejecuta cada vez que se hace una solicitud a un endpoint
                        // Funcionamiento
                        // Llegada de la Solicitud: Un cliente envía una solicitud HTTP (por ejemplo, GET /api//products).
                        // Cadena de Filtros: Spring intercepta la solicitud y la pasa a través de una larga cadena de filtros de seguridad.
                        // Ejecución del JwtFilter: Como usted lo insertó al inicio de la cadena, su JwtFilter es uno de los primeros en ejecutarse.
                        // Su método doFilterInternal se ejecuta.
                        // Si el token es válido: El filtro establece la autenticación en el SecurityContext y llama a filterChain.doFilter(request, response) para pasar la solicitud al siguiente filtro y, finalmente, al controlador.
                        // Si el token falta o es inválido: El filtro rechaza la solicitud  o deja que la cadena continúe si el endpoint es público.
                        // Llegada al Controlador: Si el filtro permite el paso, la solicitud finalmente llega a su controlador.
                        // Además: si el endpoint exige authenticated y no hay JWT válido, el flujo termina en 401 JSON (RestSecurityErrorHandler).
                        .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
