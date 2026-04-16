package com.uade.tpo.ecommerce.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// @NoArgsConstructor / @AllArgsConstructor: JPA necesita constructor sin argumentos; Lombok @Builder combina con @AllArgsConstructor.
// @Builder: patrón Builder — se construye con Producto.builder().nombre("...").precio(9.99).stock(10).build();
// cada llamada .campo(valor) fija un atributo; .build() devuelve la instancia lista (ver también comentario en Usuario).
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "productos")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    private String descripcion;

    @Column(nullable = false)
    private Double precio;
    @Column(nullable = false)
    private Integer stock;

    /**
     * Dueño de la publicación en el marketplace: quien responde por precio y stock.
     * <p>{@code LAZY}: el usuario no se carga hasta que el código accede a {@code vendedor} (ahorra joins si no hace falta).</p>
     * <p>La FK en BD puede quedar {@code NULL} en productos creados antes de esta relación; el servicio exige vendedor en altas nuevas
     * y solo admin puede tocar legados sin vendedor.</p>
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendedor_id")
    private Usuario vendedor;

    // private String imagenUrl;

    // @Builder.Default: al usar Producto.builder()...build(), si no se asigna categorías, se usa lista vacía en lugar de null
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "productos_categorias",
            joinColumns = @JoinColumn(name = "producto_id"),
            inverseJoinColumns = @JoinColumn(name = "categoria_id"))
    @Builder.Default
    private List<Categoria> categorias = new ArrayList<>();
}
