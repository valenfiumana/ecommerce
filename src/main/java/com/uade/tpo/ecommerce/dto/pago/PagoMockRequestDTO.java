package com.uade.tpo.ecommerce.dto.pago;

import com.uade.tpo.ecommerce.model.pago.PagoMockResultado;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Body de {@code POST /api/pagos/mock}.
 * <p>
 * {@code resultado} es opcional: si falta y {@code app.payments.mock-random-outcome=true},
 * el servidor elige al azar (solo útil en dev).
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagoMockRequestDTO {

    @NotNull(message = "pedidoId es obligatorio")
    private Long pedidoId;

    /** Si es null y el modo random está desactivado, se asume APROBADO. */
    private PagoMockResultado resultado;
}
