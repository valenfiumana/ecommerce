package com.uade.tpo.ecommerce.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.uade.tpo.ecommerce.dto.favorito.FavoritoRequestDTO;
import com.uade.tpo.ecommerce.dto.favorito.FavoritoResponseDTO;
import com.uade.tpo.ecommerce.exception.ConflictException;
import com.uade.tpo.ecommerce.exception.ResourceNotFoundException;
import com.uade.tpo.ecommerce.model.Favorito;
import com.uade.tpo.ecommerce.model.Producto;
import com.uade.tpo.ecommerce.model.Usuario;
import com.uade.tpo.ecommerce.repository.FavoritoRepository;
import com.uade.tpo.ecommerce.repository.ProductoRepository;
import com.uade.tpo.ecommerce.repository.UsuarioRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class FavoritoService {

    @Autowired
    private FavoritoRepository favoritoRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private FavoritoMapper favoritoMapper;

    // GET lista los favoritos del usuario autenticado
    public List<FavoritoResponseDTO> listarMisFavoritos() {
        Usuario usuario = requireUsuarioAutenticado();
        List<Favorito> favoritos = favoritoRepository.findByUsuarioIdOrderByIdDesc(usuario.getId());
        return favoritoMapper.toDTOList(favoritos);
    }

    // GET obtener un favorito por id
    public FavoritoResponseDTO obtenerPorId(Long id) {
        Usuario usuario = requireUsuarioAutenticado();
        Favorito favorito = favoritoRepository.findByIdAndUsuarioId(id, usuario.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Favorito", id));
        return favoritoMapper.toDTO(favorito);
    }

    // GET favoritos de un usuario por id
    public List<FavoritoResponseDTO> listarPorUsuario(Long userId) {
        Usuario autenticado = requireUsuarioAutenticado();

        // Solo puede ver los propios, o si es admin
        if (!autenticado.getId().equals(userId)
                && autenticado.getAuthorities().stream()
                        .noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            throw new AccessDeniedException("No podés ver los favoritos de otro usuario.");
        }

        // Verificar que el usuario objetivo existe
        if (!usuarioRepository.existsById(userId)) {
            throw new ResourceNotFoundException("Usuario", userId);
        }

        List<Favorito> favoritos = favoritoRepository.findByUsuarioIdOrderByIdDesc(userId);
        return favoritoMapper.toDTOList(favoritos);
    }

    // POST gregar favorito
    public FavoritoResponseDTO agregar(FavoritoRequestDTO request) {
        Usuario usuario = requireUsuarioAutenticado();

        Producto producto = productoRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Producto", request.getProductId()));

        // Chequear duplicado
        if (favoritoRepository.findByUsuarioIdAndProductoId(usuario.getId(), producto.getId()).isPresent()) {
            throw new ConflictException("El producto ya está en tus favoritos.");
        }

        Favorito favorito = Favorito.builder()
                .usuario(usuario)
                .producto(producto)
                .build();

        favorito = favoritoRepository.save(favorito);
        return favoritoMapper.toDTO(favorito);
    }

    // DELETE quitar favorito
    public void eliminar(Long id) {
        Usuario usuario = requireUsuarioAutenticado();
        Favorito favorito = favoritoRepository.findByIdAndUsuarioId(id, usuario.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Favorito", id));
        favoritoRepository.delete(favorito);
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