package com.uade.tpo.ecommerce.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Copia inmutable de la dirección en el momento del checkout.
 * Si el usuario borra la {@link Direccion} guardada, el pedido sigue mostrando estos datos.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class DireccionSnapshot {

    @Column(name = "shipping_calle")
    private String calle;

    @Column(name = "shipping_numero")
    private String numero;

    @Column(name = "shipping_codigo_postal")
    private String codigoPostal;

    @Column(name = "shipping_ciudad")
    private String ciudad;

    @Column(name = "shipping_provincia")
    private String provincia;

    @Column(name = "shipping_pais")
    private String pais;

    @Column(name = "shipping_referencia")
    private String referencia;

    public static String formatearUnaLinea(DireccionSnapshot s) {
        if (s == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        if (s.getCalle() != null) {
            sb.append(s.getCalle().trim());
        }
        if (s.getNumero() != null && !s.getNumero().isBlank()) {
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(s.getNumero().trim());
        }
        if (s.getCodigoPostal() != null && !s.getCodigoPostal().isBlank()) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(s.getCodigoPostal().trim());
        }
        if (s.getCiudad() != null && !s.getCiudad().isBlank()) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(s.getCiudad().trim());
        }
        if (s.getProvincia() != null && !s.getProvincia().isBlank()) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(s.getProvincia().trim());
        }
        if (s.getPais() != null && !s.getPais().isBlank()) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(s.getPais().trim());
        }
        if (s.getReferencia() != null && !s.getReferencia().isBlank()) {
            if (sb.length() > 0) {
                sb.append(" — ");
            }
            sb.append(s.getReferencia().trim());
        }
        return sb.length() > 0 ? sb.toString() : null;
    }
}
