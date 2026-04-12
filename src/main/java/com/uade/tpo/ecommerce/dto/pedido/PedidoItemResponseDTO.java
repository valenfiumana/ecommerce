package com.uade.tpo.ecommerce.dto.pedido;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Representa una línea del pedido en la respuesta de la API.
 * Incluye el precioUnitario snapshot (el precio que pagó el comprador),
 * no el precio actual del producto.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PedidoItemResponseDTO {

    private Long id;
    private Long productoId;
    private String nombreProducto;
    private Integer cantidad;

    // Precio que se cobró al momento del checkout (snapshot — no cambia aunque el producto cambie de precio).
    private Double precioUnitario;

    private Double subtotal;
}
