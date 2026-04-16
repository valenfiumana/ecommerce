package com.uade.tpo.ecommerce.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "direcciones")
public class Direccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false)
    private String calle;

    @Column(nullable = false)
    private String numero;

    @Column(name = "codigo_postal", nullable = false)
    private String codigoPostal;

    @Column(nullable = false)
    private String ciudad;

    @Column(nullable = false)
    private String provincia;

    @Column(nullable = false)
    private String pais;

    private String referencia;

    @Column(nullable = false)
    @Builder.Default
    private boolean principal = false;
}
