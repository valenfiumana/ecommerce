package com.uade.tpo.ecommerce.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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

import com.uade.tpo.ecommerce.dto.producto.ProductoCreateRequestDTO;
import com.uade.tpo.ecommerce.dto.producto.ProductoResponseDTO;
import com.uade.tpo.ecommerce.dto.producto.ProductoUpdateRequestDTO;
import com.uade.tpo.ecommerce.service.ProductoService;

import jakarta.validation.Valid;

//TODO: cambiar todos los métodos para que devuelvan ResponseEntity, es mala práctica devolver la entidad directamente, debe devolver un DTO
@RestController
// para acceder a este controlador, la URL base será /api/productos
// http://localhost:8080/api/productos -> devuelve la lista de productos
@RequestMapping("/api/productos")
public class ProductoController {

    
    @Autowired
    private ProductoService productoService;

    //http://localhost:8080/api/productos -> devuelve la lista de productos
    @GetMapping
    public ResponseEntity<List<ProductoResponseDTO>> getAllProductos() {
        return ResponseEntity.ok(productoService.getAllProductos());
    }

    // //http://localhost:8080/api/productos/1 -> devuelve el producto con id 1
    // @GetMapping("/{id}")
    // public Producto getProductoById(@PathVariable Long id) {
    //     return productoService.getProductoById(id);
    // }


    
    @GetMapping("/{id}")
    public ResponseEntity<ProductoResponseDTO> getProductoById(@PathVariable Long id) {
        ProductoResponseDTO productoResponse = productoService.getProductoById(id);

        // Se crea una nueva instancia de ResponseEntity, pasando el producto encontrado como cuerpo de la respuesta
        // y HttpStatus.OK (código 200) como estado de la respuesta.

        //ResponseEntity es una clase que representa toda la respuesta HTTP: código de estado, encabezados y cuerpo.
        //devuelve una promesa en el cuerpo los datos del producto, y un codigo de estado 200 (OK)
        // el cuerpo es un json productos -> json
        //TODO: ssanchez - devolver en todos los enpoints ResponseEntity con DTO correspondiente
        return new ResponseEntity<>(productoResponse, HttpStatus.OK);
    }


    // del http://localhost:8080/api/productos/1 -> elimina el producto con id 1
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProductoById(@PathVariable Long id) {
        productoService.deleteProductoById(id);
        return ResponseEntity.noContent().build();
    }

    // @Valid: dispara la validación Jakarta Bean Validation definida en el DTO (anotaciones como @NotNull, @NotBlank)
    @PostMapping
    public ResponseEntity<ProductoResponseDTO> saveProducto(@Valid @RequestBody ProductoCreateRequestDTO request) {
        ProductoResponseDTO savedProducto = productoService.saveProducto(request);
        return new ResponseEntity<>(savedProducto, HttpStatus.CREATED);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ProductoResponseDTO> updateProducto(
            @PathVariable Long id,
            @Valid @RequestBody ProductoUpdateRequestDTO request) {
        return ResponseEntity.ok(productoService.updateProducto(id, request));
    }
    
    
}
