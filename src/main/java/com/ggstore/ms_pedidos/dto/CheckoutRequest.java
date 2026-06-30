package com.ggstore.ms_pedidos.dto;

public record CheckoutRequest(
        String codigoCupon  // opcional, puede ser null
) {}
