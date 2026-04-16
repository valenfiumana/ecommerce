package com.uade.tpo.ecommerce.dto.producto;

import java.util.Arrays;
import java.util.Optional;

import org.springframework.data.domain.Sort;

import com.uade.tpo.ecommerce.exception.ArgumentInvalidException;

/**
 * Whitelist de órdenes permitidos para evitar depender de nombres de columna recibidos desde el cliente.
 * Como Producto todavía no tiene fecha de alta, "fecha" se interpreta como id desc (más recientes primero).
 */
public enum ProductoOrden {

    RECIENTES("fecha", Sort.by(Sort.Direction.DESC, "id")),
    PRECIO_ASC("precio_asc", Sort.by(Sort.Direction.ASC, "precio")),
    PRECIO_DESC("precio_desc", Sort.by(Sort.Direction.DESC, "precio"));

    private final String value;
    private final Sort sort;

    ProductoOrden(String value, Sort sort) {
        this.value = value;
        this.sort = sort;
    }

    public Sort toSort() {
        return sort;
    }

    public static ProductoOrden fromQueryParam(String rawValue) {
        String normalizedValue = Optional.ofNullable(rawValue)
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .orElse(RECIENTES.value);

        return Arrays.stream(values())
                .filter(orden -> orden.value.equalsIgnoreCase(normalizedValue))
                .findFirst()
                .orElseThrow(() -> new ArgumentInvalidException(
                        "El parámetro orden es inválido. Valores permitidos: fecha, precio_asc, precio_desc"));
    }
}
