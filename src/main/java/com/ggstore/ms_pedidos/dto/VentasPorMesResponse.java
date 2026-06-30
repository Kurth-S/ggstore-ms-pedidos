package com.ggstore.ms_pedidos.dto;

import java.math.BigDecimal;

public record VentasPorMesResponse(
        int mes,
        int anio,
        Long totalPedidos,
        BigDecimal ingresos
) {}