package com.tuboleteria.boleteria.model;

import jakarta.persistence.*;
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

    private String nombreComprador;
    private String identificacionComprador;
    private String correoComprador;
    private String celular;
    private int edad;
    private BigDecimal valorTotal;
    private String metodoPago;
    private String estado;
    private String numeroTransaccion;

    @Column(unique = true)
    private String codigoQr;

    private String etapaVenta;

    private LocalDateTime fechaCompra;

    private String tipo; // "General" o "VIP"

    @ManyToOne
    private Usuario vendedor;

    @ManyToOne
    private Evento evento;
}
