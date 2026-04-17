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
    private String nombreUsuario;
    private String nombre;
    private String apellido;
    private String email;
    private LocalDate fechaNacimiento;
    private Sexo sexo;
    private Role role;

    /** Productos donde el usuario es vendedor. */
    private List<PublicacionPerfilDTO> publicaciones;

    /** Pedidos donde el usuario es comprador (más recientes primero). */
    private List<CompraPerfilDTO> compras;

    /**
     * Pedidos donde participó como vendedor (al menos una línea de sus publicaciones).
     * Misma forma que {@link #compras}; tope en servidor al armar el perfil.
     */
    private List<CompraPerfilDTO> ventas;
}
