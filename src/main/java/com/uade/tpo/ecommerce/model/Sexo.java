package com.uade.tpo.ecommerce.model;

/**
 * Sexo biológico o identidad registrada en el perfil del usuario.
 * Se persiste como texto en la base de datos gracias a @Enumerated(EnumType.STRING) en la entidad Usuario.
 */
public enum Sexo {
    MASCULINO,
    FEMENINO,
    OTRO,
    NO_INDICA
}
