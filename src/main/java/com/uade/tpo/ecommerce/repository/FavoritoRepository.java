package com.uade.tpo.ecommerce.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.uade.tpo.ecommerce.model.Favorito;

public interface FavoritoRepository extends JpaRepository<Favorito, Long> {

    @EntityGraph(attributePaths = "producto")
    List<Favorito> findByUsuarioIdOrderByIdDesc(Long usuarioId);

    Optional<Favorito> findByUsuarioIdAndProductoId(Long usuarioId, Long productoId);

    Optional<Favorito> findByIdAndUsuarioId(Long id, Long usuarioId);
}