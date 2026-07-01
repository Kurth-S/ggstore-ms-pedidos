package com.ggstore.ms_pedidos.service;

import com.ggstore.ms_pedidos.client.JuegoClient;
import com.ggstore.ms_pedidos.client.JuegoDTO;
import com.ggstore.ms_pedidos.dto.WishlistItemResponse;
import com.ggstore.ms_pedidos.model.WishlistItem;
import com.ggstore.ms_pedidos.repository.WishlistRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WishlistServiceTest {

    @Mock private WishlistRepository wishlistRepository;
    @Mock private JuegoClient juegoClient;

    @InjectMocks private WishlistService wishlistService;

    private UUID usuarioId;
    private UUID juegoId;
    private JuegoDTO juegoDTO;

    @BeforeEach
    void setUp() {
        usuarioId = UUID.randomUUID();
        juegoId = UUID.randomUUID();

        juegoDTO = new JuegoDTO(
                juegoId, "Zelda TotK", "Aventura épica",
                new BigDecimal("54990"), 80, "https://img.url",
                "SWITCH", UUID.randomUUID(), "Aventura",
                new BigDecimal("5"), new BigDecimal("52240")
        );
    }

    @Test
    void listar_retornaItemsConDatosEnriquecidos() {
        WishlistItem item = new WishlistItem();
        item.setId(UUID.randomUUID());
        item.setUsuarioId(usuarioId);
        item.setJuegoId(juegoId);

        when(wishlistRepository.findByUsuarioId(usuarioId)).thenReturn(List.of(item));
        when(juegoClient.obtenerJuego(juegoId)).thenReturn(juegoDTO);

        List<WishlistItemResponse> result = wishlistService.listar(usuarioId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).tituloJuego()).isEqualTo("Zelda TotK");
        assertThat(result.get(0).precioFinal()).isEqualByComparingTo(new BigDecimal("52240"));
    }

    @Test
    void listar_retornaListaVaciaSiNoHayItems() {
        when(wishlistRepository.findByUsuarioId(usuarioId)).thenReturn(List.of());

        List<WishlistItemResponse> result = wishlistService.listar(usuarioId);

        assertThat(result).isEmpty();
    }

    @Test
    void agregar_guardaItemSiNoExiste() {
        when(juegoClient.obtenerJuego(juegoId)).thenReturn(juegoDTO);
        when(wishlistRepository.findByUsuarioIdAndJuegoId(usuarioId, juegoId)).thenReturn(Optional.empty());

        wishlistService.agregar(usuarioId, juegoId);

        verify(wishlistRepository).save(any(WishlistItem.class));
    }

    @Test
    void agregar_noGuardaSiYaExiste() {
        WishlistItem existente = new WishlistItem();
        existente.setId(UUID.randomUUID());

        when(juegoClient.obtenerJuego(juegoId)).thenReturn(juegoDTO);
        when(wishlistRepository.findByUsuarioIdAndJuegoId(usuarioId, juegoId)).thenReturn(Optional.of(existente));

        wishlistService.agregar(usuarioId, juegoId);

        verify(wishlistRepository, never()).save(any());
    }

    @Test
    void eliminar_eliminaCorrectamente() {
        UUID itemId = UUID.randomUUID();
        WishlistItem item = new WishlistItem();
        item.setId(itemId);
        item.setUsuarioId(usuarioId);

        when(wishlistRepository.findById(itemId)).thenReturn(Optional.of(item));

        wishlistService.eliminar(usuarioId, itemId);

        verify(wishlistRepository).delete(item);
    }

    @Test
    void eliminar_lanzaExcepcionSiNoAutorizado() {
        UUID itemId = UUID.randomUUID();
        WishlistItem item = new WishlistItem();
        item.setId(itemId);
        item.setUsuarioId(UUID.randomUUID()); // distinto usuario

        when(wishlistRepository.findById(itemId)).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> wishlistService.eliminar(usuarioId, itemId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("No autorizado");
    }
}