package com.uade.tpo.ecommerce.dto.usuario;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Resumen de un producto publicado por el usuario (GET /api/usuarios/me).
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicacionPerfilDTO {

    private Long id;
    private String nombre;
    private Double precio;
    private Integer stock;
}
