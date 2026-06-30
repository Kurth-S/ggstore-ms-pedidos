package com.ggstore.ms_pedidos.controller;

import com.ggstore.ms_pedidos.dto.WishlistItemResponse;
import com.ggstore.ms_pedidos.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;

    @GetMapping
    public ResponseEntity<List<WishlistItemResponse>> listar(@RequestHeader("X-Usuario-Id") UUID usuarioId) {
        return ResponseEntity.ok(wishlistService.listar(usuarioId));
    }

    @PostMapping("/{juegoId}")
    public ResponseEntity<Void> agregar(
            @RequestHeader("X-Usuario-Id") UUID usuarioId,
            @PathVariable UUID juegoId) {
        wishlistService.agregar(usuarioId, juegoId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @RequestHeader("X-Usuario-Id") UUID usuarioId,
            @PathVariable UUID id) {
        wishlistService.eliminar(usuarioId, id);
        return ResponseEntity.noContent().build();
    }
}