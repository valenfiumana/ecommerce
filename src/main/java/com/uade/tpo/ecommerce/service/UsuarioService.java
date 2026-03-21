package com.uade.tpo.ecommerce.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.uade.tpo.ecommerce.model.Usuario;
import com.uade.tpo.ecommerce.repository.UsuarioRepository;

import jakarta.transaction.Transactional;

// @Service: marca esta clase como parte de la "capa de servicio". Spring la crea solo, la guarda
// en un contenedor y puede pasársela al controller cuando haga falta (inyección de dependencias).
@Service
// @Transactional: cada vez que llamás a un método público de esta clase, Spring abre una transacción
// con la base antes de entrar y la cierra al salir. Si hubo error, deshace los cambios (rollback);
// si todo salió bien, los confirma (commit). Así no quedás a medias si algo falla a la mitad.
@Transactional
public class UsuarioService {

    // @Autowired: Spring busca un bean que sea UsuarioRepository (ya existe porque extiende JpaRepository)
    // y te lo asigna a este campo. No hacés "new UsuarioRepository()" porque Spring ya lo creó.
    @Autowired
    private UsuarioRepository usuarioRepository;

    public List<Usuario> getAllUsuarios() {
        return usuarioRepository.findAll();
    }

    public Usuario getUsuarioById(Long id) {
        return usuarioRepository.findById(id).orElse(null);
    }

    public void deleteUsuarioById(Long id) {
        usuarioRepository.deleteById(id);
    }

    public Usuario saveUsuario(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }

    public List<Usuario> saveAllUsuarios(List<Usuario> usuarios) {
        return usuarioRepository.saveAll(usuarios);
    }

    public Usuario updateUsuario(Long id, Usuario usuario) {
        Usuario existing = getUsuarioById(id);
        if (existing != null) {
            existing.setNombre(usuario.getNombre());
            existing.setEmail(usuario.getEmail());
            return usuarioRepository.save(existing);
        }
        return null;
    }
}
