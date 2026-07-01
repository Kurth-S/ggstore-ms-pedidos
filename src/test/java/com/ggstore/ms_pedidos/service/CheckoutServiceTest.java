package com.ggstore.ms_pedidos.service;

import com.ggstore.ms_pedidos.client.JuegoClient;
import com.ggstore.ms_pedidos.client.JuegoDTO;
import com.ggstore.ms_pedidos.dto.CheckoutRequest;
import com.ggstore.ms_pedidos.dto.PedidoResponse;
import com.ggstore.ms_pedidos.model.*;
import com.ggstore.ms_pedidos.repository.*;
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
class CheckoutServiceTest {

    @Mock private CarritoRepository carritoRepository;
    @Mock private CarritoDetalleRepository carritoDetalleRepository;
    @Mock private PedidoRepository pedidoRepository;
    @Mock private PedidoDetalleRepository pedidoDetalleRepository;
    @Mock private CuponRepository cuponRepository;
    @Mock private BibliotecaRepository bibliotecaRepository;
    @Mock private JuegoClient juegoClient;

    @InjectMocks private CheckoutService checkoutService;

    private UUID usuarioId;
    private UUID juegoId;
    private Carrito carrito;
    private CarritoDetalle detalle;
    private JuegoDTO juegoDTO;

    @BeforeEach
    void setUp() {
        usuarioId = UUID.randomUUID();
        juegoId = UUID.randomUUID();

        carrito = new Carrito();
        carrito.setId(UUID.randomUUID());
        carrito.setUsuarioId(usuarioId);

        detalle = new CarritoDetalle();
        detalle.setId(UUID.randomUUID());
        detalle.setCarrito(carrito);
        detalle.setJuegoId(juegoId);
        detalle.setCantidad(1);

        juegoDTO = new JuegoDTO(
                juegoId, "Cyberpunk 2077", "RPG futurista",
                new BigDecimal("39990"), 50, "https://img.url",
                "PC", UUID.randomUUID(), "RPG",
                new BigDecimal("20"), new BigDecimal("31992")
        );
    }

    @Test
    void checkout_exitoso() {
        Pedido pedido = new Pedido();
        pedido.setId(UUID.randomUUID());
        pedido.setUsuarioId(usuarioId);
        pedido.setTotal(new BigDecimal("31992"));
        pedido.setEstado("PAGADO");

        when(carritoRepository.findByUsuarioId(usuarioId)).thenReturn(Optional.of(carrito));
        when(carritoDetalleRepository.findByCarritoId(carrito.getId())).thenReturn(List.of(detalle));
        when(juegoClient.obtenerJuego(juegoId)).thenReturn(juegoDTO);
        when(pedidoRepository.save(any())).thenReturn(pedido);
        when(pedidoDetalleRepository.save(any())).thenAnswer(inv -> {
            PedidoDetalle d = inv.getArgument(0);
            d.setId(UUID.randomUUID());
            return d;
        });
        when(bibliotecaRepository.existsByUsuarioIdAndJuegoId(any(), any())).thenReturn(false);

        PedidoResponse response = checkoutService.checkout(usuarioId, new CheckoutRequest(null));

        assertThat(response).isNotNull();
        assertThat(response.estado()).isEqualTo("PAGADO");
        assertThat(response.items()).hasSize(1);
        verify(juegoClient).descontarStock(juegoId, 1);
        verify(carritoDetalleRepository).deleteAll(List.of(detalle));
    }

    @Test
    void checkout_lanzaExcepcionSiCarritoVacio() {
        when(carritoRepository.findByUsuarioId(usuarioId)).thenReturn(Optional.of(carrito));
        when(carritoDetalleRepository.findByCarritoId(carrito.getId())).thenReturn(List.of());

        assertThatThrownBy(() -> checkoutService.checkout(usuarioId, new CheckoutRequest(null)))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("vacío");
    }

    @Test
    void checkout_lanzaExcepcionSiStockInsuficiente() {
        JuegoDTO sinStock = new JuegoDTO(
                juegoId, "Juego", "desc",
                new BigDecimal("10000"), 0, "img",
                "PC", UUID.randomUUID(), "Accion",
                BigDecimal.ZERO, new BigDecimal("10000")
        );

        when(carritoRepository.findByUsuarioId(usuarioId)).thenReturn(Optional.of(carrito));
        when(carritoDetalleRepository.findByCarritoId(carrito.getId())).thenReturn(List.of(detalle));
        when(juegoClient.obtenerJuego(juegoId)).thenReturn(sinStock);

        assertThatThrownBy(() -> checkoutService.checkout(usuarioId, new CheckoutRequest(null)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Stock insuficiente");
    }

    @Test
    void checkout_noAgregaABibliotecaSiYaTiene() {
        Pedido pedido = new Pedido();
        pedido.setId(UUID.randomUUID());
        pedido.setUsuarioId(usuarioId);
        pedido.setTotal(new BigDecimal("31992"));
        pedido.setEstado("PAGADO");

        when(carritoRepository.findByUsuarioId(usuarioId)).thenReturn(Optional.of(carrito));
        when(carritoDetalleRepository.findByCarritoId(carrito.getId())).thenReturn(List.of(detalle));
        when(juegoClient.obtenerJuego(juegoId)).thenReturn(juegoDTO);
        when(pedidoRepository.save(any())).thenReturn(pedido);
        when(pedidoDetalleRepository.save(any())).thenAnswer(inv -> {
            PedidoDetalle d = inv.getArgument(0);
            d.setId(UUID.randomUUID());
            return d;
        });
        when(bibliotecaRepository.existsByUsuarioIdAndJuegoId(any(), any())).thenReturn(true);

        checkoutService.checkout(usuarioId, new CheckoutRequest(null));

        verify(bibliotecaRepository, never()).save(any());
    }

    @Test
    void historial_retornaListaDePedidos() {
        Pedido pedido = new Pedido();
        pedido.setId(UUID.randomUUID());
        pedido.setUsuarioId(usuarioId);
        pedido.setTotal(new BigDecimal("31992"));
        pedido.setEstado("PAGADO");

        PedidoDetalle pd = new PedidoDetalle();
        pd.setId(UUID.randomUUID());
        pd.setJuegoId(juegoId);
        pd.setPrecio(new BigDecimal("31992"));
        pd.setCantidad(1);
        pd.setClaveDigital("GG-XXXX-YYYY");

        when(pedidoRepository.findByUsuarioIdOrderByFechaDesc(usuarioId)).thenReturn(List.of(pedido));
        when(pedidoDetalleRepository.findByPedidoId(pedido.getId())).thenReturn(List.of(pd));
        when(juegoClient.obtenerJuego(juegoId)).thenReturn(juegoDTO);

        var result = checkoutService.historial(usuarioId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).estado()).isEqualTo("PAGADO");
    }
}