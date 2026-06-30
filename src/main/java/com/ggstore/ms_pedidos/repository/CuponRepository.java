package com.ggstore.ms_pedidos.repository;

import com.ggstore.ms_pedidos.model.Cupon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CuponRepository extends JpaRepository<Cupon, UUID> {
    Optional<Cupon> findByCodigoAndActivoTrue(String codigo);
}
