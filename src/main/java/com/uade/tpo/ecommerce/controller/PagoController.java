package com.uade.tpo.ecommerce.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uade.tpo.ecommerce.dto.pago.PagoMockRequestDTO;
import com.uade.tpo.ecommerce.dto.pedido.PedidoResponseDTO;
import com.uade.tpo.ecommerce.service.PagoService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Pagos simulados (mock) — ver {@link PagoService}.
 * <p>
 * Requiere JWT. En producción desactivar con {@code app.payments.mock-enabled=false}.
 * </p>
 */
@RestController
@RequestMapping("/api/pagos")
@RequiredArgsConstructor
public class PagoController {

    private final PagoService pagoService;

    @PostMapping("/mock")
    public ResponseEntity<PedidoResponseDTO> pagarMock(@Valid @RequestBody PagoMockRequestDTO request) {
        return ResponseEntity.ok(pagoService.procesarMock(request));
    }
}
