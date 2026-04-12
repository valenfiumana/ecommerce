package com.uade.tpo.ecommerce.dto.pedido;

import java.time.LocalDateTime;
import java.util.List;

import com.uade.tpo.ecommerce.model.EstadoPedido;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Respuesta completa de un pedido: cabecera + lista de líneas.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PedidoResponseDTO {

    private Long id;
    private Long compradorId;
    private LocalDateTime fecha;
    private Double total;
    private EstadoPedido estado;
    private String direccionEnvio;
    private String notas;
    private List<PedidoItemResponseDTO> items;
}
