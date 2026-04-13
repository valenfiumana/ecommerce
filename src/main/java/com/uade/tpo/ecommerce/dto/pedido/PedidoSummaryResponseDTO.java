package com.uade.tpo.ecommerce.dto.pedido;

import java.time.LocalDateTime;

import com.uade.tpo.ecommerce.model.EstadoPedido;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Resumen liviano para listados paginados de compras o ventas.
 * Evita enviar el detalle completo cuando el cliente solo necesita armar el historial.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PedidoSummaryResponseDTO {

    private Long id;
    private LocalDateTime fecha;
    private Double total;
    private EstadoPedido estado;
    private Integer cantidadItems;
}
