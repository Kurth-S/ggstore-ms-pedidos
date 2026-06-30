package com.ggstore.ms_pedidos.service;

import com.ggstore.ms_pedidos.client.JuegoClient;
import com.ggstore.ms_pedidos.client.JuegoDTO;
import com.ggstore.ms_pedidos.dto.WishlistItemResponse;
import com.ggstore.ms_pedidos.model.WishlistItem;
import com.ggstore.ms_pedidos.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final JuegoClient juegoClient;

    @Transactional(readOnly = true)
    public List<WishlistItemResponse> listar(UUID usuarioId) {
        return wishlistRepository.findByUsuarioId(usuarioId).stream()
                .map(item -> {
                    JuegoDTO juego = juegoClient.obtenerJuego(item.getJuegoId());
                    return new WishlistItemResponse(
                            item.getId(),
                            item.getJuegoId(),
                            juego.titulo(),
                            juego.imagenUrl(),
                            juego.precio(),
                            juego.precioFinal(),
                            juego.descuentoPorcentaje()
                    );
                })
                .toList();
    }

    @Transactional
    public void agregar(UUID usuarioId, UUID juegoId) {
        // Verifica que el juego exista en ms-catalogo antes de agregarlo
        juegoClient.obtenerJuego(juegoId);

        boolean yaExiste = wishlistRepository.findByUsuarioIdAndJuegoId(usuarioId, juegoId).isPresent();
        if (yaExiste) {
            return; // idempotente: si ya está, no hace nada
        }

        WishlistItem item = new WishlistItem();
        item.setUsuarioId(usuarioId);
        item.setJuegoId(juegoId);
        wishlistRepository.save(item);
    }

    @Transactional
    public void eliminar(UUID usuarioId, UUID id) {
        WishlistItem item = wishlistRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Item no encontrado en la wishlist"));

        if (!item.getUsuarioId().equals(usuarioId)) {
            throw new RuntimeException("No autorizado");
        }

        wishlistRepository.delete(item);
    }
}