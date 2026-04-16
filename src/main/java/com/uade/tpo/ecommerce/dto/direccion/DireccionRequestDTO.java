package com.uade.tpo.ecommerce.dto.direccion;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DireccionRequestDTO {

    @NotBlank(message = "calle es obligatoria")
    private String calle;

    @NotBlank(message = "numero es obligatorio")
    private String numero;

    @NotBlank(message = "codigoPostal es obligatorio")
    private String codigoPostal;

    @NotBlank(message = "ciudad es obligatoria")
    private String ciudad;

    @NotBlank(message = "provincia es obligatoria")
    private String provincia;

    @NotBlank(message = "pais es obligatorio")
    private String pais;

    private String referencia;

    private Boolean principal;
}
