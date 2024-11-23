package com.tuboleteria.boleteria.model;

import java.util.List;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "usuarios", uniqueConstraints = {
    @UniqueConstraint(columnNames = "identificacion")
})
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Size(min = 3, max = 50, message = "El nombre debe tener entre 3 y 50 caracteres")
    private String nombre;

    @Column(unique = true) // Define la unicidad a nivel de base de datos
    @NotNull
    @Pattern(regexp = "\\d{8,10}", message = "La identificación debe ser numérica y tener entre 8 y 10 dígitos")
    private String identificacion;

    @Column(unique = true)
    @NotNull
    @Email(message = "El correo debe ser válido")
    private String correo;

    @NotNull
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    private String contraseña;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "usuario_roles", joinColumns = @JoinColumn(name = "usuario_id"))
    @Column(name = "roles")
    private List<String> roles; // Los roles se guardan en la tabla usuario_roles
}


