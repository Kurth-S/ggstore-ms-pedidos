package com.ggstore.ms_pedidos.client;

import java.math.BigDecimal;
import java.util.UUID;

public record JuegoDTO(
        UUID id,
        String titulo,
        String descripcion,
        BigDecimal precio,
        Integer stock,
        String imagenUrl,
        String plataforma,
        UUID categoriaId,
        String categoriaNombre,
        BigDecimal descuentoPorcentaje,
        BigDecimal precioFinal
) {}
