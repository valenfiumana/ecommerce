package com.uade.tpo.ecommerce.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.uade.tpo.ecommerce.dto.Resena.ResenaResponseDTO;
import com.uade.tpo.ecommerce.dto.Resena.VendedorResumenDTO;
import com.uade.tpo.ecommerce.model.Resena;

/**
 * Convierte entidades Resena a sus DTOs de respuesta.
 * No tiene lógica de negocio: solo mapea campos.
 * 
 * Al separarlo del servicio, el código de negocio queda limpio y más legible.
 * 
 * Anotación @Component:
 * - Le dice a Spring que gestione esta clase automáticamente
 * - Spring la crea como un singleton (una sola instancia)
 * - Podés inyectarla con @Autowired en servicios y controllers
 */
@Component
public class ResenaMapper {

    /**
     * Convierte una entidad Resena a ResenaResponseDTO.
     * 
     * Extrae los campos de la entidad y los copia al DTO.
     * También obtiene el nombre del comprador de la relación.
     * 
     * @param resena la entidad JPA (viene de la BD)
     * @return el DTO (lo q envías al cliente)
     */
    public ResenaResponseDTO toResponseDTO(Resena resena) {
        return ResenaResponseDTO.builder()
                .id(resena.getId())
                .puntuacion(resena.getPuntuacion())
                .comentario(resena.getComentario())
                .fecha(resena.getFecha())
                // Extraer el nombre del comprador: nombre + apellido
                .nombreComprador(resena.getComprador().getNombre() + " " + 
                                 resena.getComprador().getApellido())
                .build();
    }

    /**
     * Convierte una lista de Resenas a una lista de ResenaResponseDTOs.
     * 
     * Usa streams de Java para convertir cada elemento:
     * - map(this::toResponseDTO) = aplica toResponseDTO a cada Resena
     * - collect(Collectors.toList()) = recolecta en una lista
     * 
     * @param resenas lista de entidades JPA
     * @return lista de DTOs
     */
    public List<ResenaResponseDTO> toResponseDTOList(List<Resena> resenas) {
        return resenas.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Crea un VendedorResumenDTO con la reputación agregada del vendedor.
     * 
     * Los parámetros vienen directo del ResenaRepository:
     * - promedio es el resultado de @Query AVG(...)
     * - cantidad es el resultado de @Query COUNT(...)
     * - ventas es el total de PedidoItems vendidos
     * 
     * @param promedioPuntuacion el AVG de puntuaciones (ej: 4.5)
     * @param cantidadResenas el COUNT de reseñas (ej: 15)
     * @param cantidadVentas el total de productos vendidos (ej: 50)
     * @return DTO con el resumen
     */
    public VendedorResumenDTO toVendedorResumen(Double promedioPuntuacion, 
                                                 Long cantidadResenas, 
                                                 Long cantidadVentas) {
        return VendedorResumenDTO.builder()
                .promedioPuntuacion(promedioPuntuacion)
                .cantidadResenas(cantidadResenas)
                .cantidadVentas(cantidadVentas)
                .build();
    }
}