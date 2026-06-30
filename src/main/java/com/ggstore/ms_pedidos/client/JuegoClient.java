package com.ggstore.ms_pedidos.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.HttpClientErrorException;

import java.util.UUID;

@Component
public class JuegoClient {

    private final RestClient restClient;

    public JuegoClient(@Value("${ms-catalogo.url}") String msCatalogoUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(msCatalogoUrl)
                .build();
    }

    /**
     * Pide a ms-catalogo los datos de un juego (precio, titulo, stock, etc).
     * Lanza RuntimeException si el juego no existe (404).
     */
    public JuegoDTO obtenerJuego(UUID juegoId) {
        try {
            return restClient.get()
                    .uri("/juegos/{id}", juegoId)
                    .retrieve()
                    .body(JuegoDTO.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new RuntimeException("Juego no encontrado en el catálogo");
        }
    }

    /**
     * Le pide a ms-catalogo que descuente stock tras una compra confirmada.
     * Requiere el endpoint PATCH /juegos/{id}/stock en ms-catalogo (ver instrucciones).
     */
    public void descontarStock(UUID juegoId, int cantidad) {
        restClient.patch()
                .uri("/juegos/{id}/stock?cantidad={cantidad}", juegoId, cantidad)
                .retrieve()
                .toBodilessEntity();
    }
}
