package com.ggstore.ms_pedidos.repository;

import com.ggstore.ms_pedidos.model.PedidoDetalle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface PedidoDetalleRepository extends JpaRepository<PedidoDetalle, UUID> {

    List<PedidoDetalle> findByPedidoId(UUID pedidoId);

    @Query(value = """
        SELECT juego_id, SUM(cantidad) as total_vendido
        FROM pedido_detalle
        GROUP BY juego_id
        ORDER BY total_vendido DESC
        LIMIT 5
        """, nativeQuery = true)
    List<Object[]> findTopJuegosVendidos();
}