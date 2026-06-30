package com.ggstore.ms_pedidos.service;

import com.ggstore.ms_pedidos.client.JuegoClient;
import com.ggstore.ms_pedidos.dto.AdminStatsResponse;
import com.ggstore.ms_pedidos.dto.JuegoMasVendidoResponse;
import com.ggstore.ms_pedidos.dto.VentasPorMesResponse;
import com.ggstore.ms_pedidos.model.Pedido;
import com.ggstore.ms_pedidos.model.PedidoDetalle;
import com.ggstore.ms_pedidos.repository.PedidoDetalleRepository;
import com.ggstore.ms_pedidos.repository.PedidoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final PedidoRepository pedidoRepository;
    private final PedidoDetalleRepository pedidoDetalleRepository;
    private final JuegoClient juegoClient;

    @Transactional(readOnly = true)
    public AdminStatsResponse getStats() {
        List<Pedido> pedidos = pedidoRepository.findAll();

        long totalPedidos = pedidos.size();

        BigDecimal ingresosTotales = pedidos.stream()
                .map(Pedido::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Juegos más vendidos
        List<PedidoDetalle> detalles = pedidoDetalleRepository.findAll();
        Map<UUID, Long> cantidadPorJuego = detalles.stream()
                .collect(Collectors.groupingBy(
                        PedidoDetalle::getJuegoId,
                        Collectors.summingLong(d -> d.getCantidad())));

        List<JuegoMasVendidoResponse> juegosMasVendidos = cantidadPorJuego.entrySet().stream()
                .sorted(Map.Entry.<UUID, Long>comparingByValue().reversed())
                .limit(5)
                .map(e -> {
                    String titulo;
                    try {
                        titulo = juegoClient.obtenerJuego(e.getKey()).titulo();
                    } catch (Exception ex) {
                        titulo = e.getKey().toString();
                    }
                    return new JuegoMasVendidoResponse(e.getKey(), titulo, e.getValue());
                })
                .toList();

        // Ventas por mes
        List<VentasPorMesResponse> ventasPorMes = pedidos.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getFecha().getYear() * 100 + p.getFecha().getMonthValue(),
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                lista -> new VentasPorMesResponse(
                                        lista.get(0).getFecha().getMonthValue(),
                                        lista.get(0).getFecha().getYear(),
                                        (long) lista.size(),
                                        lista.stream().map(Pedido::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add)
                                )
                        )
                ))
                .entrySet().stream()
                .sorted(Map.Entry.<Integer, VentasPorMesResponse>comparingByKey().reversed())
                .map(Map.Entry::getValue)
                .toList();

        return new AdminStatsResponse(totalPedidos, ingresosTotales, juegosMasVendidos, ventasPorMes);
    }
}