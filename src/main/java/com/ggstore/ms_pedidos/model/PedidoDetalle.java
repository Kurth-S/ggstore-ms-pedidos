package com.ggstore.ms_pedidos.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Entity
@Table(name = "pedido_detalle")
public class PedidoDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id", nullable = false)
    private Pedido pedido;

    @Column(name = "juego_id", nullable = false)
    private UUID juegoId;

    @Column(nullable = false)
    private BigDecimal precio;

    @Column(nullable = false)
    private int cantidad;

    @Column(name = "clave_digital")
    private String claveDigital;
}
