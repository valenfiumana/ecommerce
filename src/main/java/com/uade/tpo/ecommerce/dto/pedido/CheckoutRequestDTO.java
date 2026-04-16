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
 *
 * Si {@code direccionId} está presente, se copia un snapshot de esa dirección al pedido
 * (tiene prioridad sobre {@code direccionEnvio} en texto libre).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutRequestDTO {

    /** Dirección guardada del usuario autenticado; debe ser propia. */
    private Long direccionId;

    // Dirección de envío en texto libre (si no se usa direccionId).
    private String direccionEnvio;

    // Notas opcionales del comprador (ej: "dejar en portería", "llamar antes").
    private String notas;
}
