package com.uade.tpo.ecommerce.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.uade.tpo.ecommerce.dto.direccion.DireccionRequestDTO;
import com.uade.tpo.ecommerce.dto.direccion.DireccionResponseDTO;
import com.uade.tpo.ecommerce.exception.ResourceNotFoundException;
import com.uade.tpo.ecommerce.model.Direccion;
import com.uade.tpo.ecommerce.model.Usuario;
import com.uade.tpo.ecommerce.repository.DireccionRepository;
import com.uade.tpo.ecommerce.repository.UsuarioRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class DireccionService {

    private final DireccionRepository direccionRepository;
    private final UsuarioRepository usuarioRepository;
    private final DireccionMapper direccionMapper;

    public List<DireccionResponseDTO> listarPropias() {
        Usuario u = requireUsuarioAutenticado();
        return direccionRepository.findByUsuarioIdOrderByPrincipalDescIdAsc(u.getId()).stream()
                .map(direccionMapper::toResponse)
                .collect(Collectors.toList());
    }

    public DireccionResponseDTO obtener(Long id) {
        Usuario u = requireUsuarioAutenticado();
        Direccion d = direccionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Direccion", id));
        assertPropietario(d, u.getId());
        return direccionMapper.toResponse(d);
    }

    public DireccionResponseDTO crear(DireccionRequestDTO dto) {
        Usuario u = requireUsuarioAutenticado();
        Direccion d = Direccion.builder()
                .usuario(u)
                .principal(false)
                .build();
        direccionMapper.aplicar(dto, d);
        if (dto.getPrincipal() == null) {
            d.setPrincipal(false);
        }
        direccionRepository.save(d);
        if (d.isPrincipal()) {
            quitarPrincipalOtros(u.getId(), d.getId());
        }
        return direccionMapper.toResponse(d);
    }

    public DireccionResponseDTO actualizar(Long id, DireccionRequestDTO dto) {
        Usuario u = requireUsuarioAutenticado();
        Direccion d = direccionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Direccion", id));
        assertPropietario(d, u.getId());
        direccionMapper.aplicar(dto, d);
        direccionRepository.save(d);
        if (d.isPrincipal()) {
            quitarPrincipalOtros(u.getId(), d.getId());
        }
        return direccionMapper.toResponse(d);
    }

    public void eliminar(Long id) {
        Usuario u = requireUsuarioAutenticado();
        Direccion d = direccionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Direccion", id));
        assertPropietario(d, u.getId());
        direccionRepository.delete(d);
    }

    private void quitarPrincipalOtros(Long usuarioId, Long mantenerId) {
        List<Direccion> todas = direccionRepository.findByUsuarioIdOrderByPrincipalDescIdAsc(usuarioId);
        for (Direccion otra : todas) {
            if (!otra.getId().equals(mantenerId) && otra.isPrincipal()) {
                otra.setPrincipal(false);
                direccionRepository.save(otra);
            }
        }
    }

    private void assertPropietario(Direccion d, Long usuarioId) {
        if (!d.getUsuario().getId().equals(usuarioId)) {
            throw new AccessDeniedException("No tenés permiso sobre esta dirección.");
        }
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
