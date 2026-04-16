package com.uade.tpo.ecommerce.exception;

/**
 * Excepción de dominio cuando no existe un recurso solicitado (404).
 * Agrupa casos homogéneos: por ejemplo Producto, Usuario o Pedido no encontrados por id u otro criterio,
 * evitando una clase distinta por cada entidad.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    /**
     * @param resourceName nombre legible del recurso (ej. "Producto", "Usuario", "Pedido")
     * @param id           identificador usado en la búsqueda (se incluye en el mensaje)
     */
    public ResourceNotFoundException(String resourceName, Object id) {
        super(String.format("%s no encontrado con identificador: %s", resourceName, id));
    }
}
