package com.uade.tpo.ecommerce.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.uade.tpo.ecommerce.model.PedidoItem;

/**
 * Repositorio para manejar operaciones CRUD de la entidad PedidoItem.
 * 
 * Extiende JpaRepository, que proporciona automáticamente:
 * - findAll()
 * - findById(Long id)
 * - save(PedidoItem item)
 * - delete(PedidoItem item)
 * - deleteById(Long id)
 * - count()
 * - existsById(Long id)
 * 
 * Los métodos personalizados abajo añaden funcionalidad específica del negocio.
 */
public interface PedidoItemRepository extends JpaRepository<PedidoItem, Long> {

    /**
     * Busca todos los PedidoItems de un pedido específico.
     * 
     * @param pedidoId ID del pedido
     * @return Lista de items del pedido
     */
    List<PedidoItem> findByPedidoId(Long pedidoId);

    /**
     * Busca todos los PedidoItems de un usuario (comprador) ordenados por ID ascendente,
     * y carga el Producto asociado (join).
     * 
     * Útil para el carrito del usuario.
     * 
     * @param usuarioId ID del usuario comprador
     * @return Lista de items del carrito del usuario
     */
    @Query("SELECT pi FROM PedidoItem pi " +
           "JOIN FETCH pi.pedido p " +
           "WHERE p.comprador.id = :usuarioId " +
           "ORDER BY pi.id ASC")
    List<PedidoItem> findByUsuarioIdOrderByIdAscWithProducto(@Param("usuarioId") Long usuarioId);

    /**
     * Cuenta cuántos productos ha vendido un vendedor específico.
     * 
     * Se cuenta por la cantidad total de unidades en los PedidoItems
     * cuyo producto fue publicado por ese vendedor.
     * 
     * @param vendedorId ID del vendedor
     * @return Cantidad total de productos vendidos
     */
    @Query("SELECT COALESCE(SUM(pi.cantidad), 0) FROM PedidoItem pi " +
           "WHERE pi.producto.vendedor.id = :vendedorId")
    Long countByProductoVendedorId(@Param("vendedorId") Long vendedorId);
}
