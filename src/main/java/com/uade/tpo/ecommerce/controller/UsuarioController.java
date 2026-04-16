package com.uade.tpo.ecommerce.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uade.tpo.ecommerce.dto.usuario.UsuarioPerfilResponseDTO;
import com.uade.tpo.ecommerce.dto.usuario.UsuarioPerfilUpdateRequestDTO;
import com.uade.tpo.ecommerce.service.UsuarioService;

import jakarta.validation.Valid;

/**
 * Rutas bajo {@code /api/usuarios}.
 * <p>Las rutas {@code /me} tienen que ir <b>antes</b> que {@code /{id}}: si no, Spring intenta parsear "me" como Long y falla.</p>
 * <p>Roles: ver README.md en la raíz del repo (USER y ADMIN).</p>
 */
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    // GET /api/usuarios/me — perfil del usuario del token (sin password).
    @GetMapping("/me")
    public ResponseEntity<UsuarioPerfilResponseDTO> getMiPerfil() {
        return ResponseEntity.ok(usuarioService.obtenerPerfilActual());
    }

    // PATCH /api/usuarios/me — actualización parcial; email único si lo cambiás.
    @PatchMapping("/me")
    public ResponseEntity<UsuarioPerfilResponseDTO> patchMiPerfil(@Valid @RequestBody UsuarioPerfilUpdateRequestDTO body) {
        return ResponseEntity.ok(usuarioService.actualizarPerfil(body));
    }

    @GetMapping
    public ResponseEntity<String> getAllUsuarios() {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body("Listado de usuarios no implementado");
    }

    @GetMapping("/{id}")
    public ResponseEntity<String> getUsuarioById(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body("Detalle de usuario no implementado");
    }
}
