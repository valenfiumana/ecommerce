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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Una línea dentro de un pedido.
 *
 * precioUnitario es el SNAPSHOT del precio del producto al momento de confirmar el pedido.
 * Si mañana el vendedor cambia el precio del Producto, este campo NO cambia.
 * El subtotal también se persiste para no tener que recalcularlo.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "pedido_items")
public class PedidoItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FK al pedido al que pertenece esta línea.
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id", nullable = false)
    private Pedido pedido;

    // Referencia al producto. Si el producto se elimina en el futuro,
    // la FK queda null pero el precio snapshot sigue en precioUnitario.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id")
    private Producto producto;

    @Column(nullable = false)
    private Integer cantidad;

    // Precio unitario copiado del Producto.precio en el momento exacto del checkout.
    // Nunca se actualiza aunque el producto cambie de precio.
    @Column(nullable = false)
    private Double precioUnitario;

    // subtotal = precioUnitario * cantidad. Se persiste para no recalcular.
    @Column(nullable = false)
    private Double subtotal;
}
