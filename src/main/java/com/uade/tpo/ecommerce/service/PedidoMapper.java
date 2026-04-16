package com.uade.tpo.ecommerce.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import com.uade.tpo.ecommerce.dto.pedido.DireccionSnapshotResponseDTO;
import com.uade.tpo.ecommerce.dto.pedido.PedidoItemResponseDTO;
import com.uade.tpo.ecommerce.dto.pedido.PedidoResponseDTO;
import com.uade.tpo.ecommerce.dto.pedido.PedidoSummaryResponseDTO;
import com.uade.tpo.ecommerce.model.DireccionSnapshot;
import com.uade.tpo.ecommerce.model.Pedido;
import com.uade.tpo.ecommerce.model.PedidoItem;

/**
 * Convierte entidades Pedido / PedidoItem a sus DTOs de respuesta.
 * No tiene lógica de negocio: solo mapea campos.
 * Al separarlo del servicio, el código de negocio queda limpio.
 */
@Component
public class PedidoMapper {

    public PedidoResponseDTO toDTO(Pedido pedido) {
        return PedidoResponseDTO.builder()
                .id(pedido.getId())
                .compradorId(pedido.getComprador().getId())
                .fecha(pedido.getFecha())
                .total(pedido.getTotal())
                .estado(pedido.getEstado())
                .direccionEnvio(pedido.getDireccionEnvio())
                .direccionSnapshot(toSnapshotDTO(pedido.getDireccionSnapshot()))
                .notas(pedido.getNotas())
                .items(pedido.getItems().stream()
                        .map(this::toItemDTO)
                        .collect(Collectors.toList()))
                .build();
    }

    public List<PedidoResponseDTO> toDTOList(List<Pedido> pedidos) {
        return pedidos.stream().map(this::toDTO).collect(Collectors.toList());
    }

    public PedidoSummaryResponseDTO toSummaryDTO(Pedido pedido) {
        // En el historial mostramos la cantidad total de unidades compradas/vendidas,
        // no solo la cantidad de líneas del pedido.
        int cantidadItems = pedido.getItems().stream()
                .mapToInt(PedidoItem::getCantidad)
                .sum();

        return PedidoSummaryResponseDTO.builder()
                .id(pedido.getId())
                .fecha(pedido.getFecha())
                .total(pedido.getTotal())
                .estado(pedido.getEstado())
                .cantidadItems(cantidadItems)
                .build();
    }

    public Page<PedidoSummaryResponseDTO> toSummaryPage(Page<Pedido> pedidos) {
        return pedidos.map(this::toSummaryDTO);
    }

    private DireccionSnapshotResponseDTO toSnapshotDTO(DireccionSnapshot snap) {
        if (snap == null) {
            return null;
        }
        return DireccionSnapshotResponseDTO.builder()
                .calle(snap.getCalle())
                .numero(snap.getNumero())
                .codigoPostal(snap.getCodigoPostal())
                .ciudad(snap.getCiudad())
                .provincia(snap.getProvincia())
                .pais(snap.getPais())
                .referencia(snap.getReferencia())
                .build();
    }

    private PedidoItemResponseDTO toItemDTO(PedidoItem item) {
        // Si el producto fue eliminado de la BD, mostramos un String
        // pero el precio snapshot sigue disponible.
        String nombre = item.getProducto() != null
                ? item.getProducto().getNombre()
                : "(producto eliminado)";
        Long productoId = item.getProducto() != null
                ? item.getProducto().getId()
                : null;

        return PedidoItemResponseDTO.builder()
                .id(item.getId())
                .productoId(productoId)
                .nombreProducto(nombre)
                .cantidad(item.getCantidad())
                .precioUnitario(item.getPrecioUnitario())
                .subtotal(item.getSubtotal())
                .build();
    }
}
