package com.uade.tpo.ecommerce.model;

/**
 * Estados posibles de un pedido y las transiciones válidas entre ellos.
 */
public enum EstadoPedido {

    PENDIENTE_PAGO,
    PAGADO,
    ENVIADO,
    ENTREGADO,
    CANCELADO;

    /**
     * Indica si la transición desde {@code this} hacia {@code nuevo} es válida.
     * Toda la lógica de transiciones vive acá  ningún servicio la duplica.
     *
     * @param nuevo el estado al que se quiere pasar
     * @return true si la transición está permitida
     */
    public boolean puedeTransicionarA(EstadoPedido nuevo) {
        return switch (this) {
            case PENDIENTE_PAGO -> nuevo == PAGADO    || nuevo == CANCELADO;
            case PAGADO         -> nuevo == ENVIADO   || nuevo == CANCELADO;
            case ENVIADO        -> nuevo == ENTREGADO || nuevo == CANCELADO;
            case ENTREGADO      -> false; // estado final
            case CANCELADO      -> false; // estado final
        };
    }
}
