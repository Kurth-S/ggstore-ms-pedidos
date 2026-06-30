package com.ggstore.ms_pedidos.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "cupones")
public class Cupon {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String codigo;

    @Column(name = "porcentaje_descuento", nullable = false)
    private BigDecimal porcentajeDescuento;

    @Column(name = "fecha_expiracion")
    private OffsetDateTime fechaExpiracion;

    @Column(nullable = false)
    private boolean activo;
}
