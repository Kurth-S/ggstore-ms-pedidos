package com.ggstore.ms_pedidos.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record CarritoDetalleResponse(
        UUID id,
        UUID juegoId,
        String tituloJuego,
        String imagenUrl,
        BigDecimal precioUnitario,
        BigDecimal precioFinal,
        int cantidad,
        BigDecimal subtotal
) {}
