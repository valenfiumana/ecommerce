package com.uade.tpo.ecommerce.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uade.tpo.ecommerce.model.Direccion;

public interface DireccionRepository extends JpaRepository<Direccion, Long> {

    List<Direccion> findByUsuarioIdOrderByPrincipalDescIdAsc(Long usuarioId);

    boolean existsByIdAndUsuarioId(Long id, Long usuarioId);
}
