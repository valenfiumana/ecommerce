package com.uade.tpo.ecommerce.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uade.tpo.ecommerce.dto.direccion.DireccionRequestDTO;
import com.uade.tpo.ecommerce.dto.direccion.DireccionResponseDTO;
import com.uade.tpo.ecommerce.service.DireccionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/direcciones")
@RequiredArgsConstructor
public class DireccionController {

    private final DireccionService direccionService;

    @GetMapping
    public ResponseEntity<List<DireccionResponseDTO>> listar() {
        return ResponseEntity.ok(direccionService.listarPropias());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DireccionResponseDTO> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(direccionService.obtener(id));
    }

    @PostMapping
    public ResponseEntity<DireccionResponseDTO> crear(@Valid @RequestBody DireccionRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(direccionService.crear(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DireccionResponseDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody DireccionRequestDTO request) {
        return ResponseEntity.ok(direccionService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        direccionService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
