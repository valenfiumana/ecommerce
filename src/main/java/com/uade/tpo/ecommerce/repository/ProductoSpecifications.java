package com.uade.tpo.ecommerce.repository;

import org.springframework.data.jpa.domain.Specification;

import com.uade.tpo.ecommerce.model.Categoria;
import com.uade.tpo.ecommerce.model.Producto;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;

/**
 * Specifications reutilizables para componer búsquedas dinámicas sobre productos.
 * Cada filtro se agrega solo si el cliente envía el parámetro correspondiente.
 */
public final class ProductoSpecifications {

    private ProductoSpecifications() {
    }

    public static Specification<Producto> textoLibre(String textoBuscado) {
        return (root, query, cb) -> {
            if (textoBuscado == null || textoBuscado.isBlank()) {
                return cb.conjunction();
            }

            String textoLike = "%" + textoBuscado.trim().toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("nombre")), textoLike),
                    cb.like(cb.lower(cb.coalesce(root.get("descripcion"), "")), textoLike));
        };
    }

    public static Specification<Producto> categoriaId(Long categoriaId) {
        return (root, query, cb) -> {
            if (categoriaId == null) {
                return cb.conjunction();
            }

            query.distinct(true);
            Join<Producto, Categoria> categoria = root.join("categorias", JoinType.INNER);
            return cb.equal(categoria.get("id"), categoriaId);
        };
    }

    public static Specification<Producto> precioMin(Double precioMin) {
        return (root, query, cb) -> precioMin == null
                ? cb.conjunction()
                : cb.greaterThanOrEqualTo(root.get("precio"), precioMin);
    }

    public static Specification<Producto> precioMax(Double precioMax) {
        return (root, query, cb) -> precioMax == null
                ? cb.conjunction()
                : cb.lessThanOrEqualTo(root.get("precio"), precioMax);
    }
}
