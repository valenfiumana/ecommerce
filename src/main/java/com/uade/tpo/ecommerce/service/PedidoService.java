package com.uade.tpo.ecommerce.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.uade.tpo.ecommerce.model.EstadoPedido;
import com.uade.tpo.ecommerce.model.Pedido;
import com.uade.tpo.ecommerce.model.Usuario;
import com.uade.tpo.ecommerce.repository.PedidoRepository;
import com.uade.tpo.ecommerce.repository.UsuarioRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class PedidoService {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    public List<Pedido> getAllPedidos() {
        return pedidoRepository.findAll();
    }

    public Pedido getPedidoById(Long id) {
        return pedidoRepository.findById(id).orElse(null);
    }

    public List<Pedido> getPedidosByUsuarioId(Long usuarioId) {
        return pedidoRepository.findByUsuarioId(usuarioId);
    }

    public void deletePedidoById(Long id) {
        pedidoRepository.deleteById(id);
    }

    public Pedido savePedido(Pedido pedido) {
        Pedido listo = prepararPedido(pedido);
        if (listo == null) {
            return null;
        }
        return pedidoRepository.save(listo);
    }

    // Si algún pedido no es válido (usuario inexistente, etc.) → null y no se guarda ninguno.
    public List<Pedido> saveAllPedidos(List<Pedido> pedidos) {
        if (pedidos == null) {
            return null;
        }
        if (pedidos.isEmpty()) {
            return new ArrayList<>();
        }
        for (Pedido p : pedidos) {
            if (prepararPedido(p) == null) {
                return null;
            }
        }
        return pedidoRepository.saveAll(pedidos);
    }

    private Pedido prepararPedido(Pedido pedido) {
        if (pedido.getUsuario() == null || pedido.getUsuario().getId() == null) {
            return null;
        }
        Usuario usuario = usuarioRepository.findById(pedido.getUsuario().getId()).orElse(null);
        if (usuario == null) {
            return null;
        }
        pedido.setUsuario(usuario);
        if (pedido.getFechaCreacion() == null) {
            pedido.setFechaCreacion(LocalDateTime.now());
        }
        if (pedido.getEstado() == null) {
            pedido.setEstado(EstadoPedido.PENDIENTE);
        }
        return pedido;
    }

    public Pedido updatePedido(Long id, Pedido pedido) {
        Pedido existing = getPedidoById(id);
        if (existing == null) {
            return null;
        }
        if (pedido.getEstado() != null) {
            existing.setEstado(pedido.getEstado());
        }
        return pedidoRepository.save(existing);
    }
}
