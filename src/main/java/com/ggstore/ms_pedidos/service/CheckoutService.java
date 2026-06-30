package com.ggstore.ms_pedidos.service;

import com.ggstore.ms_pedidos.client.JuegoClient;
import com.ggstore.ms_pedidos.client.JuegoDTO;
import com.ggstore.ms_pedidos.dto.CheckoutRequest;
import com.ggstore.ms_pedidos.dto.PedidoDetalleResponse;
import com.ggstore.ms_pedidos.dto.PedidoResponse;
import com.ggstore.ms_pedidos.model.*;
import com.ggstore.ms_pedidos.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CheckoutService {

    private final CarritoRepository carritoRepository;
    private final CarritoDetalleRepository carritoDetalleRepository;
    private final PedidoRepository pedidoRepository;
    private final PedidoDetalleRepository pedidoDetalleRepository;
    private final CuponRepository cuponRepository;
    private final BibliotecaRepository bibliotecaRepository;
    private final JuegoClient juegoClient;

    @Transactional
    public PedidoResponse checkout(UUID usuarioId, CheckoutRequest request) {

        Carrito carrito = carritoRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new RuntimeException("El carrito está vacío"));

        List<CarritoDetalle> items = carritoDetalleRepository.findByCarritoId(carrito.getId());
        if (items.isEmpty()) {
            throw new RuntimeException("El carrito está vacío");
        }

        // 1. Verificar stock (vía ms-catalogo) y calcular total
        BigDecimal total = BigDecimal.ZERO;
        List<JuegoDTO> juegosInfo = new ArrayList<>();

        for (CarritoDetalle item : items) {
            JuegoDTO juego = juegoClient.obtenerJuego(item.getJuegoId());
            if (juego.stock() < item.getCantidad()) {
                throw new IllegalStateException(
                        "Stock insuficiente para: " + juego.titulo() + ". Disponible: " + juego.stock());
            }
            juegosInfo.add(juego);
            total = total.add(juego.precioFinal().multiply(BigDecimal.valueOf(item.getCantidad())));
        }

        // 2. Aplicar cupón si corresponde
        Cupon cupon = null;
        if (request.codigoCupon() != null && !request.codigoCupon().isBlank()) {
            cupon = cuponRepository.findByCodigoAndActivoTrue(request.codigoCupon())
                    .orElseThrow(() -> new RuntimeException("Cupón inválido o expirado"));

            if (cupon.getFechaExpiracion() != null && cupon.getFechaExpiracion().isBefore(OffsetDateTime.now())) {
                throw new RuntimeException("El cupón ha expirado");
            }

            BigDecimal descuentoCupon = cupon.getPorcentajeDescuento().divide(BigDecimal.valueOf(100));
            total = total.multiply(BigDecimal.ONE.subtract(descuentoCupon));
        }

        // 3. Crear el pedido
        Pedido pedido = new Pedido();
        pedido.setUsuarioId(usuarioId);
        pedido.setFecha(OffsetDateTime.now());
        pedido.setTotal(total);
        pedido.setEstado("PAGADO");
        pedido.setCupon(cupon);
        pedidoRepository.save(pedido);

        // 4. Crear detalles, descontar stock en ms-catalogo, generar claves y agregar a biblioteca
        List<PedidoDetalle> detalles = new ArrayList<>();

        for (int i = 0; i < items.size(); i++) {
            CarritoDetalle item = items.get(i);
            JuegoDTO juego = juegosInfo.get(i);

            juegoClient.descontarStock(item.getJuegoId(), item.getCantidad());

            String claveDigital = "GG-" + item.getJuegoId().toString().substring(0, 8).toUpperCase()
                    + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

            PedidoDetalle detalle = new PedidoDetalle();
            detalle.setPedido(pedido);
            detalle.setJuegoId(item.getJuegoId());
            detalle.setPrecio(juego.precioFinal());
            detalle.setCantidad(item.getCantidad());
            detalle.setClaveDigital(claveDigital);
            pedidoDetalleRepository.save(detalle);
            detalles.add(detalle);

            if (!bibliotecaRepository.existsByUsuarioIdAndJuegoId(usuarioId, item.getJuegoId())) {
                Biblioteca entrada = new Biblioteca();
                entrada.setUsuarioId(usuarioId);
                entrada.setJuegoId(item.getJuegoId());
                entrada.setPedidoDetalle(detalle);
                bibliotecaRepository.save(entrada);
            }
        }

        // 5. Vaciar carrito
        carritoDetalleRepository.deleteAll(items);

        return toPedidoResponse(pedido, detalles);
    }

    @Transactional(readOnly = true)
    public List<PedidoResponse> historial(UUID usuarioId) {
        return pedidoRepository.findByUsuarioIdOrderByFechaDesc(usuarioId)
                .stream()
                .map(pedido -> {
                    List<PedidoDetalle> detalles = pedidoDetalleRepository.findByPedidoId(pedido.getId());
                    return toPedidoResponse(pedido, detalles);
                })
                .toList();
    }

    private PedidoResponse toPedidoResponse(Pedido pedido, List<PedidoDetalle> detalles) {
        List<PedidoDetalleResponse> items = detalles.stream()
                .map(d -> {
                    JuegoDTO juego = juegoClient.obtenerJuego(d.getJuegoId());
                    return new PedidoDetalleResponse(
                            d.getId(), d.getJuegoId(), juego.titulo(), juego.imagenUrl(),
                            d.getPrecio(), d.getCantidad(), d.getClaveDigital()
                    );
                })
                .toList();

        return new PedidoResponse(pedido.getId(), pedido.getFecha(), pedido.getTotal(), pedido.getEstado(), items);
    }
}
