package com.GoldenFeet.GoldenFeets.repository;

import com.GoldenFeet.GoldenFeets.entity.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Integer> {

    /**
     * Busca todas las ventas asociadas a un cliente por el ID del usuario.
     * Spring Data JPA entiende "findByCliente_IdUsuario" y navega a través
     * de la entidad Venta -> Usuario -> idUsuario.
     *
     * @param idUsuario el ID del usuario cliente.
     * @return una lista de ventas de ese cliente.
     */
    List<Venta> findByCliente_IdUsuario(Integer idUsuario);
}