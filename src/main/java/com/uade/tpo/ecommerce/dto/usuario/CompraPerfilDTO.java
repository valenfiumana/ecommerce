package com.uade.tpo.ecommerce.dto.usuario;

import java.time.LocalDateTime;

import com.uade.tpo.ecommerce.model.EstadoPedido;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Resumen de una compra en el perfil ({@code GET /api/usuarios/me}). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompraPerfilDTO {

    private Long id;
    private LocalDateTime fecha;
    private Double total;
    private EstadoPedido estado;
}
