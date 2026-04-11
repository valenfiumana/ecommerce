package com.uade.tpo.ecommerce.model;

import lombok.Data;

@Data
public class Direccion {
 
    private Long id;
    private String calle;
    private String ciudad;
    private String provincia;
    private String codigoPostal;
    private String pais;
    
    
}
