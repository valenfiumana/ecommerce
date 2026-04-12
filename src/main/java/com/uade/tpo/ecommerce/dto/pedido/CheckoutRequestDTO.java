package com.uade.tpo.ecommerce.dto.pedido;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Body del POST /api/pedidos/checkout.
 *
 * Todos los campos son opcionales: el checkout funciona aunque se mande
 * el body vacío o directamente sin body.
 * El comprador SIEMPRE sale del JWT — nunca se acepta un compradorId del cliente.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutRequestDTO {

    // Dirección de envío en texto libre (puede evolucionar a un ID de dirección en el futuro).
    private String direccionEnvio;

    // Notas opcionales del comprador (ej: "dejar en portería", "llamar antes").
    private String notas;
}
