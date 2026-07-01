package com.ggstore.ms_pedidos.controller;

import com.ggstore.ms_pedidos.dto.AgregarAlCarritoRequest;
import com.ggstore.ms_pedidos.dto.CarritoResponse;
import com.ggstore.ms_pedidos.service.CarritoService;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CarritoControllerTest {

    @Mock private CarritoService carritoService;
    @InjectMocks private CarritoController carritoController;

    private UUID usuarioId;
    private CarritoResponse carritoResponse;

    @BeforeEach
    void setUp() {
        usuarioId = UUID.randomUUID();
        carritoResponse = new CarritoResponse(UUID.randomUUID(), List.of(), BigDecimal.ZERO);
    }

    @Test
    void obtener_retornaCarrito() {
        when(carritoService.obtenerCarrito(usuarioId)).thenReturn(carritoResponse);

        ResponseEntity<CarritoResponse> response = carritoController.obtener(usuarioId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(carritoResponse);
    }

    @Test
    void agregar_retornaCarritoActualizado() {
        UUID juegoId = UUID.randomUUID();
        AgregarAlCarritoRequest request = new AgregarAlCarritoRequest(juegoId, 1);

        when(carritoService.agregar(usuarioId, request)).thenReturn(carritoResponse);

        ResponseEntity<CarritoResponse> response = carritoController.agregar(usuarioId, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(carritoService).agregar(usuarioId, request);
    }

    @Test
    void actualizar_retornaCarritoActualizado() {
        UUID detalleId = UUID.randomUUID();

        when(carritoService.actualizar(usuarioId, detalleId, 3)).thenReturn(carritoResponse);

        ResponseEntity<CarritoResponse> response = carritoController.actualizar(usuarioId, detalleId, 3);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(carritoService).actualizar(usuarioId, detalleId, 3);
    }

    @Test
    void eliminar_retornaNoContent() {
        UUID detalleId = UUID.randomUUID();

        ResponseEntity<Void> response = carritoController.eliminar(usuarioId, detalleId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(carritoService).eliminarItem(usuarioId, detalleId);
    }
}