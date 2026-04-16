package com.uade.tpo.ecommerce.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.uade.tpo.ecommerce.dto.carrito.CarritoItemAddRequestDTO;
import com.uade.tpo.ecommerce.dto.carrito.CarritoItemUpdateRequestDTO;
import com.uade.tpo.ecommerce.dto.carrito.CarritoLineResponseDTO;
import com.uade.tpo.ecommerce.exception.BusinessRuleException;
import com.uade.tpo.ecommerce.exception.ResourceNotFoundException;
import com.uade.tpo.ecommerce.model.CarritoItem;
import com.uade.tpo.ecommerce.model.Producto;
import com.uade.tpo.ecommerce.model.Usuario;
import com.uade.tpo.ecommerce.repository.CarritoItemRepository;
import com.uade.tpo.ecommerce.repository.ProductoRepository;
import com.uade.tpo.ecommerce.repository.UsuarioRepository;

import jakarta.transaction.Transactional;

// Carrito del usuario logueado: el user sale del JWT (como en ProductoService), no del body.
@Service
@Transactional
public class CarritoService {

    private static final String RECURSO_PRODUCTO = "Producto";
    private static final String RECURSO_LINEA = "Ítem del carrito";

    @Autowired
    private CarritoItemRepository carritoItemRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    // Devuelve las líneas del carrito del usuario, con datos del producto cargados de una vez.
    public List<CarritoLineResponseDTO> listarCarrito() {
        Usuario u = requireUsuarioAutenticado();
        // find...WithProducto trae cada producto en la misma consulta (ver repo).
        return carritoItemRepository.findByUsuarioIdOrderByIdAscWithProducto(u.getId()).stream()
                .map(this::toLineDto)
                .collect(Collectors.toList());
    }

    // Suma cantidad. Si ya había ese producto en el carrito, suma encima. No puede pasar del stock.
    public List<CarritoLineResponseDTO> agregar(CarritoItemAddRequestDTO request) {
        Usuario usuario = requireUsuarioAutenticado();
        // El producto tiene que existir; el id viene del body pero el carrito es siempre del usuario del token.
        Producto producto = productoRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException(RECURSO_PRODUCTO, request.getProductId()));

        // Chequeo con la cantidad que quiere sumar ahora (si ya hay línea, volvemos a chequear abajo con el total).
        validarStockDisponible(producto, request.getQuantity());

        // Buscamos si ya tenía ese producto en el carrito (por el índice único usuario+producto).
        CarritoItem linea = carritoItemRepository
                .findByUsuarioIdAndProductoId(usuario.getId(), producto.getId())
                .orElse(null);

        if (linea == null) {
            // Primera vez que agrega ese producto: nueva fila.
            linea = CarritoItem.builder()
                    .usuario(usuario)
                    .producto(producto)
                    .cantidad(request.getQuantity())
                    .build();
        } else {
            // Ya había línea: sumamos y volvemos a chequear que no pase del stock.
            int nueva = linea.getCantidad() + request.getQuantity();
            validarStockDisponible(producto, nueva);
            linea.setCantidad(nueva);
        }

        carritoItemRepository.save(linea);
        // Devolvemos el carrito entero para que el front refresque sin otro GET.
        return listarCarrito();
    }

    // Pone la cantidad exacta. Si mandás 0, borra esa línea del carrito.
    public List<CarritoLineResponseDTO> actualizarCantidad(CarritoItemUpdateRequestDTO request) {
        Usuario usuario = requireUsuarioAutenticado();

        // Caso especial: cantidad 0 = sacar el producto del carrito (si existía la línea).
        if (request.getQuantity() == 0) {
            carritoItemRepository.findByUsuarioIdAndProductoId(usuario.getId(), request.getProductId())
                    .ifPresent(carritoItemRepository::delete);
            // Si no había línea, no pasa nada malo: devolvemos el carrito como está.
            return listarCarrito();
        }

        // Cargamos el producto para conocer el stock actual (pudo cambiar desde el último GET).
        Producto producto = productoRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException(RECURSO_PRODUCTO, request.getProductId()));

        // No permitimos cantidad mayor al stock publicado.
        validarStockDisponible(producto, request.getQuantity());

        // Tiene que existir una línea de este usuario para ese producto; si no, no hay nada que “actualizar”.
        CarritoItem linea = carritoItemRepository
                .findByUsuarioIdAndProductoId(usuario.getId(), producto.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No hay ninguna línea en el carrito para el producto con id: " + request.getProductId()));

        linea.setCantidad(request.getQuantity());
        carritoItemRepository.save(linea);
        // Igual que en agregar: respondemos la lista completa del carrito.
        return listarCarrito();
    }

    // Borra una línea por id. Solo si esa línea es de este usuario.
    public List<CarritoLineResponseDTO> eliminarLinea(Long lineaId) {
        Usuario usuario = requireUsuarioAutenticado();
        // findByIdAndUsuarioId evita que un usuario borre líneas de otro aunque adivine el id.
        CarritoItem linea = carritoItemRepository.findByIdAndUsuarioId(lineaId, usuario.getId())
                .orElseThrow(() -> new ResourceNotFoundException(RECURSO_LINEA, lineaId));
        carritoItemRepository.delete(linea);
        return listarCarrito();
    }

    // No dejamos pedir más unidades de las que hay en stock (el stock no se baja acá, solo al pagar/comprar).
    private static void validarStockDisponible(Producto producto, int cantidadPedida) {
        // Datos raros en BD: tratamos como sin stock vendible.
        if (producto.getStock() == null || producto.getStock() < 0) {
            throw new BusinessRuleException("El producto no tiene stock disponible.");
        }
        // Mensaje claro para el front (ej. usuario pidió 5 y hay 2).
        if (cantidadPedida > producto.getStock()) {
            throw new BusinessRuleException(
                    String.format("Stock insuficiente: hay %d unidades disponibles y se pidieron %d.",
                            producto.getStock(), cantidadPedida));
        }
    }

    // Pasa de entidad JPA a DTO para no exponer el grafo completo en la API.
    private CarritoLineResponseDTO toLineDto(CarritoItem item) {
        Producto p = item.getProducto();
        return CarritoLineResponseDTO.builder()
                .id(item.getId())
                .productId(p.getId())
                .nombreProducto(p.getNombre())
                .cantidad(item.getCantidad())
                .precioActual(p.getPrecio())
                .stockDisponible(p.getStock())
                .build();
    }

    // Usuario logueado: email del token → buscamos en la base.
    private Usuario requireUsuarioAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        // Sin token válido o usuario anónimo: no seguimos (las rutas /api/cart ya piden JWT, esto es defensa extra).
        if (auth == null
                || !auth.isAuthenticated()
                || auth instanceof AnonymousAuthenticationToken
                || auth.getName() == null) {
            throw new AccessDeniedException("Se requiere un usuario autenticado.");
        }
        // El JwtFilter guarda el email en el contexto; tiene que existir en usuarios.
        return usuarioRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new AccessDeniedException("Usuario autenticado no encontrado."));
    }
}
