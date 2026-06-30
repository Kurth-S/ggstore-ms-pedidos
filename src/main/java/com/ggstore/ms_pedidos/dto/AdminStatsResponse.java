package com.ggstore.ms_pedidos.dto;

import java.math.BigDecimal;
import java.util.List;

public record AdminStatsResponse(
        long totalPedidos,
        BigDecimal ingresosTotales,
        List<JuegoMasVendidoResponse> juegosMasVendidos,
        List<VentasPorMesResponse> ventasPorMes
) {}