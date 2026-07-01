package com.ggstore.ms_pedidos.controller;

import com.ggstore.ms_pedidos.dto.WishlistItemResponse;
import com.ggstore.ms_pedidos.service.WishlistService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WishlistControllerTest {

    @Mock private WishlistService wishlistService;
    @InjectMocks private WishlistController wishlistController;

    private UUID usuarioId;

    @BeforeEach
    void setUp() {
        usuarioId = UUID.randomUUID();
    }

    @Test
    void listar_retornaItems() {
        WishlistItemResponse item = new WishlistItemResponse(
                UUID.randomUUID(), UUID.randomUUID(), "Zelda TotK", "img.url",
                new BigDecimal("54990"), new BigDecimal("52240"), new BigDecimal("5")
        );
        when(wishlistService.listar(usuarioId)).thenReturn(List.of(item));

        ResponseEntity<List<WishlistItemResponse>> response = wishlistController.listar(usuarioId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
    }

    @Test
    void agregar_retornaNoContent() {
        UUID juegoId = UUID.randomUUID();

        ResponseEntity<Void> response = wishlistController.agregar(usuarioId, juegoId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(wishlistService).agregar(usuarioId, juegoId);
    }

    @Test
    void eliminar_retornaNoContent() {
        UUID itemId = UUID.randomUUID();

        ResponseEntity<Void> response = wishlistController.eliminar(usuarioId, itemId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(wishlistService).eliminar(usuarioId, itemId);
    }
}