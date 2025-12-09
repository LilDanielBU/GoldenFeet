package com.GoldenFeet.GoldenFeets.repository;

import com.GoldenFeet.GoldenFeets.entity.InventarioMovimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface InventarioMovimientoRepository extends JpaRepository<InventarioMovimiento, Long> {


    List<InventarioMovimiento> findByVariante_Id(Long varianteId);

    // --- CORRECCIÓN: Método para eliminar movimientos por ID de Variante ---
    @Modifying
    @Transactional
    void deleteByVarianteId(Long varianteId);
}