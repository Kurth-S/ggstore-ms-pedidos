package com.ggstore.ms_pedidos.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record WishlistItemResponse(
        UUID id,
        UUID juegoId,
        String tituloJuego,
        String imagenUrl,
        BigDecimal precio,
        BigDecimal precioFinal,
        BigDecimal descuentoPorcentaje
) {}