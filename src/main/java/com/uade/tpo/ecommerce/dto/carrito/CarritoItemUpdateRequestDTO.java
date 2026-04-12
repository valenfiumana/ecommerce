package com.uade.tpo.ecommerce.dto.carrito;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Body del PUT /api/cart: producto y cantidad final. Con cantidad 0 borrás la línea.
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CarritoItemUpdateRequestDTO {

    @NotNull(message = "El id del producto es obligatorio")
    private Long productId; // A qué línea (producto) le cambiás la cantidad

    @NotNull(message = "La cantidad es obligatoria")
    @PositiveOrZero(message = "La cantidad no puede ser negativa")
    private Integer quantity; // Valor final; 0 = borrar esa línea
}
