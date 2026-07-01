package com.ggstore.ms_pedidos.service;

import com.ggstore.ms_pedidos.client.JuegoClient;
import com.ggstore.ms_pedidos.client.JuegoDTO;
import com.ggstore.ms_pedidos.dto.AgregarAlCarritoRequest;
import com.ggstore.ms_pedidos.dto.CarritoResponse;
import com.ggstore.ms_pedidos.model.Carrito;
import com.ggstore.ms_pedidos.model.CarritoDetalle;
import com.ggstore.ms_pedidos.repository.CarritoDetalleRepository;
import com.ggstore.ms_pedidos.repository.CarritoRepository;
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
class CarritoServiceTest {

    @Mock private CarritoRepository carritoRepository;
    @Mock private CarritoDetalleRepository carritoDetalleRepository;
    @Mock private JuegoClient juegoClient;

    @InjectMocks private CarritoService carritoService;

    private UUID usuarioId;
    private UUID juegoId;
    private Carrito carrito;
    private JuegoDTO juegoDTO;

    @BeforeEach
    void setUp() {
        usuarioId = UUID.randomUUID();
        juegoId = UUID.randomUUID();

        carrito = new Carrito();
        carrito.setId(UUID.randomUUID());
        carrito.setUsuarioId(usuarioId);

        juegoDTO = new JuegoDTO(
                juegoId, "Cyberpunk 2077", "RPG futurista",
                new BigDecimal("39990"), 50, "https://img.url",
                "PC", UUID.randomUUID(), "RPG",
                new BigDecimal("20"), new BigDecimal("31992")
        );
    }

    @Test
    void obtenerCarrito_creaCarritoSiNoExiste() {
        when(carritoRepository.findByUsuarioId(usuarioId)).thenReturn(Optional.empty());
        when(carritoRepository.save(any())).thenReturn(carrito);
        when(carritoDetalleRepository.findByCarritoId(any())).thenReturn(List.of());

        CarritoResponse response = carritoService.obtenerCarrito(usuarioId);

        assertThat(response).isNotNull();
        assertThat(response.items()).isEmpty();
        assertThat(response.total()).isEqualByComparingTo(BigDecimal.ZERO);
        verify(carritoRepository).save(any());
    }

    @Test
    void obtenerCarrito_retornaCarritoExistente() {
        when(carritoRepository.findByUsuarioId(usuarioId)).thenReturn(Optional.of(carrito));
        when(carritoDetalleRepository.findByCarritoId(carrito.getId())).thenReturn(List.of());

        CarritoResponse response = carritoService.obtenerCarrito(usuarioId);

        assertThat(response.carritoId()).isEqualTo(carrito.getId());
        verify(carritoRepository, never()).save(any());
    }

    @Test
    void agregar_itemNuevoAlCarrito() {
        AgregarAlCarritoRequest request = new AgregarAlCarritoRequest(juegoId, 1);
        CarritoDetalle detalle = new CarritoDetalle();
        detalle.setId(UUID.randomUUID());
        detalle.setCarrito(carrito);
        detalle.setJuegoId(juegoId);
        detalle.setCantidad(1);

        when(carritoRepository.findByUsuarioId(usuarioId)).thenReturn(Optional.of(carrito));
        when(juegoClient.obtenerJuego(juegoId)).thenReturn(juegoDTO);
        when(carritoDetalleRepository.findByCarritoIdAndJuegoId(any(), any())).thenReturn(Optional.empty());
        when(carritoDetalleRepository.save(any())).thenReturn(detalle);
        when(carritoDetalleRepository.findByCarritoId(carrito.getId())).thenReturn(List.of(detalle));

        CarritoResponse response = carritoService.agregar(usuarioId, request);

        assertThat(response.items()).hasSize(1);
        verify(carritoDetalleRepository).save(any());
    }

    @Test
    void agregar_lanzaExcepcionSiStockInsuficiente() {
        JuegoDTO sinStock = new JuegoDTO(
                juegoId, "Juego", "desc",
                new BigDecimal("10000"), 0, "img",
                "PC", UUID.randomUUID(), "Accion",
                BigDecimal.ZERO, new BigDecimal("10000")
        );
        AgregarAlCarritoRequest request = new AgregarAlCarritoRequest(juegoId, 1);

        when(carritoRepository.findByUsuarioId(usuarioId)).thenReturn(Optional.of(carrito));
        when(juegoClient.obtenerJuego(juegoId)).thenReturn(sinStock);

        assertThatThrownBy(() -> carritoService.agregar(usuarioId, request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Stock insuficiente");
    }

    @Test
    void eliminarItem_eliminaCorrectamente() {
        UUID detalleId = UUID.randomUUID();
        CarritoDetalle detalle = new CarritoDetalle();
        detalle.setId(detalleId);
        detalle.setCarrito(carrito);
        detalle.setJuegoId(juegoId);
        detalle.setCantidad(1);

        when(carritoDetalleRepository.findById(detalleId)).thenReturn(Optional.of(detalle));

        carritoService.eliminarItem(usuarioId, detalleId);

        verify(carritoDetalleRepository).delete(detalle);
    }

    @Test
    void eliminarItem_lanzaExcepcionSiNoAutorizado() {
        UUID detalleId = UUID.randomUUID();
        UUID otroUsuario = UUID.randomUUID();
        Carrito otroCarrito = new Carrito();
        otroCarrito.setId(UUID.randomUUID());
        otroCarrito.setUsuarioId(otroUsuario);

        CarritoDetalle detalle = new CarritoDetalle();
        detalle.setId(detalleId);
        detalle.setCarrito(otroCarrito);

        when(carritoDetalleRepository.findById(detalleId)).thenReturn(Optional.of(detalle));

        assertThatThrownBy(() -> carritoService.eliminarItem(usuarioId, detalleId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("No autorizado");
    }
}