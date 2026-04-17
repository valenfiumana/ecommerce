package com.uade.tpo.ecommerce.dto.producto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de entrada (request) para crear un producto: no incluye id (lo genera la base de datos).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductoCreateRequestDTO {

    // @NotBlank: el string no puede ser null, vacío ni solo espacios
    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    private String descripcion;

    // @NotNull: el precio debe estar presente en el JSON
    // @Positive: exige valor estrictamente mayor que cero (no admite 0 ni negativos)
    @NotNull(message = "El precio es obligatorio")
    @Positive(message = "El precio debe ser mayor que cero")
    private Double precio;

    @NotNull(message = "El stock es obligatorio")
    @PositiveOrZero(message = "El stock no puede ser negativo")
    private Integer stock;

    @NotEmpty(message = "Debes enviar al menos una categoría")
    private List<@NotNull(message = "Cada categoría debe tener un id válido") Long> categoriaIds;

    @NotEmpty(message = "Debes enviar al menos una imagen")
    private List<@NotBlank(message = "Cada imagen debe contener una URL") String> imagenes;
}
