package com.uade.tpo.ecommerce.dto.usuario;

import com.uade.tpo.ecommerce.model.Role;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Fila del listado {@code GET /api/usuarios} (solo ADMIN). Sin password. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioAdminListDTO {

    private Long id;
    private String nombre;
    private String apellido;
    private String email;
    private Role role;
    private boolean activo;
}
