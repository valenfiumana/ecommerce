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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Direcciones", description = "CRUD de direcciones del usuario; uso en checkout")
@RestController
@RequestMapping("/api/direcciones")
@RequiredArgsConstructor
public class DireccionController {

    private final DireccionService direccionService;

    @Operation(summary = "Listar mis direcciones", description = "Solo direcciones del usuario del JWT.")
    @GetMapping
    public ResponseEntity<List<DireccionResponseDTO>> listar() {
        return ResponseEntity.ok(direccionService.listarPropias());
    }

    @Operation(summary = "Obtener dirección por id", description = "403 si la dirección no es del usuario autenticado.")
    @GetMapping("/{id}")
    public ResponseEntity<DireccionResponseDTO> obtener(
            @Parameter(description = "ID de la dirección") @PathVariable Long id) {
        return ResponseEntity.ok(direccionService.obtener(id));
    }

    @Operation(summary = "Crear dirección", description = "Asocia la dirección al usuario del token. 201 con el recurso creado.")
    @PostMapping
    public ResponseEntity<DireccionResponseDTO> crear(@Valid @RequestBody DireccionRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(direccionService.crear(request));
    }

    @Operation(summary = "Actualizar dirección", description = "Solo el dueño puede editar.")
    @PutMapping("/{id}")
    public ResponseEntity<DireccionResponseDTO> actualizar(
            @Parameter(description = "ID de la dirección") @PathVariable Long id,
            @Valid @RequestBody DireccionRequestDTO request) {
        return ResponseEntity.ok(direccionService.actualizar(id, request));
    }

    @Operation(summary = "Eliminar dirección", description = "204 sin cuerpo. Solo el dueño.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@Parameter(description = "ID de la dirección") @PathVariable Long id) {
        direccionService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
