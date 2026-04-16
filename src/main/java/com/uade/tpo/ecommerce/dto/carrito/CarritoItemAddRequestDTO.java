package com.uade.tpo.ecommerce.dto.carrito;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Body del POST /api/cart: qué producto y cuántas unidades sumar.
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CarritoItemAddRequestDTO {

    @NotNull(message = "El id del producto es obligatorio")
    private Long productId; // Qué producto sumar

    @NotNull(message = "La cantidad es obligatoria")
    @Positive(message = "La cantidad debe ser al menos 1")
    private Integer quantity; // Cuántas unidades sumar (mínimo 1)
}
