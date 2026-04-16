package com.uade.tpo.ecommerce.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uade.tpo.ecommerce.dto.pedido.CambioEstadoRequestDTO;
import com.uade.tpo.ecommerce.dto.pedido.CheckoutRequestDTO;
import com.uade.tpo.ecommerce.dto.pedido.PedidoResponseDTO;
import com.uade.tpo.ecommerce.dto.pedido.PedidoSummaryResponseDTO;
import com.uade.tpo.ecommerce.service.PedidoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Pedidos", description = "Checkout, historial, detalle y cambio de estado")
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
    @Operation(summary = "Checkout desde carrito", description = "Crea pedido con snapshot de precios, descuenta stock, vacía carrito. Body opcional (direccionId, direccionEnvio, notas). 201.")
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
    @Operation(summary = "Mis pedidos (lista completa)", description = "Todos los pedidos del comprador autenticado, detalle completo, más reciente primero. Sin paginación.")
    @GetMapping("/mis-pedidos")
    public ResponseEntity<List<PedidoResponseDTO>> misPedidos() {
        return ResponseEntity.ok(pedidoService.misPedidos());
    }

    /**
     * Historial paginado de compras del usuario autenticado.
     * Devuelve 200 incluso si no hay pedidos: la página llega vacía.
     */
    @Operation(summary = "Mis compras (paginado)", description = "Resumen de pedidos donde sos comprador. Parámetros estándar page, size, sort.")
    @GetMapping("/mis-compras")
    public ResponseEntity<Page<PedidoSummaryResponseDTO>> misCompras(
            @PageableDefault(size = 10, sort = "fecha", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(pedidoService.listarMisCompras(pageable));
    }

    /**
     * Historial paginado de ventas del usuario autenticado.
     * El usuario solo ve pedidos que contienen productos de sus publicaciones.
     */
    @Operation(summary = "Mis ventas (paginado)", description = "Pedidos que incluyen al menos una línea de tus publicaciones.")
    @GetMapping("/mis-ventas")
    public ResponseEntity<Page<PedidoSummaryResponseDTO>> misVentas(
            @PageableDefault(size = 10, sort = "fecha", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(pedidoService.listarMisVentas(pageable));
    }

    /**
     * Detalle de un pedido específico.
     * Comprador del pedido, vendedor con al menos una línea propia, o admin.
     */
    @Operation(summary = "Detalle de pedido", description = "Comprador, vendedor involucrado en alguna línea, o ADMIN. 403 si no corresponde.")
    @GetMapping("/{id}")
    public ResponseEntity<PedidoResponseDTO> obtenerPedido(
            @Parameter(description = "ID del pedido") @PathVariable Long id) {
        return ResponseEntity.ok(pedidoService.obtenerPedido(id));
    }

    /**
     * Cambia el estado del pedido.
     * - ADMIN: puede cualquier transición válida.
     * - COMPRADOR: cancelar cuando el flujo lo permite; o ENTREGADO cuando el pedido está ENVIADO.
     * - VENDEDOR (con ítems en el pedido): ENVIADO desde PAGADO; ENTREGADO desde ENVIADO.
     * Si la transición no es válida → 400 con mensaje claro.
     */
    @Operation(summary = "Cambiar estado del pedido", description = "Transiciones válidas según rol: admin todas; comprador cancelar o ENTREGADO si ENVIADO; vendedor PAGADO→ENVIADO o ENVIADO→ENTREGADO. 400 si transición inválida.")
    @PatchMapping("/{id}/estado")
    public ResponseEntity<PedidoResponseDTO> cambiarEstado(
            @Parameter(description = "ID del pedido") @PathVariable Long id,
            @Valid @RequestBody CambioEstadoRequestDTO request) {
        return ResponseEntity.ok(pedidoService.cambiarEstado(id, request));
    }
}
