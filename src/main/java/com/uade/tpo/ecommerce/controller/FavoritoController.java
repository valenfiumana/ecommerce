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

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/favorites")
public class FavoritoController {

    @Autowired
    private FavoritoService favoritoService;

    @GetMapping // GET de favoritos del usuario
    public ResponseEntity<List<FavoritoResponseDTO>> listarMisFavoritos() {
        return ResponseEntity.ok(favoritoService.listarMisFavoritos());
    }

    // GET  del favorito por id
    @GetMapping("/{id}")
    public ResponseEntity<FavoritoResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(favoritoService.obtenerPorId(id));
    }

    // GET favoritos de un usuario por id
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<FavoritoResponseDTO>> listarPorUsuario(@PathVariable Long userId) {
        return ResponseEntity.ok(favoritoService.listarPorUsuario(userId));
    }

    // POST agregar producto a favoritos
    @PostMapping
    public ResponseEntity<FavoritoResponseDTO> agregar(@Valid @RequestBody FavoritoRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(favoritoService.agregar(request));
    }

    // DELETE quitar de favoritos
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        favoritoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}