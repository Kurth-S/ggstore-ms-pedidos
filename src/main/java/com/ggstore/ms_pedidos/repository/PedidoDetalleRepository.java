package com.ggstore.ms_pedidos.repository;

import com.ggstore.ms_pedidos.model.PedidoDetalle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PedidoDetalleRepository extends JpaRepository<PedidoDetalle, UUID> {
    List<PedidoDetalle> findByPedidoId(UUID pedidoId);
}
