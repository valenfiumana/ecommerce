package com.uade.tpo.ecommerce.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Representa una reseña/calificación que un usuario hace después de comprar.
 * 
 * Una reseña:
 * - Solo se puede hacer si el pedido está ENTREGADO
 * - Solo el comprador puede reseñar
 * - Solo UNA reseña por PedidoItem (para evitar duplicados)
 * - Incluye puntuación (1-5 estrellas) y comentario opcional
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "resenas")
public class Resena {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // El usuario que escribió la reseña (quien compró)
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "comprador_id", nullable = false)
    private Usuario comprador;

    // La línea del pedido a la que corresponde esta reseña
    // Esto nos permite saber exactamente qué producto se reseña y en qué pedido
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_item_id", nullable = false)
    private PedidoItem pedidoItem;

    // Calificación: debe estar entre 1 y 5
    // @Min(1) y @Max(5) validan automáticamente
    @Column(nullable = false)
    @Min(1)
    @Max(5)
    private Integer puntuacion;

    // Comentario opcional (puede ser NULL)
    // Máximo 1000 caracteres
    @Column(length = 1000)
    private String comentario;

    // Cuándo se creó la reseña (se asigna automáticamente en el servicio)
    @Column(nullable = false)
    private LocalDateTime fecha;
}