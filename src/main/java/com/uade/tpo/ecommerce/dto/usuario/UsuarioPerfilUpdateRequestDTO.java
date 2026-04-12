package com.uade.tpo.ecommerce.dto.usuario;

import java.time.LocalDate;

import com.uade.tpo.ecommerce.model.Sexo;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// PATCH parcial: solo mandás los campos que querés cambiar (null en JSON = no tocar).
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioPerfilUpdateRequestDTO {

    @Size(max = 120, message = "El nombre no puede superar 120 caracteres")
    private String nombre;

    @Size(max = 120, message = "El apellido no puede superar 120 caracteres")
    private String apellido;

    @Email(message = "El email debe tener un formato válido")
    @Size(max = 255)
    private String email;

    @Past(message = "La fecha de nacimiento debe ser una fecha pasada")
    private LocalDate fechaNacimiento;

    private Sexo sexo;
}
