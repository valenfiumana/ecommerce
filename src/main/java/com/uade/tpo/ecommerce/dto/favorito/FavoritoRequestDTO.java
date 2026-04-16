package com.uade.tpo.ecommerce.dto.favorito;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FavoritoRequestDTO {

    @NotNull(message = "El id del producto es obligatorio")
    private Long productId;
}