package com.uade.tpo.ecommerce.model;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.*;
import lombok.*;


/**
 * Entidad JPA mapeada a la tabla {@code usuarios}. 
 * Implementa {@link UserDetails} para integrar el usuario con Spring Security
 * - credenciales del usuario (username y password)
 * - roles como {@code ROLE_USER}/{@code ROLE_ADMIN}
 * - estado de la cuenta (si está bloqueada, expirada, etc.)
 */
@Data
// @Builder (patrón Builder): Lombok genera un "builder" para armar el objeto paso a paso de forma legible,
// por ejemplo Usuario.builder().nombre("Ana").email("a@mail.com").password(enc).role(Role.USER).build()
// en lugar de un constructor gigante o muchos setters. El builder es útil cuando hay muchos campos opcionales
// o querés dejar explícito qué valor corresponde a qué atributo. Junto con @NoArgsConstructor y @AllArgsConstructor,
// JPA y el builder pueden convivir: Hibernate usa el constructor vacío y el código de negocio puede usar .builder().
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "usuarios")
public class Usuario implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String apellido;
    @Column(name = "nombre_usuario", unique = true)
    private String nombreUsuario;
    @Column(unique = true)
    private String email;
    private String password;

    // Fecha de nacimiento del usuario (solo fecha, sin zona horaria; tipo DATE en SQL)
    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    // Sexo del perfil; se persiste como texto (MASCULINO, FEMENINO, …) gracias a EnumType.STRING
    @Enumerated(EnumType.STRING)
    @Column(length = 32)
    private Sexo sexo;

    // El rol del usuario (ADMIN o USER) se almacena como un string en la base de datos
    // @Enumerated(EnumType.STRING) indica que el enum se guardará como texto en la base de datos, no como un número
    @Enumerated(EnumType.STRING)
    private Role role;

    @Builder.Default
    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private boolean activo = true;

    // @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL)
    // private List<Pedido> pedidos;

    /**
     * getAuthorities() devuelve la colección de roles/permisos del usuario
     * - Cada autoridad debe implementar GrantedAuthority
     * - En este caso, convertimos el enum Role a un formato "ROLE_X" (ej: ROLE_ADMIN, ROLE_USER)
     * - Spring Security utiliza estas autoridades para control de acceso y seguridad
     * - Si el rol es null, asigna por defecto "ROLE_USER"
     * ? extended GrantedAuthority cualquier clase que extienda de GrantedAuthority
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // resultado ROLE_USER o ROLE_ADMIN
        return List.of(new SimpleGrantedAuthority("ROLE_" + (role != null ? role.name() : "USER")));
    }

    /**
     * getUsername() retorna el identificador único del usuario para autenticación
     * - En este caso usamos el email como username
     * - Spring Security utiliza este método para identificar al usuario durante
     *   el proceso de autenticación y en el contexto de seguridad
     */
    @Override
    public String getUsername() {
        return email;
    }

    //estado de la cuenta
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return activo;
    }
}
