package com.uade.tpo.ecommerce.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.uade.tpo.ecommerce.model.CarritoItem;

// Repo del carrito. El listado trae el producto junto para no hacer una query por cada línea.
public interface CarritoItemRepository extends JpaRepository<CarritoItem, Long> {

    // Listado del carrito con producto cargado (nombre, precio, stock) sin N+1.
    @EntityGraph(attributePaths = "producto")
    @Query("SELECT c FROM CarritoItem c WHERE c.usuario.id = :usuarioId ORDER BY c.id ASC")
    List<CarritoItem> findByUsuarioIdOrderByIdAscWithProducto(@Param("usuarioId") Long usuarioId);

    // Para merge al agregar: ¿ya hay línea de este user para este producto?
    Optional<CarritoItem> findByUsuarioIdAndProductoId(Long usuarioId, Long productoId);

    // Para DELETE seguro: la línea tiene que ser de ese usuario.
    Optional<CarritoItem> findByIdAndUsuarioId(Long id, Long usuarioId);
}
