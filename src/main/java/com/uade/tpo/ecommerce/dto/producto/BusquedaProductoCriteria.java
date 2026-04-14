package com.uade.tpo.ecommerce.dto.producto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Agrupa los filtros opcionales del buscador de productos.
 * Tenerlos en un solo objeto evita firmas largas en el servicio.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusquedaProductoCriteria {

    private String q;
    private Long categoriaId;
    private Double precioMin;
    private Double precioMax;
    private String orden;
}
