package com.uade.tpo.ecommerce.dto.producto;

import java.util.List;

import com.uade.tpo.ecommerce.dto.categoria.CategoriaResponseDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de salida (response) para exponer un producto en la API sin devolver la entidad JPA.
 * Incluye los campos que el cliente necesita ver tras crear, leer o actualizar.
 * <p>Datos del vendedor: solo lo imprescindible para mostrar en catálogo (id + nombre visible), no email ni contraseña.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductoResponseDTO {

    private Long id;
    private String nombre;
    private String descripcion;
    private Double precio;
    private Integer stock;
    private List<CategoriaResponseDTO> categorias;
    private List<String> imagenes;

    /** Id del usuario que publicó; útil para enlaces o soporte, no reemplaza políticas de autorización en servidor. */
    private Long vendedorId;
    /** Texto amigable armado en el servicio (ej. nombre + inicial del apellido); nunca el email. */
    private String vendedorNombre;
}
