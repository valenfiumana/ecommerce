package com.uade.tpo.ecommerce.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uade.tpo.ecommerce.model.Producto;



public interface ProductoRepository extends JpaRepository<Producto, Long> {
}
