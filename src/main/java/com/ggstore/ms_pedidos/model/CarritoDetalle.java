package com.ggstore.ms_pedidos.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Data
@Entity
@Table(name = "carrito_detalle")
public class CarritoDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "carrito_id", nullable = false)
    private Carrito carrito;

    // No usamos relación JPA hacia Juego: ese dato vive en ms-catalogo.
    @Column(name = "juego_id", nullable = false)
    private UUID juegoId;

    @Column(nullable = false)
    private int cantidad;
}
