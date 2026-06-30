package com.ggstore.ms_pedidos.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record BibliotecaResponse(
        UUID id,
        UUID juegoId,
        String tituloJuego,
        String imagenUrl,
        String claveDigital,
        OffsetDateTime fechaAdquisicion
) {}
