package com.ggstore.ms_pedidos.controller;

import com.ggstore.ms_pedidos.dto.BibliotecaResponse;
import com.ggstore.ms_pedidos.service.BibliotecaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/biblioteca")
@RequiredArgsConstructor
public class BibliotecaController {

    private final BibliotecaService bibliotecaService;

    @GetMapping
    public ResponseEntity<List<BibliotecaResponse>> obtener(@RequestHeader("X-Usuario-Id") UUID usuarioId) {
        return ResponseEntity.ok(bibliotecaService.obtenerBiblioteca(usuarioId));
    }
}
