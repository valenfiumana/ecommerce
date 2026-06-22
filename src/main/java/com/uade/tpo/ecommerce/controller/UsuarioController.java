package com.uade.tpo.ecommerce.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uade.tpo.ecommerce.dto.usuario.UsuarioAdminListDTO;
import com.uade.tpo.ecommerce.dto.usuario.UsuarioPerfilResponseDTO;
import com.uade.tpo.ecommerce.dto.usuario.UsuarioPerfilUpdateRequestDTO;
import com.uade.tpo.ecommerce.service.UsuarioService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * Rutas bajo {@code /api/usuarios}.
 * <p>Las rutas {@code /me} tienen que ir <b>antes</b> que {@code /{id}}: si no, Spring intenta parsear "me" como Long y falla.</p>
 * <p>{@code GET /} listado solo ADMIN. {@code GET /{id}} perfil completo si es el propio id o ADMIN; si no, datos públicos mínimos.</p>
 */
@Tag(name = "Usuarios", description = "Perfil /me, listado admin, consulta por id")
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @Operation(summary = "Mi perfil", description = "Datos del usuario del JWT: publicaciones, compras y ventas (resúmenes). Sin password.")
    @GetMapping("/me")
    public ResponseEntity<UsuarioPerfilResponseDTO> getMiPerfil() {
        return ResponseEntity.ok(usuarioService.obtenerPerfilActual());
    }

    @Operation(summary = "Actualizar mi perfil", description = "PATCH parcial; email único si se modifica.")
    @PatchMapping("/me")
    public ResponseEntity<UsuarioPerfilResponseDTO> patchMiPerfil(@Valid @RequestBody UsuarioPerfilUpdateRequestDTO body) {
        return ResponseEntity.ok(usuarioService.actualizarPerfil(body));
    }

    @Operation(summary = "Listar usuarios (admin)", description = "Solo ROLE_ADMIN. Incluye email y rol.")
    @GetMapping
    public ResponseEntity<List<UsuarioAdminListDTO>> listarUsuarios() {
        return ResponseEntity.ok(usuarioService.listarUsuariosParaAdmin());
    }

    /**
     * Perfil completo (misma forma que {@code /me}) si consultás tu id o sos ADMIN; en otro caso solo id/nombre/apellido.
     */
    @Operation(summary = "Usuario por id", description = "Si sos el mismo usuario o admin: perfil completo. Si no: solo id, nombre y apellido (público mínimo).")
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerUsuarioPorId(@Parameter(description = "ID del usuario") @PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.obtenerUsuarioPorId(id));
    }

    @Operation(summary = "Eliminar usuario (admin)", description = "Solo ROLE_ADMIN. Realiza baja logica: el usuario queda inactivo y no puede autenticarse.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarUsuario(@Parameter(description = "ID del usuario") @PathVariable Long id) {
        usuarioService.eliminarUsuarioComoAdmin(id);
        return ResponseEntity.noContent().build();
    }
}
