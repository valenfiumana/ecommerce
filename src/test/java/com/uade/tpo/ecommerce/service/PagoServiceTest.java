package com.uade.tpo.ecommerce.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import com.uade.tpo.ecommerce.dto.pago.PagoMockRequestDTO;
import com.uade.tpo.ecommerce.dto.pedido.PedidoResponseDTO;
import com.uade.tpo.ecommerce.exception.BusinessRuleException;
import com.uade.tpo.ecommerce.exception.ConflictException;
import com.uade.tpo.ecommerce.model.EstadoPedido;
import com.uade.tpo.ecommerce.model.Pedido;
import com.uade.tpo.ecommerce.model.Usuario;
import com.uade.tpo.ecommerce.model.pago.PagoMockResultado;
import com.uade.tpo.ecommerce.repository.PedidoRepository;
import com.uade.tpo.ecommerce.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class PagoServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PedidoMapper pedidoMapper;

    private PagoService pagoServiceHabilitado;
    private PagoService pagoServiceDeshabilitado;

    private Usuario comprador;

    @BeforeEach
    void setUp() {
        comprador = Usuario.builder().id(10L).email("a@a.com").build();
        pagoServiceHabilitado = new PagoService(
                pedidoRepository,
                usuarioRepository,
                pedidoMapper,
                true,
                false,
                false);
        pagoServiceDeshabilitado = new PagoService(
                pedidoRepository,
                usuarioRepository,
                pedidoMapper,
                false,
                false,
                false);
        SecurityContextHolder.clearContext();
    }

    private void login(Usuario u) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        u.getEmail(),
                        "n/a",
                        java.util.List.of(new SimpleGrantedAuthority("ROLE_USER"))));
    }

    @Test
    void mockDeshabilitadoLanzaBusinessRule() {
        login(comprador);

        assertThrows(
                BusinessRuleException.class,
                () -> pagoServiceDeshabilitado.procesarMock(
                        PagoMockRequestDTO.builder().pedidoId(1L).resultado(PagoMockResultado.APROBADO).build()));
    }

    @Test
    void apruebaPagoYpasaAPagado() {
        login(comprador);
        when(usuarioRepository.findByEmail("a@a.com")).thenReturn(Optional.of(comprador));

        Pedido pedido = Pedido.builder()
                .id(5L)
                .comprador(comprador)
                .estado(EstadoPedido.PENDIENTE_PAGO)
                .build();
        when(pedidoRepository.findById(5L)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(pedidoMapper.toDTO(any())).thenReturn(PedidoResponseDTO.builder().id(5L).estado(EstadoPedido.PAGADO).build());

        PedidoResponseDTO res = pagoServiceHabilitado.procesarMock(
                PagoMockRequestDTO.builder().pedidoId(5L).resultado(PagoMockResultado.APROBADO).build());

        ArgumentCaptor<Pedido> cap = ArgumentCaptor.forClass(Pedido.class);
        verify(pedidoRepository).save(cap.capture());
        assertEquals(EstadoPedido.PAGADO, cap.getValue().getEstado());
        assertEquals(EstadoPedido.PAGADO, res.getEstado());
    }

    @Test
    void rechazoSinCancelarDejaPendientePago() {
        login(comprador);
        when(usuarioRepository.findByEmail("a@a.com")).thenReturn(Optional.of(comprador));

        Pedido pedido = Pedido.builder()
                .id(5L)
                .comprador(comprador)
                .estado(EstadoPedido.PENDIENTE_PAGO)
                .build();
        when(pedidoRepository.findById(5L)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(pedidoMapper.toDTO(any())).thenAnswer(inv -> {
            Pedido p = inv.getArgument(0);
            return PedidoResponseDTO.builder().id(p.getId()).estado(p.getEstado()).build();
        });

        pagoServiceHabilitado.procesarMock(
                PagoMockRequestDTO.builder().pedidoId(5L).resultado(PagoMockResultado.RECHAZADO).build());

        ArgumentCaptor<Pedido> cap = ArgumentCaptor.forClass(Pedido.class);
        verify(pedidoRepository).save(cap.capture());
        assertEquals(EstadoPedido.PENDIENTE_PAGO, cap.getValue().getEstado());
    }

    @Test
    void rechazoConCancelacionActiva() {
        login(comprador);
        when(usuarioRepository.findByEmail("a@a.com")).thenReturn(Optional.of(comprador));

        PagoService svc = new PagoService(pedidoRepository, usuarioRepository, pedidoMapper, true, false, true);

        Pedido pedido = Pedido.builder()
                .id(5L)
                .comprador(comprador)
                .estado(EstadoPedido.PENDIENTE_PAGO)
                .build();
        when(pedidoRepository.findById(5L)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(pedidoMapper.toDTO(any())).thenReturn(PedidoResponseDTO.builder().estado(EstadoPedido.CANCELADO).build());

        svc.procesarMock(
                PagoMockRequestDTO.builder().pedidoId(5L).resultado(PagoMockResultado.RECHAZADO).build());

        ArgumentCaptor<Pedido> cap = ArgumentCaptor.forClass(Pedido.class);
        verify(pedidoRepository).save(cap.capture());
        assertEquals(EstadoPedido.CANCELADO, cap.getValue().getEstado());
    }

    @Test
    void pedidoDeOtroUsuario403() {
        login(comprador);
        when(usuarioRepository.findByEmail("a@a.com")).thenReturn(Optional.of(comprador));

        Usuario otro = Usuario.builder().id(99L).email("otro@a.com").build();
        Pedido pedido = Pedido.builder()
                .id(5L)
                .comprador(otro)
                .estado(EstadoPedido.PENDIENTE_PAGO)
                .build();
        when(pedidoRepository.findById(5L)).thenReturn(Optional.of(pedido));

        assertThrows(
                AccessDeniedException.class,
                () -> pagoServiceHabilitado.procesarMock(
                        PagoMockRequestDTO.builder().pedidoId(5L).resultado(PagoMockResultado.APROBADO).build()));

        verify(pedidoRepository, never()).save(any());
    }

    @Test
    void estadoNoPendientePago409() {
        login(comprador);
        when(usuarioRepository.findByEmail("a@a.com")).thenReturn(Optional.of(comprador));

        Pedido pedido = Pedido.builder()
                .id(5L)
                .comprador(comprador)
                .estado(EstadoPedido.PAGADO)
                .build();
        when(pedidoRepository.findById(5L)).thenReturn(Optional.of(pedido));

        assertThrows(
                ConflictException.class,
                () -> pagoServiceHabilitado.procesarMock(
                        PagoMockRequestDTO.builder().pedidoId(5L).resultado(PagoMockResultado.APROBADO).build()));
    }
}
