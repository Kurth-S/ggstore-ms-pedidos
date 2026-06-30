package com.ggstore.ms_pedidos.repository;

import com.ggstore.ms_pedidos.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PedidoRepository extends JpaRepository<Pedido, UUID> {
    List<Pedido> findByUsuarioIdOrderByFechaDesc(UUID usuarioId);
}
