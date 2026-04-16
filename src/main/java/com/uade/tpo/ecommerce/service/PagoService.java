package com.uade.tpo.ecommerce.service;

import java.util.concurrent.ThreadLocalRandom;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.uade.tpo.ecommerce.dto.pago.PagoMockRequestDTO;
import com.uade.tpo.ecommerce.dto.pedido.PedidoResponseDTO;
import com.uade.tpo.ecommerce.exception.BusinessRuleException;
import com.uade.tpo.ecommerce.exception.ConflictException;
import com.uade.tpo.ecommerce.exception.ResourceNotFoundException;
import com.uade.tpo.ecommerce.model.EstadoPedido;
import com.uade.tpo.ecommerce.model.Pedido;
import com.uade.tpo.ecommerce.model.Usuario;
import com.uade.tpo.ecommerce.model.pago.PagoMockResultado;
import com.uade.tpo.ecommerce.repository.PedidoRepository;
import com.uade.tpo.ecommerce.repository.UsuarioRepository;

import jakarta.transaction.Transactional;

/**
 * Simulación de pasarela de pago para desarrollo.
 * <p>
 * <b>Solo debe usarse con {@code app.payments.mock-enabled=true}</b> (típicamente perfil {@code dev}).
 * En producción el flag debe estar en {@code false}: el endpoint responderá error de negocio.
 * </p>
 * <p>
 * El stock del catálogo ya se descuenta en {@link PedidoService#confirmarDesdeCarrito};
 * aprobar el pago mock <b>no</b> vuelve a tocar stock.
 * </p>
 */
@Service
@Transactional
public class PagoService {

    private final PedidoRepository pedidoRepository;
    private final UsuarioRepository usuarioRepository;
    private final PedidoMapper pedidoMapper;

    private final boolean mockEnabled;
    private final boolean mockRandomOutcome;
    private final boolean mockRechazoCancelaPedido;

    public PagoService(
            PedidoRepository pedidoRepository,
            UsuarioRepository usuarioRepository,
            PedidoMapper pedidoMapper,
            @Value("${app.payments.mock-enabled:false}") boolean mockEnabled,
            @Value("${app.payments.mock-random-outcome:false}") boolean mockRandomOutcome,
            @Value("${app.payments.mock-rechazo-cancela-pedido:false}") boolean mockRechazoCancelaPedido) {
        this.pedidoRepository = pedidoRepository;
        this.usuarioRepository = usuarioRepository;
        this.pedidoMapper = pedidoMapper;
        this.mockEnabled = mockEnabled;
        this.mockRandomOutcome = mockRandomOutcome;
        this.mockRechazoCancelaPedido = mockRechazoCancelaPedido;
    }

    public PedidoResponseDTO procesarMock(PagoMockRequestDTO request) {
        if (!mockEnabled) {
            throw new BusinessRuleException(
                    "El pago simulado está deshabilitado (app.payments.mock-enabled=false). "
                            + "Activalo solo en desarrollo.");
        }

        Usuario comprador = requireUsuarioAutenticado();
        Pedido pedido = pedidoRepository.findById(request.getPedidoId())
                .orElseThrow(() -> new ResourceNotFoundException("Pedido", request.getPedidoId()));

        if (!pedido.getComprador().getId().equals(comprador.getId())) {
            throw new AccessDeniedException("No podés pagar el pedido de otro usuario.");
        }

        if (pedido.getEstado() != EstadoPedido.PENDIENTE_PAGO) {
            throw new ConflictException(
                    String.format("El pedido no admite pago en su estado actual (%s).", pedido.getEstado()));
        }

        PagoMockResultado resultado = request.getResultado();
        if (resultado == null) {
            if (mockRandomOutcome) {
                resultado = ThreadLocalRandom.current().nextBoolean()
                        ? PagoMockResultado.APROBADO
                        : PagoMockResultado.RECHAZADO;
            } else {
                resultado = PagoMockResultado.APROBADO;
            }
        }

        if (resultado == PagoMockResultado.APROBADO) {
            pedido.transicionarA(EstadoPedido.PAGADO);
        } else {
            if (mockRechazoCancelaPedido) {
                pedido.transicionarA(EstadoPedido.CANCELADO);
            }
            // Si no cancela: el pedido sigue en PENDIENTE_PAGO (pago rechazado, se puede reintentar).
        }

        pedidoRepository.save(pedido);
        return pedidoMapper.toDTO(pedido);
    }

    private Usuario requireUsuarioAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null
                || !auth.isAuthenticated()
                || auth instanceof AnonymousAuthenticationToken
                || auth.getName() == null) {
            throw new AccessDeniedException("Se requiere un usuario autenticado.");
        }
        return usuarioRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new AccessDeniedException("Usuario autenticado no encontrado."));
    }
}
