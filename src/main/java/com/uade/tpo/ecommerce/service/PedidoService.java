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
import com.uade.tpo.ecommerce.model.EstadoPedido;
import com.uade.tpo.ecommerce.model.Pedido;
import com.uade.tpo.ecommerce.model.PedidoItem;
import com.uade.tpo.ecommerce.model.Role;
import com.uade.tpo.ecommerce.model.Usuario;
import com.uade.tpo.ecommerce.repository.CarritoItemRepository;
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


    
    public PedidoResponseDTO confirmarDesdeCarrito(CheckoutRequestDTO request) {
        Usuario comprador = requireUsuarioAutenticado();

        //items actuales del carrito
        List<CarritoItem> carritoItems =
                carritoItemRepository.findByUsuarioIdOrderByIdAscWithProducto(comprador.getId());

        //Carrito vacío → 400 
        if (carritoItems.isEmpty()) {
            throw new BusinessRuleException(
                    "El carrito está vacío. Agregá al menos un producto antes de confirmar el pedido.");
        }

        //Construir líneas con snapshot de precio + validación de stock.
        List<PedidoItem> lineas = new ArrayList<>();
        double total = 0.0;

        for (CarritoItem ci : carritoItems) {
            if (ci.getProducto() == null) {
                throw new ResourceNotFoundException(
                        "Uno de los productos del carrito ya no existe en el catálogo.");
            }

            int cantidadPedida = ci.getCantidad();
            int stockActual = ci.getProducto().getStock() != null ? ci.getProducto().getStock() : 0;

            // Validación de stock: mismo criterio que CarritoService.
            if (cantidadPedida > stockActual) {
                throw new BusinessRuleException(
                        String.format("Stock insuficiente para '%s': hay %d disponibles y se pidieron %d.",
                                ci.getProducto().getNombre(), stockActual, cantidadPedida));
            }

            // Snapshot del precio: copiamos el valor actual del producto.
            double precioSnapshot = ci.getProducto().getPrecio();
            double subtotal = precioSnapshot * cantidadPedida;
            total += subtotal;

            lineas.add(PedidoItem.builder()
                    .producto(ci.getProducto())
                    .cantidad(cantidadPedida)
                    .precioUnitario(precioSnapshot) // ← snapshot guardado acá
                    .subtotal(subtotal)
                    .build());
        }

        //Crear el pedido (sin id aún; cascade persiste los ítems junto con él)
        Pedido pedido = Pedido.builder()
                .comprador(comprador)
                .fecha(LocalDateTime.now())
                .total(total)
                .estado(EstadoPedido.PENDIENTE_PAGO)
                .direccionEnvio(request != null ? request.getDireccionEnvio() : null)
                .notas(request != null ? request.getNotas() : null)
                .items(new ArrayList<>())
                .build();

        // Asignamos la FK bidireccional a cada línea antes de persistir
        for (PedidoItem item : lineas) {
            item.setPedido(pedido);
            pedido.getItems().add(item);
        }

        //Persistir (cascade ALL guarda los PedidoItems automáticamente)
        pedidoRepository.save(pedido);

        //Vaciar el carrito.
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
            throw new AccessDeniedException("No tenés permiso para ver este pedido.");
        }

        return pedidoMapper.toDTO(pedido);
    }

    /**
     * Historial de pedidos del usuario autenticado, del más reciente al más antiguo.
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
     * Un pedido aparece si al menos una línea pertenece a una publicación del vendedor.
     */
    public Page<PedidoSummaryResponseDTO> listarMisVentas(Pageable pageable) {
        Usuario usuario = requireUsuarioAutenticado();
        Page<Pedido> pedidos = pedidoRepository.findVentasByVendedorId(
                usuario.getId(),
                normalizarPageable(pageable));
        return pedidoMapper.toSummaryPage(pedidos);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // CAMBIO DE ESTADO
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Cambia el estado de un pedido, respetando transiciones válidas y los permisos por rol.
     *
     * Si la transición no es válida → BusinessRuleException → 400.
     * Si el actor no tiene permiso  → AccessDeniedException → 403.
     */
    public PedidoResponseDTO cambiarEstado(Long pedidoId, CambioEstadoRequestDTO request) {
        Usuario actor = requireUsuarioAutenticado();
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido", pedidoId));

        EstadoPedido nuevoEstado = request.getNuevoEstado();

        if (esAdmin(actor)) {
            // El admin puede aplicar cualquier transición válida.
            pedido.transicionarA(nuevoEstado);

        } else if (pedido.getComprador().getId().equals(actor.getId())) {
            // El comprador solo puede cancelar su propio pedido.
            if (nuevoEstado != EstadoPedido.CANCELADO) {
                throw new AccessDeniedException(
                        "El comprador solo puede cancelar su pedido. Para otros cambios de estado contactá al vendedor.");
            }
            pedido.transicionarA(nuevoEstado);

        } else {
            throw new AccessDeniedException("No tenés permiso para cambiar el estado de este pedido.");
        }

        pedidoRepository.save(pedido);
        return pedidoMapper.toDTO(pedido);
    }

    
    // El usuario siempre sale del JWT, no del body.
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

        // Si el cliente no manda sort, aplicamos fecha desc para que el historial
        // salga de más reciente a más antiguo.
        return PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "fecha"));
    }
}
