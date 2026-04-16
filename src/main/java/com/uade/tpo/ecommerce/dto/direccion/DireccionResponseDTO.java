package com.uade.tpo.ecommerce.dto.direccion;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DireccionResponseDTO {

    private Long id;
    private String calle;
    private String numero;
    private String codigoPostal;
    private String ciudad;
    private String provincia;
    private String pais;
    private String referencia;
    private boolean principal;
}
