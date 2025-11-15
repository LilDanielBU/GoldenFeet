package com.GoldenFeet.GoldenFeets.service;

import com.GoldenFeet.GoldenFeets.entity.Novedad;

public interface NovedadService {

    /**
     * Guarda una nueva novedad en la base de datos.
     * @param novedad La entidad Novedad a guardar.
     * @return La entidad Novedad guardada.
     */
    Novedad guardar(Novedad novedad);
}