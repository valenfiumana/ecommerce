package com.uade.tpo.ecommerce.dto.usuario;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Resumen de una compra del usuario; lista viene vacía hasta existir pedidos/checkout.
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompraPerfilDTO {

    private Long id;
    private LocalDateTime fecha;
    private Double total;
}
