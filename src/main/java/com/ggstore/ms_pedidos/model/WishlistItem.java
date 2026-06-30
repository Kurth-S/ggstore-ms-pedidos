package com.ggstore.ms_pedidos.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "wishlist_items", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"usuario_id", "juego_id"})
})
public class WishlistItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    // No usamos relación JPA hacia Juego: ese dato vive en ms-catalogo.
    @Column(name = "juego_id", nullable = false)
    private UUID juegoId;

    @CreationTimestamp
    @Column(name = "fecha_agregado", updatable = false)
    private OffsetDateTime fechaAgregado;
}