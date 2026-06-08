package com.uade.tpo.ecommerce.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uade.tpo.ecommerce.dto.LoginRequestDTO;
import com.uade.tpo.ecommerce.dto.LoginResponseDTO;
import com.uade.tpo.ecommerce.dto.RegisterRequestDTO;
import com.uade.tpo.ecommerce.service.AuthenticationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Endpoints de alta de usuario y obtención de JWT.
 * <p>
 * Rutas bajo {@code /api/auth} están declaradas como {@code permitAll} en {@code SecurityConfig}: el cliente puede
 * registrarse y loguearse <i>sin</i> mandar token. La respuesta del login incluye el JWT en {@code token} y debe
 * enviarse luego en {@code Authorization: Bearer ...} para rutas protegidas.
 * </p>
 * <p>Errores típicos (vía {@code GlobalExceptionHandler}): validación {@code @Valid} → 400; email duplicado → 409
 * ({@link com.uade.tpo.ecommerce.exception.ConflictException}); credenciales incorrectas en login → 401
 * ({@link org.springframework.security.authentication.BadCredentialsException}).</p>
 */
@Tag(name = "Autenticación", description = "Registro y login (JWT)")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    /**
     * Registro: persiste usuario con contraseña hasheada (BCrypt) y rol por defecto.
     * {@code @Valid} dispara las anotaciones de Bean Validation del {@link RegisterRequestDTO}.
     */
    @Operation(summary = "Registro de usuario", description = "Crea cuenta con BCrypt y rol por defecto. Público. Errores: 400 validación, 409 email duplicado.")
    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequestDTO request) {
        return ResponseEntity.ok(authenticationService.register(request));
    }

    /**
     * Login: valida credenciales vía {@code AuthenticationManager}; si OK, devuelve JWT firmado.
     * {@code @Valid} asegura email/contraseña no vacíos y formato de email antes de llamar a Spring Security.
     */
    @Operation(summary = "Login", description = "Devuelve JSON con el JWT en `token`. Usar en header Authorization: Bearer … para el resto de la API. 401 si credenciales inválidas.")
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        String token = authenticationService.authenticate(request);
        return ResponseEntity.ok(LoginResponseDTO.builder().token(token).build());
    }
}
