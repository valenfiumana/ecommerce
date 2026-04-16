package com.uade.tpo.ecommerce.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.uade.tpo.ecommerce.dto.favorito.FavoritoResponseDTO;
import com.uade.tpo.ecommerce.model.Favorito;
import com.uade.tpo.ecommerce.model.Producto;

@Component
public class FavoritoMapper {

    public FavoritoResponseDTO toDTO(Favorito favorito) {
        Producto p = favorito.getProducto();
        return FavoritoResponseDTO.builder()
                .id(favorito.getId())
                .productId(p.getId())
                .nombreProducto(p.getNombre())
                .precio(p.getPrecio())
                .build();
    }

    public List<FavoritoResponseDTO> toDTOList(List<Favorito> favoritos) {
        return favoritos.stream().map(this::toDTO).collect(Collectors.toList());
    }
}