package com.uade.tpo.ecommerce.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uade.tpo.ecommerce.dto.favorito.FavoritoRequestDTO;
import com.uade.tpo.ecommerce.dto.favorito.FavoritoResponseDTO;
import com.uade.tpo.ecommerce.service.FavoritoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Favoritos", description = "Publicaciones guardadas del usuario")
@RestController
@RequestMapping("/api/favorites")
public class FavoritoController {

    @Autowired
    private FavoritoService favoritoService;

    @Operation(summary = "Mis favoritos", description = "Listado del usuario autenticado.")
    @GetMapping
    public ResponseEntity<List<FavoritoResponseDTO>> listarMisFavoritos() {
        return ResponseEntity.ok(favoritoService.listarMisFavoritos());
    }

    @Operation(summary = "Obtener favorito por id", description = "403 si el favorito no pertenece al usuario del token.")
    @GetMapping("/{id}")
    public ResponseEntity<FavoritoResponseDTO> obtenerPorId(
            @Parameter(description = "ID del registro favorito") @PathVariable Long id) {
        return ResponseEntity.ok(favoritoService.obtenerPorId(id));
    }

    @Operation(summary = "Favoritos por userId", description = "Solo el mismo userId que el token o ROLE_ADMIN; 403 en otro caso.")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<FavoritoResponseDTO>> listarPorUsuario(
            @Parameter(description = "ID del usuario") @PathVariable Long userId) {
        return ResponseEntity.ok(favoritoService.listarPorUsuario(userId));
    }

    @Operation(summary = "Agregar favorito", description = "201. Idempotente según reglas de negocio (duplicado puede dar 409).")
    @PostMapping
    public ResponseEntity<FavoritoResponseDTO> agregar(@Valid @RequestBody FavoritoRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(favoritoService.agregar(request));
    }

    @Operation(summary = "Quitar favorito", description = "204. Solo si el favorito es del usuario autenticado.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@Parameter(description = "ID del registro favorito") @PathVariable Long id) {
        favoritoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}