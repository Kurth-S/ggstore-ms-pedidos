package com.ggstore.ms_pedidos.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AgregarAlCarritoRequest(
        @NotNull(message = "El juego es obligatorio")
        UUID juegoId,

        @Min(value = 1, message = "La cantidad mínima es 1")
        int cantidad
) {}
