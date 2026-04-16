package com.uade.tpo.ecommerce.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.uade.tpo.ecommerce.dto.Resena.ResenaRequestDTO;
import com.uade.tpo.ecommerce.dto.Resena.ResenaResponseDTO;
import com.uade.tpo.ecommerce.dto.Resena.VendedorResumenDTO;
import com.uade.tpo.ecommerce.exception.BusinessRuleException;
import com.uade.tpo.ecommerce.exception.ConflictException;
import com.uade.tpo.ecommerce.exception.ResourceNotFoundException;
import com.uade.tpo.ecommerce.model.EstadoPedido;
import com.uade.tpo.ecommerce.model.PedidoItem;
import com.uade.tpo.ecommerce.model.Resena;
import com.uade.tpo.ecommerce.model.Usuario;
import com.uade.tpo.ecommerce.repository.PedidoItemRepository;
import com.uade.tpo.ecommerce.repository.ResenaRepository;
import com.uade.tpo.ecommerce.repository.UsuarioRepository;

import jakarta.transaction.Transactional;

/**
 * Servicio que maneja toda la lógica de negocio para las reseñas.
 * 
 * Responsabilidades:
 * - Validar que solo se puedan hacer reseñas si el pedido está ENTREGADO
 * - Validar que el usuario que reseña fue quien compró el producto
 * - Evitar duplicados (una reseña por pedido_item)
 * - Calcular resumen de vendedores (promedio + cantidad)
 */
@Service
@Transactional
public class ResenaService {

    @Autowired
    private ResenaRepository resenaRepository;

    @Autowired
    private PedidoItemRepository pedidoItemRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ResenaMapper resenaMapper;

    private static final String RECURSO_PEDIDO_ITEM = "PedidoItem";
    private static final String RECURSO_VENDEDOR = "Vendedor";

    /**
     * Crear una nueva reseña para un producto específico de un pedido.
     * 
     * Validaciones:
     * 1. El pedido_item existe
     * 2. El pedido del item está en estado ENTREGADO
     * 3. El usuario autenticado es el comprador del pedido
     * 4. No existe una reseña previa para este pedido_item (evita duplicados)
     * 
     * @param pedidoItemId ID del PedidoItem a reseñar
     * @param requestDTO contiene puntuación y comentario
     * @return la reseña creada convertida a DTO
     * @throws ResourceNotFoundException si el pedido_item no existe
     * @throws BusinessRuleException si el pedido no está ENTREGADO o el usuario no es comprador
     * @throws ConflictException si ya existe una reseña para este item
     */
    public ResenaResponseDTO crearResena(Long pedidoItemId, ResenaRequestDTO requestDTO) {
        // 1️⃣ Obtener el PedidoItem (con su relación al Pedido)
        PedidoItem pedidoItem = pedidoItemRepository.findById(pedidoItemId)
                .orElseThrow(() -> new ResourceNotFoundException(RECURSO_PEDIDO_ITEM, pedidoItemId));

        // 2️⃣ Obtener el usuario autenticado (quien intenta hacer la reseña)
        Usuario compradorAutenticado = requireUsuarioAutenticado();

        // 3️⃣ Validar que el usuario autenticado es el comprador del pedido
        if (!pedidoItem.getPedido().getComprador().getId().equals(compradorAutenticado.getId())) {
            throw new BusinessRuleException(
                    "No puedes reseñar un producto que no compraste. Solo el comprador puede reseñar.");
        }

        // 4️⃣ Validar que el pedido está en estado ENTREGADO
        if (pedidoItem.getPedido().getEstado() != EstadoPedido.ENTREGADO) {
            throw new BusinessRuleException(
                    "El pedido debe estar ENTREGADO para poder reseñar. Estado actual: " + 
                    pedidoItem.getPedido().getEstado());
        }

        // 5️⃣ Validar que no exista una reseña previa (prevenir duplicados)
        if (resenaRepository.findByCompradorIdAndPedidoItemId(
                compradorAutenticado.getId(), pedidoItemId).isPresent()) {
            throw new ConflictException(
                    "Ya has reseñado este producto. No puedes dejar más de una reseña por compra.");
        }

        // 6️⃣ Crear la nueva reseña
        Resena resena = Resena.builder()
                .comprador(compradorAutenticado)
                .pedidoItem(pedidoItem)
                .puntuacion(requestDTO.getPuntuacion())
                .comentario(requestDTO.getComentario())
                .fecha(LocalDateTime.now())  // Se crea con la fecha/hora actual del servidor
                .build();

        // 7️⃣ Guardar en BD
        Resena resenaSaved = resenaRepository.save(resena);

        // 8️⃣ Convertir a DTO y retornar
        return resenaMapper.toResponseDTO(resenaSaved);
    }

    /**
     * Obtener todas las reseñas de un producto específico.
     * 
     * Se navega por la relación: Resena → PedidoItem → Producto
     * para filtrar todas las reseñas que corresponden a líneas de ese producto.
     * 
     * Endpoint público: no requiere autenticación.
     * 
     * @param productoId ID del producto
     * @return lista de reseñas ordenadas (usualmente por fecha reciente primero)
     */
    public List<ResenaResponseDTO> obtenerReseniasProducto(Long productoId) {
        List<Resena> resenas = resenaRepository.findByPedidoItem_Producto_Id(productoId);
        return resenaMapper.toResponseDTOList(resenas);
    }

    /**
     * Obtener el resumen de reputación de un vendedor.
     * 
     * El resumen incluye:
     * - Promedio de calificación (ej: 4.5 estrellas)
     * - Cantidad total de reseñas
     * - Cantidad total de ventas (pedido_items)
     * 
     * Endpoint público: cualquiera puede verlo.
     * 
     * @param vendedorId ID del usuario vendedor
     * @return resumen con estadísticas del vendedor
     * @throws ResourceNotFoundException si el vendedor no existe
     */
    public VendedorResumenDTO obtenerResumenVendedor(Long vendedorId) {
        usuarioRepository.findById(vendedorId)
                .orElseThrow(() -> new ResourceNotFoundException(RECURSO_VENDEDOR, vendedorId));

        Double promedio = resenaRepository.findPromedioCalificacionVendedor(vendedorId);
        Long cantidadResenas = resenaRepository.countReseniasVendedor(vendedorId);
        Long cantidadVentas = pedidoItemRepository.countByProductoVendedorId(vendedorId);

        return resenaMapper.toVendedorResumen(promedio, cantidadResenas, cantidadVentas);
    }

    /**
     * Obtiene el usuario autenticado del contexto de Spring Security.
     * 
     * El usuario viene del JWT: Spring Security lo extrae del token y lo pone
     * en SecurityContextHolder. Aquí lo extraemos.
     * 
     * @return Usuario actual autenticado
     * @throws AccessDeniedException si no hay usuario autenticado o es anónimo
     */
    private Usuario requireUsuarioAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // Si no hay autenticación o es anónima, lanzar excepción
        if (auth == null || auth instanceof AnonymousAuthenticationToken) {
            throw new AccessDeniedException("Debes estar autenticado para reseñar");
        }

        // El name en la autenticación es el email del usuario (lo seteamos así en AuthenticationService)
        String email = auth.getName();

        // Buscar el usuario en BD por email
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", email));
    }
}