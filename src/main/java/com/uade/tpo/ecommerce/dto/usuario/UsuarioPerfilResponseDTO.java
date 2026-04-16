package com.uade.tpo.ecommerce.dto.usuario;

import java.time.LocalDate;
import java.util.List;

import com.uade.tpo.ecommerce.model.Role;
import com.uade.tpo.ecommerce.model.Sexo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Respuesta de perfil: nunca incluye password ni datos internos de Spring Security.
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioPerfilResponseDTO {

    private Long id;
    private String nombre;
    private String apellido;
    private String email;
    private LocalDate fechaNacimiento;
    private Sexo sexo;
    private Role role;

    /** Productos donde el usuario es vendedor. */
    private List<PublicacionPerfilDTO> publicaciones;

    /** Compras del usuario; vacío hasta que exista el módulo de pedidos. */
    private List<CompraPerfilDTO> compras;
}
