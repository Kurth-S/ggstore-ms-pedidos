package com.ggstore.ms_pedidos.controller;

import com.ggstore.ms_pedidos.dto.CheckoutRequest;
import com.ggstore.ms_pedidos.dto.PedidoResponse;
import com.ggstore.ms_pedidos.service.CheckoutService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/pedidos")
@RequiredArgsConstructor
public class CheckoutController {

    private final CheckoutService checkoutService;

    @PostMapping("/checkout")
    public ResponseEntity<PedidoResponse> checkout(
            @RequestHeader("X-Usuario-Id") UUID usuarioId,
            @RequestBody(required = false) CheckoutRequest request) {
        if (request == null) request = new CheckoutRequest(null);
        return ResponseEntity.ok(checkoutService.checkout(usuarioId, request));
    }

    @GetMapping("/historial")
    public ResponseEntity<List<PedidoResponse>> historial(@RequestHeader("X-Usuario-Id") UUID usuarioId) {
        return ResponseEntity.ok(checkoutService.historial(usuarioId));
    }
}
