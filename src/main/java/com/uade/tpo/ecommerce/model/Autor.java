package com.uade.tpo.ecommerce.model;

import java.util.List;

import jakarta.persistence.*;
import lombok.Data;


/**
 * Entidad de ejemplo en relación {@link Libro} (OneToMany/ManyToOne). 
 * Tabla {@code autores}.
 */
@Data
@Entity
@Table(name = "autores")
public class Autor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String nombre;
    private String nacionalidad;
    private String biografía;

    @OneToMany(mappedBy = "autor", fetch = FetchType.LAZY)
    private List<Libro> libros;
    // trae los libros del autor, si y solo sí los solicito, sino no los trae por el lazy
    //autor.getLibros(); 

    //sin lazy
    // select * from autores a left join libros l on a.id = l.autor_id where a.id = 1; //

    // con lazy
    // select * from autores a where a.id = 1; // solo trae el autor

    // solo trae los libros del autor si los solicito con get
    // autor.getLibros();
}
