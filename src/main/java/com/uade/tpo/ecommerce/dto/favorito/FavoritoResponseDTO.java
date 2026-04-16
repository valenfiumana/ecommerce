package com.uade.tpo.ecommerce.dto.favorito;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FavoritoResponseDTO {

    private Long id;
    private Long productId;
    private String nombreProducto;
    private Double precio;
}