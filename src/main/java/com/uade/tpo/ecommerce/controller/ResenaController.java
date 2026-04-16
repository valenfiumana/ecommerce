package com.uade.tpo.ecommerce.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.uade.tpo.ecommerce.dto.Resena.ResenaRequestDTO;
import com.uade.tpo.ecommerce.dto.Resena.ResenaResponseDTO;
import com.uade.tpo.ecommerce.dto.Resena.VendedorResumenDTO;
import com.uade.tpo.ecommerce.service.ResenaService;

import jakarta.validation.Valid;

/**
 * Controller REST para manejo de reseñas y reputación de vendedores.
 * 
 * Endpoints:
 * - POST /api/resenas → Crear una reseña (autenticado)
 * - GET /api/productos/{id}/resenas → Listar reseñas de un producto (público)
 * - GET /api/vendedores/{id}/resumen → Resumen de vendedor (público)
 */
@RestController
@RequestMapping("/api/resenas")
public class ResenaController {

    @Autowired
    private ResenaService resenaService;

    /**
     * Crear una nueva reseña para un producto.
     * 
     * El usuario autenticado (del JWT) es quien hace la reseña.
     * 
     * Ejemplo de request:
     * POST /api/resenas?pedidoItemId=5
     * {
     *   "puntuacion": 5,
     *   "comentario": "Excelente producto, muy recomendado"
     * }
     * 
     * Validaciones (del servicio):
     * - El pedido debe estar ENTREGADO
     * - El usuario autenticado debe ser el comprador
     * - No debe existir reseña previa para este item
     * 
     * @param pedidoItemId ID del PedidoItem a reseñar (query parameter)
     * @param requestDTO datos de la reseña (puntuación + comentario)
     * @return ResponseEntity con código 201 Created y la reseña creada
     * @throws ResourceNotFoundException si el pedidoItem no existe
     * @throws BusinessRuleException si no cumple con las reglas de negocio
     * @throws ConflictException si ya existe una reseña para este item
     */
    @PostMapping
    public ResponseEntity<ResenaResponseDTO> crearResena(
            @RequestParam Long pedidoItemId,
            @Valid @RequestBody ResenaRequestDTO requestDTO) {
        ResenaResponseDTO resena = resenaService.crearResena(pedidoItemId, requestDTO);
        return new ResponseEntity<>(resena, HttpStatus.CREATED);
    }

    /**
     * Obtener todas las reseñas de un producto.
     * 
     * Endpoint PÚBLICO: no requiere autenticación.
     * Útil para ver qué opinaron otros clientes de un producto específico.
     * 
     * Ejemplo:
     * GET /api/productos/123/resenas
     * Retorna:
     * [
     *   {
     *     "id": 1,
     *     "puntuacion": 5,
     *     "comentario": "Muy bueno",
     *     "fecha": "2026-04-16T14:30:45",
     *     "nombreComprador": "Juan Pérez"
     *   },
     *   ... más reseñas
     * ]
     * 
     * @param productoId ID del producto
     * @return ResponseEntity con lista de reseñas
     */
    @GetMapping("/productos/{productoId}")
    public ResponseEntity<List<ResenaResponseDTO>> obtenerReseniasProducto(
            @PathVariable Long productoId) {
        List<ResenaResponseDTO> resenas = resenaService.obtenerReseniasProducto(productoId);
        return ResponseEntity.ok(resenas);
    }

    /**
     * Obtener el resumen de reputación de un vendedor.
     * 
     * Endpoint PÚBLICO: cualquiera puede ver la reputación de un vendedor.
     * 
     * Ejemplo:
     * GET /api/vendedores/42/resumen
     * Retorna:
     * {
     *   "promedioPuntuacion": 4.5,
     *   "cantidadResenas": 15,
     *   "cantidadVentas": 50
     * }
     * 
     * Interpretación:
     * - El vendedor tiene un promedio de 4.5 ⭐
     * - Ha recibido 15 reseñas
     * - Ha vendido 50 productos en total
     * 
     * @param vendedorId ID del usuario vendedor
     * @return ResponseEntity con el resumen de reputación
     * @throws ResourceNotFoundException si el vendedor no existe
     */
    @GetMapping("/vendedores/{vendedorId}")
    public ResponseEntity<VendedorResumenDTO> obtenerResumenVendedor(
            @PathVariable Long vendedorId) {
        VendedorResumenDTO resumen = resenaService.obtenerResumenVendedor(vendedorId);
        return ResponseEntity.ok(resumen);
    }
}
