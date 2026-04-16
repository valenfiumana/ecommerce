package com.uade.tpo.ecommerce.dto.pedido;

import com.uade.tpo.ecommerce.model.EstadoPedido;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Body del PATCH /api/pedidos/{id}/estado.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CambioEstadoRequestDTO {

    // El estado al que se quiere mover el pedido. Obligatorio.
    @NotNull(message = "El campo nuevoEstado es obligatorio")
    private EstadoPedido nuevoEstado;

    // Motivo opcional (ej: "pago rechazado por el banco", "cliente solicitó cancelación").
    private String motivo;
}
