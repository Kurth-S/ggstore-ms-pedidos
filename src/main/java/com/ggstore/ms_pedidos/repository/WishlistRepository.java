package com.ggstore.ms_pedidos.repository;

import com.ggstore.ms_pedidos.model.WishlistItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WishlistRepository extends JpaRepository<WishlistItem, UUID> {
    List<WishlistItem> findByUsuarioId(UUID usuarioId);
    Optional<WishlistItem> findByUsuarioIdAndJuegoId(UUID usuarioId, UUID juegoId);
}