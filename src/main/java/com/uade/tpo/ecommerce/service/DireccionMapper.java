package com.uade.tpo.ecommerce.service;

import org.springframework.stereotype.Component;

import com.uade.tpo.ecommerce.dto.direccion.DireccionRequestDTO;
import com.uade.tpo.ecommerce.dto.direccion.DireccionResponseDTO;
import com.uade.tpo.ecommerce.model.Direccion;
import com.uade.tpo.ecommerce.model.DireccionSnapshot;

@Component
public class DireccionMapper {

    public DireccionResponseDTO toResponse(Direccion d) {
        return DireccionResponseDTO.builder()
                .id(d.getId())
                .calle(d.getCalle())
                .numero(d.getNumero())
                .codigoPostal(d.getCodigoPostal())
                .ciudad(d.getCiudad())
                .provincia(d.getProvincia())
                .pais(d.getPais())
                .referencia(d.getReferencia())
                .principal(d.isPrincipal())
                .build();
    }

    public DireccionSnapshot toSnapshot(Direccion d) {
        return DireccionSnapshot.builder()
                .calle(d.getCalle())
                .numero(d.getNumero())
                .codigoPostal(d.getCodigoPostal())
                .ciudad(d.getCiudad())
                .provincia(d.getProvincia())
                .pais(d.getPais())
                .referencia(d.getReferencia())
                .build();
    }

    public void aplicar(DireccionRequestDTO dto, Direccion destino) {
        destino.setCalle(dto.getCalle().trim());
        destino.setNumero(dto.getNumero().trim());
        destino.setCodigoPostal(dto.getCodigoPostal().trim());
        destino.setCiudad(dto.getCiudad().trim());
        destino.setProvincia(dto.getProvincia().trim());
        destino.setPais(dto.getPais().trim());
        destino.setReferencia(dto.getReferencia() != null && !dto.getReferencia().isBlank()
                ? dto.getReferencia().trim()
                : null);
        if (dto.getPrincipal() != null) {
            destino.setPrincipal(dto.getPrincipal());
        }
    }
}
