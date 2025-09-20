package com.GoldenFeet.GoldenFeets.repository;

import com.GoldenFeet.GoldenFeets.entity.Entrega;
import com.GoldenFeet.GoldenFeets.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EntregaRepository extends JpaRepository<Entrega, Long> {

    List<Entrega> findByDistribuidor(Usuario distribuidor);
}