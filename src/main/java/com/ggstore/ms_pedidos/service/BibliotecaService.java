package com.ggstore.ms_pedidos.service;

import com.ggstore.ms_pedidos.client.JuegoClient;
import com.ggstore.ms_pedidos.client.JuegoDTO;
import com.ggstore.ms_pedidos.dto.BibliotecaResponse;
import com.ggstore.ms_pedidos.repository.BibliotecaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BibliotecaService {

    private final BibliotecaRepository bibliotecaRepository;
    private final JuegoClient juegoClient;

    @Transactional(readOnly = true)
    public List<BibliotecaResponse> obtenerBiblioteca(UUID usuarioId) {
        return bibliotecaRepository.findByUsuarioId(usuarioId)
                .stream()
                .map(b -> {
                    JuegoDTO juego = juegoClient.obtenerJuego(b.getJuegoId());
                    String clave = b.getPedidoDetalle() != null ? b.getPedidoDetalle().getClaveDigital() : null;
                    return new BibliotecaResponse(
                            b.getId(), b.getJuegoId(), juego.titulo(), juego.imagenUrl(),
                            clave, b.getFechaAdquisicion()
                    );
                })
                .toList();
    }
}
