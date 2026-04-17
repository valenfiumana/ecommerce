package com.uade.tpo.ecommerce.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uade.tpo.ecommerce.dto.carrito.CarritoItemAddRequestDTO;
import com.uade.tpo.ecommerce.dto.carrito.CarritoItemUpdateRequestDTO;
import com.uade.tpo.ecommerce.dto.carrito.CarritoLineResponseDTO;
import com.uade.tpo.ecommerce.service.CarritoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

// API /api/cart: hace falta JWT. GET lista, POST suma, PUT cambia cantidad, DELETE borra una línea por id.
@Tag(name = "Carrito", description = "Ítems del usuario autenticado")
@RestController
@RequestMapping("/api/cart")
public class CarritoController {

    @Autowired
    private CarritoService carritoService;

    @Operation(summary = "Ver carrito", description = "Líneas del usuario autenticado (precio/stock actuales del producto). Requiere JWT.")
    @GetMapping
    public ResponseEntity<List<CarritoLineResponseDTO>> getCarrito() {
        return ResponseEntity.ok(carritoService.listarCarrito());
    }

    @Operation(summary = "Agregar al carrito", description = "Suma cantidad al ítem; si ya existía el producto, acumula. No puede superar el stock publicado.")
    @PostMapping
    public ResponseEntity<List<CarritoLineResponseDTO>> agregar(@Valid @RequestBody CarritoItemAddRequestDTO request) {
        return ResponseEntity.ok(carritoService.agregar(request));
    }

    @Operation(summary = "Actualizar cantidad", description = "Fija cantidad por producto; cantidad 0 elimina la línea.")
    @PutMapping
    public ResponseEntity<List<CarritoLineResponseDTO>> actualizar(@Valid @RequestBody CarritoItemUpdateRequestDTO request) {
        return ResponseEntity.ok(carritoService.actualizarCantidad(request));
    }

    @Operation(summary = "Quitar línea del carrito", description = "Elimina por id de fila carrito_items (no confundir con productoId).")
    @DeleteMapping("/{id}")
    public ResponseEntity<List<CarritoLineResponseDTO>> eliminar(
            @Parameter(description = "ID de la fila carrito_items") @PathVariable Long id) {
        return ResponseEntity.ok(carritoService.eliminarLinea(id));
    }

    @Operation(summary = "Vaciar carrito", description = "Elimina todas las líneas del carrito del usuario autenticado.")
    @DeleteMapping
    public ResponseEntity<List<CarritoLineResponseDTO>> vaciar() {
        return ResponseEntity.ok(carritoService.vaciarCarrito());
    }
}
