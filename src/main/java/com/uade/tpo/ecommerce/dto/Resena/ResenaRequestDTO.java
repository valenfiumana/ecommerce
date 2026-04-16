package com.uade.tpo.ecommerce.dto.Resena;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de entrada (request) para crear una reseña.
 * 
 * El cliente envía esto en el body del POST:
 * {
 *   "puntuacion": 5,
 *   "comentario": "Excelente producto, muy recomendado"
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResenaRequestDTO {

    /**
     * Puntuación en estrellas de 1 a 5.
     * @NotNull: obligatorio (no puede ser null)
     * @Min(1): mínimo 1 estrella
     * @Max(5): máximo 5 estrellas
     */
    @NotNull(message = "La puntuación es obligatoria")
    @Min(value = 1, message = "La puntuación debe ser mínimo 1 estrella")
    @Max(value = 5, message = "La puntuación debe ser máximo 5 estrellas")
    private Integer puntuacion;

    /**
     * Comentario opcional que el usuario puede incluir.
     * Si no lo envía, será null en la BD.
     */
    private String comentario;
}