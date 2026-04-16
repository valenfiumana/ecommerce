package com.uade.tpo.ecommerce.dto.pedido;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DireccionSnapshotResponseDTO {

    private String calle;
    private String numero;
    private String codigoPostal;
    private String ciudad;
    private String provincia;
    private String pais;
    private String referencia;
}
