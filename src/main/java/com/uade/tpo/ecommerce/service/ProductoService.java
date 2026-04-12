package com.uade.tpo.ecommerce.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.uade.tpo.ecommerce.dto.producto.ProductoCreateRequestDTO;
import com.uade.tpo.ecommerce.dto.producto.ProductoResponseDTO;
import com.uade.tpo.ecommerce.dto.producto.ProductoUpdateRequestDTO;
import com.uade.tpo.ecommerce.exception.ArgumentInvalidException;
import com.uade.tpo.ecommerce.exception.ResourceNotFoundException;
import com.uade.tpo.ecommerce.model.Producto;
import com.uade.tpo.ecommerce.model.Usuario;
import com.uade.tpo.ecommerce.repository.ProductoRepository;
import com.uade.tpo.ecommerce.repository.UsuarioRepository;

import jakarta.transaction.Transactional;

/**
 * Reglas de producto alineadas al marketplace: el vendedor sale del JWT / {@link SecurityContextHolder}, no del body.
 * Las mutaciones (PUT/DELETE) comprueban dueño o rol {@code ROLE_ADMIN}; el catálogo GET sigue sin exigir login.
 */
@Service
@Transactional
public class ProductoService {

    private static final String RECURSO_PRODUCTO = "Producto";
    /** Coincide con {@link com.uade.tpo.ecommerce.model.Usuario#getAuthorities()} y el claim del JWT emitido en login. */
    private static final String ROLE_ADMIN = "ROLE_ADMIN";

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    public List<ProductoResponseDTO> getAllProductos() {
        // Catálogo público: igualmente traemos vendedor en una sola ida por el @EntityGraph del repositorio
        return productoRepository.findAllForCatalog().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public ProductoResponseDTO getProductoById(Long id) {
        // findDetailById incluye vendedor para armar vendedorId / vendedorNombre en el DTO.
        Producto producto = productoRepository.findDetailById(id)
                .orElseThrow(() -> new ResourceNotFoundException(RECURSO_PRODUCTO, id));
        return toResponse(producto);
    }

    public void deleteProductoById(Long id) {
        Producto producto = productoRepository.findDetailById(id)
                .orElseThrow(() -> new ResourceNotFoundException(RECURSO_PRODUCTO, id));
        // Ruta autenticada en SecurityConfig; resolvemos 403 si no es el publicador ni admin
        assertDueñoOAdmin(producto, requireUsuarioAutenticado());
        productoRepository.delete(producto);
    }

    public ProductoResponseDTO saveProducto(ProductoCreateRequestDTO request) {
        validarPrecioYStock(request.getPrecio(), request.getStock());
        // Nunca tomar vendedorId del JSON: el cliente podría falsificarlo; el dueño es quien trae el Bearer válido
        Usuario vendedor = requireUsuarioAutenticado();

        // Alta nueva: el vendedor queda grabado en la fila del producto.
        Producto producto = Producto.builder()
                .nombre(request.getNombre())
                .descripcion(request.getDescripcion())
                .precio(request.getPrecio())
                .stock(request.getStock())
                .vendedor(vendedor)
                .build();

        Producto guardado = productoRepository.save(producto);
        return toResponse(guardado);
    }

    public ProductoResponseDTO updateProducto(Long id, ProductoUpdateRequestDTO request) {
        validarPrecioYStock(request.getPrecio(), request.getStock());

        Producto existente = productoRepository.findDetailById(id)
                .orElseThrow(() -> new ResourceNotFoundException(RECURSO_PRODUCTO, id));
        // No se transfiere la titularidad por PUT: el vendedor_id existente se mantiene; solo dueño/admin editan otros campos
        assertDueñoOAdmin(existente, requireUsuarioAutenticado());

        // Solo datos de catálogo; el vendedor_id no se toca en este flujo.
        existente.setNombre(request.getNombre());
        existente.setDescripcion(request.getDescripcion());
        existente.setPrecio(request.getPrecio());
        existente.setStock(request.getStock());

        return toResponse(productoRepository.save(existente));
    }

    private void validarPrecioYStock(Double precio, Integer stock) {
        // Reglas extra además del @Valid del DTO (por si se llama desde otro lado).
        if (precio != null && precio <= 0) {
            throw new ArgumentInvalidException("El precio debe ser mayor que cero");
        }
        if (stock != null && stock < 0) {
            throw new ArgumentInvalidException("El stock no puede ser negativo");
        }
    }

    /**
     * Resuelve el {@link Usuario} persistido a partir del email que dejó el {@link com.uade.tpo.ecommerce.security.JwtFilter}
     * en el contexto (mismo subject que en el token).
     */
    private Usuario requireUsuarioAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        // AnonymousAuthenticationToken cuenta como “autenticado” en Spring; lo excluimos por si la cadena de filtros cambia
        if (auth == null
                || !auth.isAuthenticated()
                || auth instanceof AnonymousAuthenticationToken
                || auth.getName() == null) {
            throw new AccessDeniedException("Se requiere un usuario autenticado.");
        }
        // auth.getName() es el email (JwtFilter / login).
        return usuarioRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new AccessDeniedException("Usuario autenticado no encontrado."));
    }

    /** Autoridades del JWT en este proyecto incluyen el prefijo ROLE_ (ej. ROLE_ADMIN). */
    private boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return false;
        }
        // Comparación exacta con el string que viene en el token.
        return auth.getAuthorities().stream().anyMatch(a -> ROLE_ADMIN.equals(a.getAuthority()));
    }

    /**
     * Regla de autorización para PUT/DELETE: admin pasa siempre; si no hay vendedor en BD solo admin; si no, debe coincidir el id.
     */
    private void assertDueñoOAdmin(Producto producto, Usuario actor) {
        if (isAdmin()) {
            return; // Admin puede tocar cualquier publicación.
        }
        Usuario vendedor = producto.getVendedor();
        if (vendedor == null) {
            // Producto viejo sin vendedor en BD: no dejamos que un USER normal lo edite.
            throw new AccessDeniedException("Solo un administrador puede modificar este producto.");
        }
        if (!vendedor.getId().equals(actor.getId())) {
            throw new AccessDeniedException("Solo el vendedor que publicó el producto o un administrador puede realizar esta operación.");
        }
    }

    /** Arma el DTO de respuesta; campos de vendedor null si el producto es legado sin FK. */
    private ProductoResponseDTO toResponse(Producto producto) {
        Usuario v = producto.getVendedor();
        // No exponemos email del vendedor en el catálogo.
        return ProductoResponseDTO.builder()
                .id(producto.getId())
                .nombre(producto.getNombre())
                .descripcion(producto.getDescripcion())
                .precio(producto.getPrecio())
                .stock(producto.getStock())
                .vendedorId(v != null ? v.getId() : null)
                .vendedorNombre(v != null ? nombreVisibleVendedor(v) : null)
                .build();
    }

    /**
     * Evita filtrar el apellido completo en listados públicos; alcanza con reconocer al vendedor sin exponer email.
     */
    private static String nombreVisibleVendedor(Usuario u) {
        if (u.getNombre() != null && !u.getNombre().isBlank()) {
            if (u.getApellido() != null && !u.getApellido().isBlank()) {
                // Ej: "Ana G." en lugar del apellido completo.
                return u.getNombre().trim() + " " + u.getApellido().trim().charAt(0) + ".";
            }
            return u.getNombre().trim();
        }
        return "Vendedor";
    }
}
