package com.uade.tpo.ecommerce.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.uade.tpo.ecommerce.dto.producto.ProductoCreateRequestDTO;
import com.uade.tpo.ecommerce.dto.producto.ProductoResponseDTO;
import com.uade.tpo.ecommerce.dto.producto.ProductoUpdateRequestDTO;
import com.uade.tpo.ecommerce.exception.ArgumentInvalidException;
import com.uade.tpo.ecommerce.exception.ResourceNotFoundException;
import com.uade.tpo.ecommerce.model.Producto;
import com.uade.tpo.ecommerce.repository.ProductoRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class ProductoService {
 
    @Autowired
    private ProductoRepository productoRepository;

    private static final String RECURSO_PRODUCTO = "Producto";
    
    public List<ProductoResponseDTO> getAllProductos() {
        return productoRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public ProductoResponseDTO getProductoById(Long id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(RECURSO_PRODUCTO, id));
        return toResponse(producto);
    }

    public void deleteProductoById(Long id) {
        if (!productoRepository.existsById(id)) {
            throw new ResourceNotFoundException(RECURSO_PRODUCTO, id);
        }
        productoRepository.deleteById(id);
    }

    // Validaciones de negocio adicionales: los DTOs ya validan formato con @Valid en el controlador
    public ProductoResponseDTO saveProducto(ProductoCreateRequestDTO request) {
        validarPrecioYStock(request.getPrecio(), request.getStock());

        Producto producto = Producto.builder()
                .nombre(request.getNombre())
                .descripcion(request.getDescripcion())
                .precio(request.getPrecio())
                .stock(request.getStock())
                .build();
        
        Producto guardado = productoRepository.save(producto);
        return toResponse(guardado);
    }

    public ProductoResponseDTO updateProducto(Long id, ProductoUpdateRequestDTO request) {
        validarPrecioYStock(request.getPrecio(), request.getStock());

        Producto existente = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(RECURSO_PRODUCTO, id));

        existente.setNombre(request.getNombre());
        existente.setDescripcion(request.getDescripcion());
        existente.setPrecio(request.getPrecio());
        existente.setStock(request.getStock());

        return toResponse(productoRepository.save(existente));
    }

    private void validarPrecioYStock(Double precio, Integer stock) {
        if (precio != null && precio <= 0) {
            throw new ArgumentInvalidException("El precio debe ser mayor que cero");
        }
        if (stock != null && stock < 0) {
            throw new ArgumentInvalidException("El stock no puede ser negativo");
        }
    }

    private ProductoResponseDTO toResponse(Producto producto) {
        return ProductoResponseDTO.builder()
                .id(producto.getId())
                .nombre(producto.getNombre())
                .descripcion(producto.getDescripcion())
                .precio(producto.getPrecio())
                .stock(producto.getStock())
                .build();
    }
}
