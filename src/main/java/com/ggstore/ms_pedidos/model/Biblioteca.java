package com.ggstore.ms_pedidos.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "biblioteca")
public class Biblioteca {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Column(name = "juego_id", nullable = false)
    private UUID juegoId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_detalle_id")
    private PedidoDetalle pedidoDetalle;

    @CreationTimestamp
    @Column(name = "fecha_adquisicion", updatable = false)
    private OffsetDateTime fechaAdquisicion;
}
