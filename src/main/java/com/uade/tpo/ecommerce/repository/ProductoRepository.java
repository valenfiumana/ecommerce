package com.uade.tpo.ecommerce.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    /**
     * Lista todos los productos para el catálogo, incluyendo datos del vendedor.
     * {@link EntityGraph}: es una instrucción a JPA: “cuando traigas {Producto},
     * también cargá la relación {vendedor} en la misma ida a la base (con un JOIN), no después”.
     * Si no estuviera (relación {LAZY}): primero iría un {SELECT} de todos los productos;
     * al armar cada DTO y tocar {producto.getVendedor()}, Hibernate haría otro {SELECT} por cada fila.
     * Eso es el problema N+1: 1 query de lista + N queries de vendedor. Acá lo evitamos: una consulta principal con join al usuario.</p>
     */
    @EntityGraph(attributePaths = "vendedor")
    @Query("SELECT p FROM Producto p")
    List<Producto> findAllForCatalog();

    /**
     * Detalle por id con vendedor incluido; usado en GET público y en PUT/DELETE para evaluar dueño vs actor.
     */
    @EntityGraph(attributePaths = "vendedor")
    @Query("SELECT p FROM Producto p WHERE p.id = :id")
    Optional<Producto> findDetailById(@Param("id") Long id);

    /** Productos publicados por un usuario (perfil / mis publicaciones). */
    List<Producto> findByVendedor_IdOrderByIdDesc(Long vendedorId);

    
    //findByPrecioBetween(Double minPrecio, Double maxPrecio); 
    // la instrucción SQL sería: select * from productos where precio between minPrecio and maxPrecio
    // 

    
}
