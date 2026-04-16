package com.uade.tpo.ecommerce.model;

/**
 * Enum que define los roles disponibles en el sistema.
 * USER: compra (carrito, etc.) y publica/gestiona sus productos.
 * ADMIN: acceso administrativo y puede actuar sobre productos aunque no sea el dueño.
 */
public enum Role {
    USER,
    ADMIN
}