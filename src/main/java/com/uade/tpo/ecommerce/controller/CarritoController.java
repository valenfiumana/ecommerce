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

import jakarta.validation.Valid;

// API /api/cart: hace falta JWT. GET lista, POST suma, PUT cambia cantidad, DELETE borra una línea por id.
@RestController
@RequestMapping("/api/cart")
public class CarritoController {

    @Autowired
    private CarritoService carritoService;

    // GET /api/cart — lista del usuario del token.
    @GetMapping
    public ResponseEntity<List<CarritoLineResponseDTO>> getCarrito() {
        return ResponseEntity.ok(carritoService.listarCarrito());
    }

    // POST /api/cart — suma producto+cantidad (merge si ya estaba).
    @PostMapping
    public ResponseEntity<List<CarritoLineResponseDTO>> agregar(@Valid @RequestBody CarritoItemAddRequestDTO request) {
        return ResponseEntity.ok(carritoService.agregar(request));
    }

    // PUT /api/cart — cantidad final por producto (0 borra la línea).
    @PutMapping
    public ResponseEntity<List<CarritoLineResponseDTO>> actualizar(@Valid @RequestBody CarritoItemUpdateRequestDTO request) {
        return ResponseEntity.ok(carritoService.actualizarCantidad(request));
    }

    // DELETE /api/cart/{id} — id = id de la fila carrito_items, no del producto.
    @DeleteMapping("/{id}")
    public ResponseEntity<List<CarritoLineResponseDTO>> eliminar(@PathVariable Long id) {
        return ResponseEntity.ok(carritoService.eliminarLinea(id));
    }
}
