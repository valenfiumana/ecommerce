package com.uade.tpo.ecommerce.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    @Autowired
    private PedidoService pedidoService;

    /**
     * Convierte el carrito en un pedido.
     * El body es opcional (sin body también funciona, en ese caso
     * no se guarda dirección ni notas).
     * Devuelve 201 Created con el pedido generado.
     */
    @PostMapping("/checkout")
    public ResponseEntity<PedidoResponseDTO> checkout(
            @RequestBody(required = false) CheckoutRequestDTO request) {
        PedidoResponseDTO pedido = pedidoService.confirmarDesdeCarrito(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(pedido);
    }

    /**
     * Historial de pedidos del usuario autenticado.
     * Ruta fija — debe declararse antes de /{id}.
     */
    @GetMapping("/mis-pedidos")
    public ResponseEntity<List<PedidoResponseDTO>> misPedidos() {
        return ResponseEntity.ok(pedidoService.misPedidos());
    }

    /**
     * Detalle de un pedido específico.
     * Solo accesible por el comprador del pedido o un admin.
     */
    @GetMapping("/{id}")
    public ResponseEntity<PedidoResponseDTO> obtenerPedido(@PathVariable Long id) {
        return ResponseEntity.ok(pedidoService.obtenerPedido(id));
    }

    /**
     * Cambia el estado del pedido.
     * - COMPRADOR: solo puede CANCELAR un pedido propio en PENDIENTE_PAGO.
     * - ADMIN:     puede hacer cualquier transición válida.
     * Si la transición no es válida → 400 con mensaje claro.
     */
    @PatchMapping("/{id}/estado")
    public ResponseEntity<PedidoResponseDTO> cambiarEstado(
            @PathVariable Long id,
            @Valid @RequestBody CambioEstadoRequestDTO request) {
        return ResponseEntity.ok(pedidoService.cambiarEstado(id, request));
    }
}
