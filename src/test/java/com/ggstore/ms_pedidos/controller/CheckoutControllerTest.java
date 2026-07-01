package com.ggstore.ms_pedidos.controller;

import com.ggstore.ms_pedidos.dto.CheckoutRequest;
import com.ggstore.ms_pedidos.dto.PedidoResponse;
import com.ggstore.ms_pedidos.service.CheckoutService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CheckoutControllerTest {

    @Mock private CheckoutService checkoutService;
    @InjectMocks private CheckoutController checkoutController;

    private UUID usuarioId;
    private PedidoResponse pedidoResponse;

    @BeforeEach
    void setUp() {
        usuarioId = UUID.randomUUID();
        pedidoResponse = new PedidoResponse(
                UUID.randomUUID(), OffsetDateTime.now(), BigDecimal.TEN, "PAGADO", List.of()
        );
    }

    @Test
    void checkout_conRequestValido_retornaPedido() {
        CheckoutRequest request = new CheckoutRequest(null);
        when(checkoutService.checkout(usuarioId, request)).thenReturn(pedidoResponse);

        ResponseEntity<PedidoResponse> response = checkoutController.checkout(usuarioId, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(pedidoResponse);
    }

    @Test
    void checkout_conRequestNulo_usaRequestPorDefecto() {
        when(checkoutService.checkout(eq(usuarioId), any(CheckoutRequest.class))).thenReturn(pedidoResponse);

        ResponseEntity<PedidoResponse> response = checkoutController.checkout(usuarioId, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(checkoutService).checkout(eq(usuarioId), any(CheckoutRequest.class));
    }

    @Test
    void historial_retornaListaDePedidos() {
        when(checkoutService.historial(usuarioId)).thenReturn(List.of(pedidoResponse));

        ResponseEntity<List<PedidoResponse>> response = checkoutController.historial(usuarioId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
    }
}