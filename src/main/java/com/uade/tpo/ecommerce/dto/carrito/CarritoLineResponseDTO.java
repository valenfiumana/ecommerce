package com.uade.tpo.ecommerce.dto.carrito;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Lo que devolvemos por cada línea del carrito. precio y stock son los del producto ahora (no precio de compra cerrada).
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CarritoLineResponseDTO {

    // Id de la fila (sirve para DELETE /api/cart/{id}).
    private Long id;
    private Long productId; // FK al catálogo
    private String nombreProducto; // Copia del nombre para mostrar sin otro GET
    private Integer cantidad; // Unidades en el carrito
    private Double precioActual; // Precio del producto hoy (no congelado hasta checkout)
    private Integer stockDisponible; // Stock del producto hoy (el front compara con cantidad)
}
