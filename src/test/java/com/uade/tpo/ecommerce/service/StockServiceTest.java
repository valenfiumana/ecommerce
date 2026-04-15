package com.uade.tpo.ecommerce.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.uade.tpo.ecommerce.exception.BusinessRuleException;
import com.uade.tpo.ecommerce.model.Producto;
import com.uade.tpo.ecommerce.repository.ProductoRepository;

@ExtendWith(MockitoExtension.class)
class StockServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private StockService stockService;

    @Test
    void descuentaStockCuandoLaActualizacionAtomicaTieneExito() {
        when(productoRepository.descontarStockSiAlcanza(1L, 2)).thenReturn(1);

        assertDoesNotThrow(() -> stockService.descontarStockParaCheckout(1L, "Mouse", 2));

        verify(productoRepository).descontarStockSiAlcanza(1L, 2);
        verify(productoRepository, never()).findById(1L);
    }

    @Test
    void lanzaErrorSiOtraCompraConsumeElUltimoStockAntesDelCheckout() {
        Producto producto = Producto.builder()
                .id(1L)
                .nombre("Mouse")
                .stock(1)
                .build();

        when(productoRepository.descontarStockSiAlcanza(1L, 2)).thenReturn(0);
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));

        BusinessRuleException ex = assertThrows(
                BusinessRuleException.class,
                () -> stockService.descontarStockParaCheckout(1L, "Mouse", 2));

        assertEquals("Stock insuficiente para 'Mouse': hay 1 disponibles y se pidieron 2.", ex.getMessage());
    }

    @Test
    void permiteUnCheckoutYRechazaElSiguienteCuandoElStockEsLimite() {
        Producto productoSinStock = Producto.builder()
                .id(1L)
                .nombre("Mouse")
                .stock(0)
                .build();

        when(productoRepository.descontarStockSiAlcanza(1L, 1)).thenReturn(1, 0);
        when(productoRepository.findById(1L)).thenReturn(Optional.of(productoSinStock));

        assertDoesNotThrow(() -> stockService.descontarStockParaCheckout(1L, "Mouse", 1));

        BusinessRuleException ex = assertThrows(
                BusinessRuleException.class,
                () -> stockService.descontarStockParaCheckout(1L, "Mouse", 1));

        assertEquals("Stock insuficiente para 'Mouse': hay 0 disponibles y se pidieron 1.", ex.getMessage());
    }
}
