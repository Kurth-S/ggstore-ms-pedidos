package com.ggstore.ms_pedidos.service;

import com.ggstore.ms_pedidos.client.JuegoClient;
import com.ggstore.ms_pedidos.client.JuegoDTO;
import com.ggstore.ms_pedidos.dto.AgregarAlCarritoRequest;
import com.ggstore.ms_pedidos.dto.CarritoDetalleResponse;
import com.ggstore.ms_pedidos.dto.CarritoResponse;
import com.ggstore.ms_pedidos.model.Carrito;
import com.ggstore.ms_pedidos.model.CarritoDetalle;
import com.ggstore.ms_pedidos.repository.CarritoDetalleRepository;
import com.ggstore.ms_pedidos.repository.CarritoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CarritoService {

    private final CarritoRepository carritoRepository;
    private final CarritoDetalleRepository carritoDetalleRepository;
    private final JuegoClient juegoClient;

    private Carrito obtenerOCrearCarrito(UUID usuarioId) {
        return carritoRepository.findByUsuarioId(usuarioId)
                .orElseGet(() -> {
                    Carrito nuevo = new Carrito();
                    nuevo.setUsuarioId(usuarioId);
                    return carritoRepository.save(nuevo);
                });
    }

    @Transactional(readOnly = true)
    public CarritoResponse obtenerCarrito(UUID usuarioId) {
        Carrito carrito = obtenerOCrearCarrito(usuarioId);
        return buildCarritoResponse(carrito);
    }

    @Transactional
    public CarritoResponse agregar(UUID usuarioId, AgregarAlCarritoRequest request) {
        Carrito carrito = obtenerOCrearCarrito(usuarioId);

        // Consultamos a ms-catalogo el estado real del juego (precio, stock)
        JuegoDTO juego = juegoClient.obtenerJuego(request.juegoId());

        if (juego.stock() < request.cantidad()) {
            throw new IllegalStateException("Stock insuficiente. Disponible: " + juego.stock());
        }

        CarritoDetalle detalle = carritoDetalleRepository
                .findByCarritoIdAndJuegoId(carrito.getId(), request.juegoId())
                .orElseGet(() -> {
                    CarritoDetalle nuevo = new CarritoDetalle();
                    nuevo.setCarrito(carrito);
                    nuevo.setJuegoId(request.juegoId());
                    nuevo.setCantidad(0);
                    return nuevo;
                });

        int nuevaCantidad = detalle.getCantidad() + request.cantidad();
        if (juego.stock() < nuevaCantidad) {
            throw new IllegalStateException("Stock insuficiente. Disponible: " + juego.stock());
        }

        detalle.setCantidad(nuevaCantidad);
        carritoDetalleRepository.save(detalle);

        return buildCarritoResponse(carrito);
    }

    @Transactional
    public CarritoResponse actualizar(UUID usuarioId, UUID detalleId, int cantidad) {
        CarritoDetalle detalle = carritoDetalleRepository.findById(detalleId)
                .orElseThrow(() -> new RuntimeException("Item no encontrado en el carrito"));

        if (!detalle.getCarrito().getUsuarioId().equals(usuarioId)) {
            throw new RuntimeException("No autorizado");
        }

        if (cantidad <= 0) {
            carritoDetalleRepository.delete(detalle);
        } else {
            JuegoDTO juego = juegoClient.obtenerJuego(detalle.getJuegoId());
            if (juego.stock() < cantidad) {
                throw new IllegalStateException("Stock insuficiente. Disponible: " + juego.stock());
            }
            detalle.setCantidad(cantidad);
            carritoDetalleRepository.save(detalle);
        }

        Carrito carrito = carritoRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new RuntimeException("Carrito no encontrado"));
        return buildCarritoResponse(carrito);
    }

    @Transactional
    public void eliminarItem(UUID usuarioId, UUID detalleId) {
        CarritoDetalle detalle = carritoDetalleRepository.findById(detalleId)
                .orElseThrow(() -> new RuntimeException("Item no encontrado en el carrito"));

        if (!detalle.getCarrito().getUsuarioId().equals(usuarioId)) {
            throw new RuntimeException("No autorizado");
        }

        carritoDetalleRepository.delete(detalle);
    }

    @Transactional
    public void vaciarCarrito(UUID usuarioId) {
        carritoRepository.findByUsuarioId(usuarioId).ifPresent(carrito -> {
            List<CarritoDetalle> items = carritoDetalleRepository.findByCarritoId(carrito.getId());
            carritoDetalleRepository.deleteAll(items);
        });
    }

    private CarritoResponse buildCarritoResponse(Carrito carrito) {
        List<CarritoDetalle> items = carritoDetalleRepository.findByCarritoId(carrito.getId());

        List<CarritoDetalleResponse> detalles = items.stream()
                .map(item -> {
                    JuegoDTO juego = juegoClient.obtenerJuego(item.getJuegoId());
                    BigDecimal subtotal = juego.precioFinal().multiply(BigDecimal.valueOf(item.getCantidad()));
                    return new CarritoDetalleResponse(
                            item.getId(),
                            item.getJuegoId(),
                            juego.titulo(),
                            juego.imagenUrl(),
                            juego.precio(),
                            juego.precioFinal(),
                            item.getCantidad(),
                            subtotal
                    );
                })
                .toList();

        BigDecimal total = detalles.stream()
                .map(CarritoDetalleResponse::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CarritoResponse(carrito.getId(), detalles, total);
    }
}
