package com.ggstore.ms_pedidos.controller;

import com.ggstore.ms_pedidos.dto.AgregarAlCarritoRequest;
import com.ggstore.ms_pedidos.dto.CarritoResponse;
import com.ggstore.ms_pedidos.service.CarritoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/carrito")
@RequiredArgsConstructor
public class CarritoController {

    private final CarritoService carritoService;

    @GetMapping
    public ResponseEntity<CarritoResponse> obtener(@RequestHeader("X-Usuario-Id") UUID usuarioId) {
        return ResponseEntity.ok(carritoService.obtenerCarrito(usuarioId));
    }

    @PostMapping("/items")
    public ResponseEntity<CarritoResponse> agregar(
            @RequestHeader("X-Usuario-Id") UUID usuarioId,
            @Valid @RequestBody AgregarAlCarritoRequest request) {
        return ResponseEntity.ok(carritoService.agregar(usuarioId, request));
    }

    @PutMapping("/items/{detalleId}")
    public ResponseEntity<CarritoResponse> actualizar(
            @RequestHeader("X-Usuario-Id") UUID usuarioId,
            @PathVariable UUID detalleId,
            @RequestParam int cantidad) {
        return ResponseEntity.ok(carritoService.actualizar(usuarioId, detalleId, cantidad));
    }

    @DeleteMapping("/items/{detalleId}")
    public ResponseEntity<Void> eliminar(
            @RequestHeader("X-Usuario-Id") UUID usuarioId,
            @PathVariable UUID detalleId) {
        carritoService.eliminarItem(usuarioId, detalleId);
        return ResponseEntity.noContent().build();
    }
}
