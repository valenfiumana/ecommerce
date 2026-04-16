package com.uade.tpo.ecommerce.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.uade.tpo.ecommerce.dto.usuario.CompraPerfilDTO;
import com.uade.tpo.ecommerce.dto.usuario.PublicacionPerfilDTO;
import com.uade.tpo.ecommerce.dto.usuario.UsuarioAdminListDTO;
import com.uade.tpo.ecommerce.dto.usuario.UsuarioPerfilResponseDTO;
import com.uade.tpo.ecommerce.dto.usuario.UsuarioPerfilUpdateRequestDTO;
import com.uade.tpo.ecommerce.dto.usuario.UsuarioPublicoResponseDTO;
import com.uade.tpo.ecommerce.exception.ArgumentInvalidException;
import com.uade.tpo.ecommerce.exception.ConflictException;
import com.uade.tpo.ecommerce.exception.ResourceNotFoundException;
import com.uade.tpo.ecommerce.model.Pedido;
import com.uade.tpo.ecommerce.model.Producto;
import com.uade.tpo.ecommerce.model.Role;
import com.uade.tpo.ecommerce.model.Usuario;
import com.uade.tpo.ecommerce.repository.PedidoRepository;
import com.uade.tpo.ecommerce.repository.ProductoRepository;
import com.uade.tpo.ecommerce.repository.UsuarioRepository;

import jakarta.transaction.Transactional;

// Perfil del usuario logueado: lee/actualiza por email del JWT.
@Service
@Transactional
public class UsuarioService {

    private static final int PERFIL_MAX_PEDIDOS = 50;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private PedidoRepository pedidoRepository;

    public UsuarioPerfilResponseDTO obtenerPerfilActual() {
        return toPerfilDto(requireUsuarioAutenticado());
    }

    /**
     * Listado global de usuarios: solo rol {@link Role#ADMIN}.
     */
    public List<UsuarioAdminListDTO> listarUsuariosParaAdmin() {
        requireAdmin();
        return usuarioRepository.findAll(Sort.by(Sort.Direction.ASC, "id")).stream()
                .map(UsuarioService::toAdminListDto)
                .toList();
    }

    /**
     * Detalle por id: perfil completo (como {@code /me}) si sos el mismo usuario o admin;
     * si no, vista pública sin email ni pedidos/publicaciones.
     */
    public Object obtenerUsuarioPorId(Long id) {
        Usuario actor = requireUsuarioAutenticado();
        Usuario target = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", id));

        if (actor.getId().equals(target.getId()) || Role.ADMIN.equals(actor.getRole())) {
            return toPerfilDto(target);
        }

        return UsuarioPublicoResponseDTO.builder()
                .id(target.getId())
                .nombre(target.getNombre())
                .apellido(target.getApellido())
                .build();
    }

    /**
     * Aplica solo los campos no nulos del DTO. Si cambia el email, valida que no exista otro usuario con ese email.
     */
    public UsuarioPerfilResponseDTO actualizarPerfil(UsuarioPerfilUpdateRequestDTO dto) {
        Usuario u = requireUsuarioAutenticado();

        boolean hayCambios = dto.getNombre() != null
                || dto.getApellido() != null
                || dto.getEmail() != null
                || dto.getFechaNacimiento() != null
                || dto.getSexo() != null;
        if (!hayCambios) {
            return toPerfilDto(u);
        }

        if (dto.getNombre() != null) {
            u.setNombre(dto.getNombre().trim());
        }
        if (dto.getApellido() != null) {
            u.setApellido(dto.getApellido().trim());
        }
        if (dto.getFechaNacimiento() != null) {
            u.setFechaNacimiento(dto.getFechaNacimiento());
        }
        if (dto.getSexo() != null) {
            u.setSexo(dto.getSexo());
        }

        if (dto.getEmail() != null) {
            String nuevo = dto.getEmail().trim();
            if (nuevo.isEmpty()) {
                throw new ArgumentInvalidException("El email no puede estar vacío");
            }
            if (!nuevo.equalsIgnoreCase(u.getEmail())
                    && usuarioRepository.existsByEmail(nuevo)) {
                throw new ConflictException("El email ya está registrado");
            }
            u.setEmail(nuevo);
        }

        return toPerfilDto(usuarioRepository.save(u));
    }

    private UsuarioPerfilResponseDTO toPerfilDto(Usuario u) {
        List<PublicacionPerfilDTO> publicaciones = productoRepository
                .findByVendedor_IdOrderByIdDesc(u.getId())
                .stream()
                .map(UsuarioService::toPublicacionPerfil)
                .toList();
        List<CompraPerfilDTO> compras = pedidoRepository
                .findByCompradorIdOrderByFechaDesc(u.getId())
                .stream()
                .map(UsuarioService::toCompraPerfil)
                .toList();
        var pageVentas = PageRequest.of(0, PERFIL_MAX_PEDIDOS, Sort.by(Sort.Direction.DESC, "fecha"));
        List<CompraPerfilDTO> ventas = pedidoRepository
                .findVentasByVendedorId(u.getId(), pageVentas)
                .getContent()
                .stream()
                .map(UsuarioService::toCompraPerfil)
                .toList();
        return UsuarioPerfilResponseDTO.builder()
                .id(u.getId())
                .nombre(u.getNombre())
                .apellido(u.getApellido())
                .email(u.getEmail())
                .fechaNacimiento(u.getFechaNacimiento())
                .sexo(u.getSexo())
                .role(u.getRole())
                .publicaciones(publicaciones)
                .compras(compras)
                .ventas(ventas)
                .build();
    }

    private static UsuarioAdminListDTO toAdminListDto(Usuario u) {
        return UsuarioAdminListDTO.builder()
                .id(u.getId())
                .nombre(u.getNombre())
                .apellido(u.getApellido())
                .email(u.getEmail())
                .role(u.getRole())
                .build();
    }

    private static PublicacionPerfilDTO toPublicacionPerfil(Producto p) {
        return PublicacionPerfilDTO.builder()
                .id(p.getId())
                .nombre(p.getNombre())
                .precio(p.getPrecio())
                .stock(p.getStock())
                .build();
    }

    private static CompraPerfilDTO toCompraPerfil(Pedido p) {
        return CompraPerfilDTO.builder()
                .id(p.getId())
                .fecha(p.getFecha())
                .total(p.getTotal())
                .estado(p.getEstado())
                .build();
    }

    // Mismo criterio que CarritoService / ProductoService: actor = email del JWT en BD.
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

    private void requireAdmin() {
        Usuario u = requireUsuarioAutenticado();
        if (!Role.ADMIN.equals(u.getRole())) {
            throw new AccessDeniedException("Solo administradores pueden listar usuarios.");
        }
    }
}
