package com.ggstore.ms_pedidos.dto;

import java.util.UUID;

public record JuegoMasVendidoResponse(
        UUID juegoId,
        String titulo,
        Long cantidadVendida
) {}