package com.uade.tpo.ecommerce.service;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.uade.tpo.ecommerce.dto.usuario.PublicacionPerfilDTO;
import com.uade.tpo.ecommerce.dto.usuario.UsuarioPerfilResponseDTO;
import com.uade.tpo.ecommerce.dto.usuario.UsuarioPerfilUpdateRequestDTO;
import com.uade.tpo.ecommerce.exception.ArgumentInvalidException;
import com.uade.tpo.ecommerce.exception.ConflictException;
import com.uade.tpo.ecommerce.model.Producto;
import com.uade.tpo.ecommerce.model.Usuario;
import com.uade.tpo.ecommerce.repository.ProductoRepository;
import com.uade.tpo.ecommerce.repository.UsuarioRepository;

import jakarta.transaction.Transactional;

// Perfil del usuario logueado: lee/actualiza por email del JWT; no permite tocar otros usuarios.
@Service
@Transactional
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ProductoRepository productoRepository;

    public UsuarioPerfilResponseDTO obtenerPerfilActual() {
        return toPerfilDto(requireUsuarioAutenticado());
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
        return UsuarioPerfilResponseDTO.builder()
                .id(u.getId())
                .nombre(u.getNombre())
                .apellido(u.getApellido())
                .email(u.getEmail())
                .fechaNacimiento(u.getFechaNacimiento())
                .sexo(u.getSexo())
                .role(u.getRole())
                .publicaciones(publicaciones)
                .compras(Collections.emptyList())
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
}
