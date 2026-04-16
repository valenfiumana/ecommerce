package com.uade.tpo.ecommerce.dto.Resena;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO que resume la reputación de un vendedor.
 * 
 * Se devuelve en GET /api/vendedores/{id}/resumen:
 * {
 *   "promedioPuntuacion": 4.5,
 *   "cantidadResenas": 15,
 *   "cantidadVentas": 50
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VendedorResumenDTO {

    /**
     * Promedio de todas las reseñas del vendedor.
     * Ej: 4.5 significa que en promedio recibe 4.5 estrellas.
     * Puede ser null si el vendedor no tiene reseñas aún.
     */
    private Double promedioPuntuacion;

    /**
     * Cantidad total de reseñas que el vendedor ha recibido.
     * Ej: 15 reseñas
     */
    private Long cantidadResenas;

    /**
     * Cantidad total de productos vendidos por este vendedor.
     * Se cuenta por la cantidad de pedido_items vendidos.
     */
    private Long cantidadVentas;
}
