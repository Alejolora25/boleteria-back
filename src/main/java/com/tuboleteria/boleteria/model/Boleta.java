package com.tuboleteria.boleteria.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "boletas") // Define explícitamente el nombre de la tabla
public class Boleta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del comprador es obligatorio")
    private String nombreComprador;

    @NotBlank(message = "La identificación del comprador es obligatoria")
    private String identificacionComprador;

    @NotBlank(message = "El correo del comprador es obligatorio")
    @Email(message = "El correo debe ser válido")
    private String correoComprador;

    @NotBlank(message = "El número de celular es obligatorio")
    private String celular;

    @NotNull(message = "La edad del comprador es obligatoria")
    @Min(value = 0, message = "La edad no puede ser negativa")
    private int edad;

    @NotNull(message = "El valor total es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El valor total debe ser mayor a cero")
    private BigDecimal valorTotal;

    @NotBlank(message = "El método de pago es obligatorio")
    private String metodoPago;

    private String estado;
    private String numeroTransaccion;

    @Column(unique = true)
    private String codigoQr;

    private String etapaVenta;

    private LocalDateTime fechaCompra;

    @NotBlank(message = "El tipo de boleta es obligatorio")
    private String tipo; // "General" o "VIP"

    @ManyToOne
    private Usuario vendedor;

    @ManyToOne
    private Evento evento;
}
