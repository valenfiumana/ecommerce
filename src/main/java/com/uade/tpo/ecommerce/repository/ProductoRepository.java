package com.uade.tpo.ecommerce.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.uade.tpo.ecommerce.model.Producto;

// realiza todas la operaciones de CRUD sobre la tabla productos, gracias a que extiende de JpaRepository
// minimiza el boiler plate, JpaRepository ya tiene implementados los métodos básicos de CRUD, por lo que no es necesario escribirlos manualmente
public interface ProductoRepository extends JpaRepository<Producto, Long> {
    //findAll() ya está implementado por JpaRepository, no es necesario definirlo aquí
    // select * from productos

    //save, delete, findById, findAll, update etc. también están implementados por JpaRepository

    //query methods personalizados pueden ser definidos aquí, por ejemplo:
    //findByNombre(String nombre); 
    // la instrucción SQL sería: select * from productos where nombre = nombre

    // el sql seria     select * from productos where nombre like '%nombre%'
    List<Producto> findByNombreContaining(String nombre);


    // sql sería: select * from productos where precio < precio
    @Query("SELECT p FROM Producto p WHERE p.precio < :precio")
    List<Producto> findByPrecioLessThan(Double precio);
    
    //findByPrecioBetween(Double minPrecio, Double maxPrecio); 
    // la instrucción SQL sería: select * from productos where precio between minPrecio and maxPrecio
    // 

    
}
