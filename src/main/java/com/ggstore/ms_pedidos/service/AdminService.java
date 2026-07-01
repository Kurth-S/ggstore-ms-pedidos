package com.ggstore.ms_pedidos.service;

import com.ggstore.ms_pedidos.dto.AdminStatsResponse;
import com.ggstore.ms_pedidos.repository.PedidoDetalleRepository;
import com.ggstore.ms_pedidos.repository.PedidoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final PedidoRepository pedidoRepository;
    private final PedidoDetalleRepository pedidoDetalleRepository;
    private final RestTemplate restTemplate;

    @Value("${ms-catalogo.url}")
    private String msCatalogoUrl;

    public AdminStatsResponse obtenerStats() {
        Long totalPedidos = pedidoRepository.countPedidosPagados();
        BigDecimal ingresosTotales = pedidoRepository.sumIngresosTotales();

        List<AdminStatsResponse.JuegoVendidoDTO> juegosMasVendidos = new ArrayList<>();
        for (Object[] row : pedidoDetalleRepository.findTopJuegosVendidos()) {
            UUID juegoId = UUID.fromString(row[0].toString());
            Long cantidadVendida = ((Number) row[1]).longValue();
            String titulo = obtenerTituloJuego(juegoId);
            juegosMasVendidos.add(new AdminStatsResponse.JuegoVendidoDTO(juegoId, titulo, cantidadVendida));
        }

        List<AdminStatsResponse.VentasMesDTO> ventasPorMes = new ArrayList<>();
        for (Object[] row : pedidoRepository.findVentasPorMes()) {
            Integer mes = ((Number) row[0]).intValue();
            Integer anio = ((Number) row[1]).intValue();
            Long totalMes = ((Number) row[2]).longValue();
            BigDecimal ingresosMes = new BigDecimal(row[3].toString());
            ventasPorMes.add(new AdminStatsResponse.VentasMesDTO(mes, anio, totalMes, ingresosMes));
        }

        return new AdminStatsResponse(totalPedidos, ingresosTotales, juegosMasVendidos, ventasPorMes);
    }

    private String obtenerTituloJuego(UUID juegoId) {
        try {
            Map juego = restTemplate.getForObject(
                    msCatalogoUrl + "/juegos/" + juegoId, Map.class);
            return juego != null ? juego.get("titulo").toString() : juegoId.toString();
        } catch (Exception e) {
            return juegoId.toString();
        }
    }
}