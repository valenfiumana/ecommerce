package com.uade.tpo.ecommerce.model;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Entidad de ejemplo (dominio libro/autor). Hibernate creará la tabla {@code libros} si no existe; no hay controladores REST aún.
 */
@Data
@Entity
@Table(name = "libros")
public class Libro {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titulo;

    @Column(nullable = false)
    private String isbn;
    private Double precio; 

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "autor_id")
    private Autor autor;


    // libro.getAutor(); // trae el autor del libro, si y solo sí lo solicito, sino no lo trae por el lazy

}
