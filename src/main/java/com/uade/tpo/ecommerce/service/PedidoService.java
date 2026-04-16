package com.uade.tpo.ecommerce.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.uade.tpo.ecommerce.dto.pedido.CambioEstadoRequestDTO;
import com.uade.tpo.ecommerce.dto.pedido.CheckoutRequestDTO;
import com.uade.tpo.ecommerce.dto.pedido.PedidoResponseDTO;
import com.uade.tpo.ecommerce.dto.pedido.PedidoSummaryResponseDTO;
import com.uade.tpo.ecommerce.exception.BusinessRuleException;
import com.uade.tpo.ecommerce.exception.ResourceNotFoundException;
import com.uade.tpo.ecommerce.model.CarritoItem;
import com.uade.tpo.ecommerce.model.Direccion;
import com.uade.tpo.ecommerce.model.DireccionSnapshot;
import com.uade.tpo.ecommerce.model.EstadoPedido;
import com.uade.tpo.ecommerce.model.Pedido;
import com.uade.tpo.ecommerce.model.PedidoItem;
import com.uade.tpo.ecommerce.model.Role;
import com.uade.tpo.ecommerce.model.Usuario;
import com.uade.tpo.ecommerce.repository.CarritoItemRepository;
import com.uade.tpo.ecommerce.repository.DireccionRepository;
import com.uade.tpo.ecommerce.repository.PedidoRepository;
import com.uade.tpo.ecommerce.repository.UsuarioRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class PedidoService {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private CarritoItemRepository carritoItemRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PedidoMapper pedidoMapper;

    @Autowired
    private StockService stockService;

    @Autowired
    private DireccionRepository direccionRepository;

    @Autowired
    private DireccionMapper direccionMapper;

    public PedidoResponseDTO confirmarDesdeCarrito(CheckoutRequestDTO request) {
        Usuario comprador = requireUsuarioAutenticado();

        String lineaEnvio = null;
        DireccionSnapshot snap = null;
        if (request != null && request.getDireccionId() != null) {
            Direccion dir = direccionRepository.findById(request.getDireccionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Direccion", request.getDireccionId()));
            if (!dir.getUsuario().getId().equals(comprador.getId())) {
                throw new AccessDeniedException("No podés usar una dirección de otro usuario.");
            }
            snap = direccionMapper.toSnapshot(dir);
            lineaEnvio = DireccionSnapshot.formatearUnaLinea(snap);
        } else if (request != null
                && request.getDireccionEnvio() != null
                && !request.getDireccionEnvio().isBlank()) {
            lineaEnvio = request.getDireccionEnvio().trim();
        }

        List<CarritoItem> carritoItems =
                carritoItemRepository.findByUsuarioIdOrderByIdAscWithProducto(comprador.getId());

        if (carritoItems.isEmpty()) {
            throw new BusinessRuleException(
                    "El carrito esta vacio. Agrega al menos un producto antes de confirmar el pedido.");
        }

        // El stock baja al confirmar el checkout, no al cambiar a PAGADO.
        // Primero tomamos snapshot del pedido y luego descontamos stock de forma atomica
        // dentro de esta misma transaccion para evitar overselling.
        List<PedidoItem> lineas = new ArrayList<>();
        double total = 0.0;

        for (CarritoItem ci : carritoItems) {
            if (ci.getProducto() == null) {
                throw new ResourceNotFoundException(
                        "Uno de los productos del carrito ya no existe en el catalogo.");
            }

            int cantidadPedida = ci.getCantidad();
            Integer stockPublicado = ci.getProducto().getStock();
            if (stockPublicado == null || stockPublicado < 0) {
                throw new BusinessRuleException(
                        String.format("El producto '%s' no tiene stock disponible.", ci.getProducto().getNombre()));
            }

            double precioSnapshot = ci.getProducto().getPrecio();
            double subtotal = precioSnapshot * cantidadPedida;
            total += subtotal;

            lineas.add(PedidoItem.builder()
                    .producto(ci.getProducto())
                    .cantidad(cantidadPedida)
                    .precioUnitario(precioSnapshot)
                    .subtotal(subtotal)
                    .build());
        }

        for (CarritoItem ci : carritoItems) {
            stockService.descontarStockParaCheckout(
                    ci.getProducto().getId(),
                    ci.getProducto().getNombre(),
                    ci.getCantidad());
        }

        Pedido pedido = Pedido.builder()
                .comprador(comprador)
                .fecha(LocalDateTime.now())
                .total(total)
                .estado(EstadoPedido.PENDIENTE_PAGO)
                .direccionEnvio(lineaEnvio)
                .direccionSnapshot(snap)
                .notas(request != null ? request.getNotas() : null)
                .items(new ArrayList<>())
                .build();

        for (PedidoItem item : lineas) {
            item.setPedido(pedido);
            pedido.getItems().add(item);
        }

        pedidoRepository.save(pedido);
        carritoItemRepository.deleteAll(carritoItems);

        return pedidoMapper.toDTO(pedido);
    }

    /**
     * Detalle de un pedido por id.
     * Solo el comprador del pedido o un ADMIN pueden verlo.
     */
    public PedidoResponseDTO obtenerPedido(Long pedidoId) {
        Usuario usuario = requireUsuarioAutenticado();
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido", pedidoId));

        if (!pedido.getComprador().getId().equals(usuario.getId()) && !esAdmin(usuario)) {
            throw new AccessDeniedException("No tenes permiso para ver este pedido.");
        }

        return pedidoMapper.toDTO(pedido);
    }

    /**
     * Historial de pedidos del usuario autenticado, del mas reciente al mas antiguo.
     */
    public List<PedidoResponseDTO> misPedidos() {
        Usuario usuario = requireUsuarioAutenticado();
        List<Pedido> pedidos =
                pedidoRepository.findByCompradorIdOrderByFechaDesc(usuario.getId());
        return pedidoMapper.toDTOList(pedidos);
    }

    /**
     * Historial paginado de compras del usuario autenticado.
     * Nunca acepta un compradorId externo: el filtro siempre sale del JWT.
     */
    public Page<PedidoSummaryResponseDTO> listarMisCompras(Pageable pageable) {
        Usuario usuario = requireUsuarioAutenticado();
        Page<Pedido> pedidos = pedidoRepository.findByCompradorId(
                usuario.getId(),
                normalizarPageable(pageable));
        return pedidoMapper.toSummaryPage(pedidos);
    }

    /**
     * Historial paginado de ventas del usuario autenticado.
     * Un pedido aparece si al menos una linea pertenece a una publicacion del vendedor.
     */
    public Page<PedidoSummaryResponseDTO> listarMisVentas(Pageable pageable) {
        Usuario usuario = requireUsuarioAutenticado();
        Page<Pedido> pedidos = pedidoRepository.findVentasByVendedorId(
                usuario.getId(),
                normalizarPageable(pageable));
        return pedidoMapper.toSummaryPage(pedidos);
    }

    /**
     * Cambia el estado de un pedido, respetando transiciones validas y los permisos por rol.
     *
     * Si la transicion no es valida -> BusinessRuleException -> 400.
     * Si el actor no tiene permiso -> AccessDeniedException -> 403.
     */
    public PedidoResponseDTO cambiarEstado(Long pedidoId, CambioEstadoRequestDTO request) {
        Usuario actor = requireUsuarioAutenticado();
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido", pedidoId));

        EstadoPedido nuevoEstado = request.getNuevoEstado();

        if (esAdmin(actor)) {
            pedido.transicionarA(nuevoEstado);
        } else if (pedido.getComprador().getId().equals(actor.getId())) {
            if (nuevoEstado != EstadoPedido.CANCELADO) {
                throw new AccessDeniedException(
                        "El comprador solo puede cancelar su pedido. Para otros cambios de estado contacta al vendedor.");
            }
            pedido.transicionarA(nuevoEstado);
        } else {
            throw new AccessDeniedException("No tenes permiso para cambiar el estado de este pedido.");
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

    private boolean esAdmin(Usuario usuario) {
        return Role.ADMIN.equals(usuario.getRole());
    }

    private Pageable normalizarPageable(Pageable pageable) {
        if (pageable == null) {
            return PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "fecha"));
        }

        if (pageable.getSort().isSorted()) {
            return pageable;
        }

        return PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "fecha"));
    }
}
