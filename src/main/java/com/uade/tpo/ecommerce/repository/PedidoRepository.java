package com.uade.tpo.ecommerce.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.uade.tpo.ecommerce.model.Pedido;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    /**
     * Historial de pedidos de un comprador, del más reciente al más antiguo.
     */
    @Query("SELECT p FROM Pedido p WHERE p.comprador.id = :compradorId ORDER BY p.fecha DESC")
    List<Pedido> findByCompradorIdOrderByFechaDesc(@Param("compradorId") Long compradorId);
}
