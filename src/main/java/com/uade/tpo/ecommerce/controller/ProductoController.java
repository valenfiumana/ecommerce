package com.uade.tpo.ecommerce.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.uade.tpo.ecommerce.dto.producto.BusquedaProductoCriteria;
import com.uade.tpo.ecommerce.dto.producto.ProductoCreateRequestDTO;
import com.uade.tpo.ecommerce.dto.producto.ProductoResponseDTO;
import com.uade.tpo.ecommerce.dto.producto.ProductoUpdateRequestDTO;
import com.uade.tpo.ecommerce.service.ProductoService;

import jakarta.validation.Valid;

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

    /**
     * Buscador público del catálogo.
     * Se separa de GET /api/productos para no romper el contrato existente del listado simple.
     */
    @GetMapping("/search")
    public ResponseEntity<Page<ProductoResponseDTO>> buscarProductos(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) Double precioMin,
            @RequestParam(required = false) Double precioMax,
            @RequestParam(required = false) String orden,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        BusquedaProductoCriteria criteria = BusquedaProductoCriteria.builder()
                .q(q)
                .categoriaId(categoriaId)
                .precioMin(precioMin)
                .precioMax(precioMax)
                .orden(orden)
                .build();
        return ResponseEntity.ok(productoService.buscar(criteria, page, size));
    }

    // GET /api/productos/{id} — catálogo público, cuerpo = ProductoResponseDTO.
    @GetMapping("/{id}")
    public ResponseEntity<ProductoResponseDTO> getProductoById(@PathVariable Long id) {
        return ResponseEntity.ok(productoService.getProductoById(id));
    }


    // DELETE /api/productos/{id} — solo dueño o admin (servicio).
    // del http://localhost:8080/api/productos/1 -> elimina el producto con id 1
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProductoById(@PathVariable Long id) {
        productoService.deleteProductoById(id);
        return ResponseEntity.noContent().build();
    }

    // POST /api/productos — vendedor = usuario del JWT (@Valid en el DTO).
    // @Valid: dispara la validación Jakarta Bean Validation definida en el DTO (anotaciones como @NotNull, @NotBlank)
    @PostMapping
    public ResponseEntity<ProductoResponseDTO> saveProducto(@Valid @RequestBody ProductoCreateRequestDTO request) {
        ProductoResponseDTO savedProducto = productoService.saveProducto(request);
        return new ResponseEntity<>(savedProducto, HttpStatus.CREATED);
    }
    
    // PUT /api/productos/{id} — solo dueño o admin (lo decide el servicio).
    @PutMapping("/{id}")
    public ResponseEntity<ProductoResponseDTO> updateProducto(
            @PathVariable Long id,
            @Valid @RequestBody ProductoUpdateRequestDTO request) {
        return ResponseEntity.ok(productoService.updateProducto(id, request));
    }
    
    
}
