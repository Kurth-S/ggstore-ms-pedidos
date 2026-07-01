package com.ggstore.ms_pedidos.repository;

import com.ggstore.ms_pedidos.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface PedidoRepository extends JpaRepository<Pedido, UUID> {

    List<Pedido> findByUsuarioIdOrderByFechaDesc(UUID usuarioId);

    @Query("SELECT COUNT(p) FROM Pedido p WHERE p.estado = 'PAGADO'")
    Long countPedidosPagados();

    @Query("SELECT COALESCE(SUM(p.total), 0) FROM Pedido p WHERE p.estado = 'PAGADO'")
    BigDecimal sumIngresosTotales();

    @Query(value = """
        SELECT EXTRACT(MONTH FROM fecha) as mes,
               EXTRACT(YEAR FROM fecha) as anio,
               COUNT(*) as total_pedidos,
               SUM(total) as ingresos
        FROM pedidos
        WHERE estado = 'PAGADO'
        GROUP BY anio, mes
        ORDER BY anio DESC, mes DESC
        LIMIT 12
        """, nativeQuery = true)
    List<Object[]> findVentasPorMes();
}