package com.uade.tpo.ecommerce.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.uade.tpo.ecommerce.exception.BusinessRuleException;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Representa un pedido confirmado.
 *
 * Los precios de cada línea se guardan como SNAPSHOT al momento del checkout.
 * Si el producto cambia de precio después, los pedidos viejos no se ven afectados.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "pedidos")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // El comprador siempre viene del JWT, nunca del body del request.
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "comprador_id", nullable = false)
    private Usuario comprador;

    // Fecha y hora  en que se confirmó el pedido.
    @Column(nullable = false)
    private LocalDateTime fecha;

    // Suma de (precioUnitario × cantidad) de todas las líneas, calculado al hacer checkout.
    @Column(nullable = false)
    private Double total;

    // Estado actual del pedido (ej: "PAGADO").
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstadoPedido estado;

    // Dirección de envío como texto libre o resumen generado desde snapshot (puede ser null).
    private String direccionEnvio;

    /** Copia estructurada al confirmar el pedido (p. ej. desde {@code direccionId} en checkout). */
    @Embedded
    private DireccionSnapshot direccionSnapshot;

    // Notas opcionales del comprador al hacer el checkout.
    private String notas;

    // Líneas del pedido. CascadeType.ALL: al guardar el Pedido se guardan también los ítems.
    // orphanRemoval: si se saca un ítem de la lista, también se borra de la BD.
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PedidoItem> items = new ArrayList<>();

    /**
     * Aplica una transición de estado después de validarla.
     * Si la transición no está permitida lanza BusinessRuleException  el
     * GlobalExceptionHandler la convierte en 400 con mensaje claro.
     *
     * @param nuevo estado al que se quiere pasar
     */
    public void transicionarA(EstadoPedido nuevo) {
        if (!this.estado.puedeTransicionarA(nuevo)) {
            throw new BusinessRuleException(
                    String.format("Transición inválida: no se puede pasar de %s a %s.", this.estado, nuevo));
        }
        this.estado = nuevo;
    }
}
