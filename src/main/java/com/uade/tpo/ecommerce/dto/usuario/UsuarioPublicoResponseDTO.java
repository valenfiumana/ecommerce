package com.uade.tpo.ecommerce.dto.usuario;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Vista mínima de otro usuario (no sos vos ni admin): solo identificación pública.
 * No incluye email ni datos de pedidos/publicaciones.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioPublicoResponseDTO {

    private Long id;
    private String nombre;
    private String apellido;
}
