package com.ggstore.ms_pedidos.controller;

import com.ggstore.ms_pedidos.dto.AdminStatsResponse;
import com.ggstore.ms_pedidos.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/stats")
    public ResponseEntity<AdminStatsResponse> obtenerStats() {
        return ResponseEntity.ok(adminService.obtenerStats());
    }
}