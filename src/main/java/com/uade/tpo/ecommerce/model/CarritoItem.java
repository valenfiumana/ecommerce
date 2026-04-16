package com.uade.tpo.ecommerce.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Una fila: usuario + producto + cantidad. No puede haber dos filas del mismo producto (índice único).
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
// uk_carrito_usuario_producto: un usuario solo tiene una fila por producto (el servicio suma cantidades).
@Table(
        name = "carrito_items",
        uniqueConstraints = @UniqueConstraint(name = "uk_carrito_usuario_producto", columnNames = { "usuario_id", "producto_id" })
)
public class CarritoItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // PK de la fila en carrito_items

    // Dueño del carrito (mismo usuario que en el JWT).
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    // Producto publicado (stock se lee de acá al validar en el servicio).
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    // Cuántas unidades quiere; no puede ser más que el stock del producto (lo chequea el servicio).
    @Column(nullable = false)
    private Integer cantidad;
}
