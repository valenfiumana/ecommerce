package com.uade.tpo.ecommerce.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @GetMapping
    public ResponseEntity<String> getAllUsuarios() {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body("Listado de usuarios no implementado");
    }

    @GetMapping("/{id}")
    public ResponseEntity<String> getUsuarioById(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body("Detalle de usuario no implementado");
    }
}
