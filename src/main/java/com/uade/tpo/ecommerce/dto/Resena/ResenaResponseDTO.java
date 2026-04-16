package com.uade.tpo.ecommerce.dto.Resena;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de salida (response) para exponer una reseña en la API.
 * 
 * Cuando el cliente GET /api/resenas/{id}, recibe esto:
 * {
 *   "id": 1,
 *   "puntuacion": 5,
 *   "comentario": "Excelente producto",
 *   "fecha": "2026-04-16T14:30:45",
 *   "nombreComprador": "Juan Pérez"
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResenaResponseDTO {

    /** ID único de la reseña (generado por la BD) */
    private Long id;

    /** Puntuación del 1-5 */
    private Integer puntuacion;

    /** Comentario que escribió el comprador */
    private String comentario;

    /** Cuándo se creó la reseña */
    private LocalDateTime fecha;

    /**
     * Nombre del usuario que escribió la reseña.
     * Se muestra así: "Pedro García - ⭐⭐⭐⭐⭐"
     */
    private String nombreComprador;
}