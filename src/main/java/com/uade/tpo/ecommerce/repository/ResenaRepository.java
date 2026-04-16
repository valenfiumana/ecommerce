package com.uade.tpo.ecommerce.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.uade.tpo.ecommerce.model.Resena;

/**
 * Repositorio para manejar operaciones de la entidad Resena.
 * 
 * Extiende JpaRepository, que proporciona automáticamente:
 * - findAll()
 * - findById(Long id)
 * - save(Resena r)
 * - delete(Resena r)
 * - deleteById(Long id)
 * - count()
 * - existsById(Long id)
 * 
 * Los métodos personalizados abajo añaden funcionalidad específica del negocio.
 */
public interface ResenaRepository extends JpaRepository<Resena, Long> {

    /**
     * Busca una reseña específica por comprador y pedido_item.
     * Útil para verificar si el usuario ya reseñó este producto.
     * 
     * SQL generado automáticamente:
     * SELECT * FROM resenas WHERE comprador_id = ? AND pedido_item_id = ?
     * 
     * @param compradorId ID del usuario que hizo la compra
     * @param pedidoItemId ID de la línea del pedido
     * @return Optional.of(resena) si existe, Optional.empty() si no
     */
    Optional<Resena> findByCompradorIdAndPedidoItemId(Long compradorId, Long pedidoItemId);

    /**
     * Busca todas las reseñas que un vendedor ha recibido.
     * 
     * SQL generado automáticamente (con JOINs internos):
     * SELECT r.* FROM resenas r 
     * JOIN pedido_items pi ON r.pedido_item_id = pi.id 
     * JOIN productos p ON pi.producto_id = p.id 
     * WHERE p.vendedor_id = ?
     * 
     * @param vendedorId ID del vendedor
     * @return Lista de todas las reseñas que tiene ese vendedor
     */
    List<Resena> findByPedidoItem_Producto_VendedorId(Long vendedorId);

    /**
     * Calcula el PROMEDIO de puntuación de un vendedor.
     * 
     * Usa @Query porque es un cálculo de agregación (AVG).
     * Los Query Methods automáticos no pueden hacer AVG fácilmente.
     * 
     * JPQL (sintaxis):
     * - LEFT JOIN para navegar relaciones de forma explícita
     * - AVG(r.puntuacion) = promedio de todas las puntuaciones
     * - :vendedorId = parámetro que pasamos (@Param)
     * 
     * @param vendedorId ID del vendedor
     * @return Valor double con el promedio (ej: 4.5), o NULL si no tiene reseñas
     */
    @Query("SELECT AVG(r.puntuacion) FROM Resena r " +
           "LEFT JOIN r.pedidoItem pi " +
           "LEFT JOIN pi.producto p " +
           "LEFT JOIN p.vendedor v " +
           "WHERE v.id = :vendedorId")
    Double findPromedioCalificacionVendedor(@Param("vendedorId") Long vendedorId);

    /**
     * Cuenta cuántas reseñas tiene un vendedor en total.
     * 
     * JPQL (sintaxis):
     * - LEFT JOIN para navegar explícito
     * - COUNT(r) = cuenta cuántas filas
     * - WHERE filtra solo del vendedor especificado
     * 
     * @param vendedorId ID del vendedor
     * @return Número total de reseñas (ej: 15)
     */
    @Query("SELECT COUNT(r) FROM Resena r " +
           "LEFT JOIN r.pedidoItem pi " +
           "LEFT JOIN pi.producto p " +
           "LEFT JOIN p.vendedor v " +
           "WHERE v.id = :vendedorId")
    Long countReseniasVendedor(@Param("vendedorId") Long vendedorId);

    /**
     * Busca todas las reseñas de un comprador específico.
     * Útil para ver el historial de reseñas que ha dejado un usuario.
     * 
     * @param compradorId ID del usuario
     * @return Lista de reseñas que dejó ese usuario
     */
    List<Resena> findByCompradorId(Long compradorId);

    /**
     * Busca todas las reseñas de un producto específico.
     * 
     * Navega: Resena → PedidoItem → Producto
     * 
     * @param productoId ID del producto a buscar
     * @return Lista de reseñas del producto
     */
    List<Resena> findByPedidoItem_Producto_Id(Long productoId);
}