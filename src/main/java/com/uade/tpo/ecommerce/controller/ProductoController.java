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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Productos", description = "Catálogo, búsqueda y CRUD (mutaciones con JWT)")
@RestController
// para acceder a este controlador, la URL base será /api/productos
// http://localhost:8080/api/productos -> devuelve la lista de productos
@RequestMapping("/api/productos")
public class ProductoController {

    
    @Autowired
    private ProductoService productoService;

    @Operation(summary = "Listar catálogo", description = "Listado completo público (sin paginación). Incluye datos mínimos del vendedor.")
    @GetMapping
    public ResponseEntity<List<ProductoResponseDTO>> getAllProductos() {
        return ResponseEntity.ok(productoService.getAllProductos());
    }

    /**
     * Buscador público del catálogo.
     * Se separa de GET /api/productos para no romper el contrato existente del listado simple.
     */
    @Operation(summary = "Buscar productos", description = "Catálogo público con filtros opcionales: q (texto), categoriaId, precioMin/Max, orden; page/size para paginación.")
    @GetMapping("/search")
    public ResponseEntity<Page<ProductoResponseDTO>> buscarProductos(
            @Parameter(description = "Texto en nombre o descripción") @RequestParam(required = false) String q,
            @Parameter(description = "ID categoría") @RequestParam(required = false) Long categoriaId,
            @Parameter(description = "Precio mínimo") @RequestParam(required = false) Double precioMin,
            @Parameter(description = "Precio máximo") @RequestParam(required = false) Double precioMax,
            @Parameter(description = "Orden (whitelist en servidor)") @RequestParam(required = false) String orden,
            @Parameter(description = "Página (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página") @RequestParam(defaultValue = "10") int size) {
        BusquedaProductoCriteria criteria = BusquedaProductoCriteria.builder()
                .q(q)
                .categoriaId(categoriaId)
                .precioMin(precioMin)
                .precioMax(precioMax)
                .orden(orden)
                .build();
        return ResponseEntity.ok(productoService.buscar(criteria, page, size));
    }

    @Operation(summary = "Detalle de producto", description = "Público. Incluye vendedorId y vendedorNombre.")
    @GetMapping("/{id}")
    public ResponseEntity<ProductoResponseDTO> getProductoById(
            @Parameter(description = "ID del producto") @PathVariable Long id) {
        return ResponseEntity.ok(productoService.getProductoById(id));
    }


    // DELETE /api/productos/{id} — solo dueño o admin (servicio).
    // del http://localhost:8080/api/productos/1 -> elimina el producto con id 1
    @Operation(summary = "Eliminar producto", description = "Solo vendedor dueño o ADMIN. 204.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProductoById(@Parameter(description = "ID del producto") @PathVariable Long id) {
        productoService.deleteProductoById(id);
        return ResponseEntity.noContent().build();
    }

    // POST /api/productos — vendedor = usuario del JWT (@Valid en el DTO).
    // @Valid: dispara la validación Jakarta Bean Validation definida en el DTO (anotaciones como @NotNull, @NotBlank)
    @Operation(summary = "Crear producto", description = "Vendedor = usuario del JWT (no se acepta vendedorId en body). 201.")
    @PostMapping
    public ResponseEntity<ProductoResponseDTO> saveProducto(@Valid @RequestBody ProductoCreateRequestDTO request) {
        ProductoResponseDTO savedProducto = productoService.saveProducto(request);
        return new ResponseEntity<>(savedProducto, HttpStatus.CREATED);
    }
    
    // PUT /api/productos/{id} — solo dueño o admin (lo decide el servicio).
    @Operation(summary = "Actualizar producto", description = "Solo dueño o ADMIN. No cambia vendedor_id.")
    @PutMapping("/{id}")
    public ResponseEntity<ProductoResponseDTO> updateProducto(
            @Parameter(description = "ID del producto") @PathVariable Long id,
            @Valid @RequestBody ProductoUpdateRequestDTO request) {
        return ResponseEntity.ok(productoService.updateProducto(id, request));
    }
    
    
}
