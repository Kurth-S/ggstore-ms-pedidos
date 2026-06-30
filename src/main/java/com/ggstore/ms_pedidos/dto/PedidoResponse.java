package com.ggstore.ms_pedidos.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record PedidoResponse(
        UUID id,
        OffsetDateTime fecha,
        BigDecimal total,
        String estado,
        List<PedidoDetalleResponse> items
) {}
