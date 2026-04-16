package com.uade.tpo.ecommerce.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.uade.tpo.ecommerce.exception.BusinessRuleException;
import com.uade.tpo.ecommerce.exception.ResourceNotFoundException;
import com.uade.tpo.ecommerce.model.Producto;
import com.uade.tpo.ecommerce.repository.ProductoRepository;

/**
 * Centraliza reglas de stock.
 * En este proyecto el stock baja al confirmar el checkout, dentro de la misma transacción del pedido.
 */
@Service
public class StockService {

    @Autowired
    private ProductoRepository productoRepository;

    /**
     * Intenta descontar stock de forma atómica.
     * Si no alcanza por concurrencia o por falta real de unidades, lanza una excepción de negocio.
     */
    public void descontarStockParaCheckout(Long productoId, String nombreProducto, int cantidad) {
        int updatedRows = productoRepository.descontarStockSiAlcanza(productoId, cantidad);
        if (updatedRows == 1) {
            return;
        }

        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto", productoId));

        int stockActual = producto.getStock() != null ? producto.getStock() : 0;
        throw new BusinessRuleException(
                String.format("Stock insuficiente para '%s': hay %d disponibles y se pidieron %d.",
                        nombreProducto != null ? nombreProducto : producto.getNombre(),
                        stockActual,
                        cantidad));
    }
}
