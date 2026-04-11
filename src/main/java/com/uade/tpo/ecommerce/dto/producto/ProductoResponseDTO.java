package com.uade.tpo.ecommerce.dto.producto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de salida (response) para exponer un producto en la API sin devolver la entidad JPA.
 * Incluye los campos que el cliente necesita ver tras crear, leer o actualizar.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductoResponseDTO {

    private Long id;
    private String nombre;
    private String descripcion;
    private Double precio;
    private Integer stock;
}
