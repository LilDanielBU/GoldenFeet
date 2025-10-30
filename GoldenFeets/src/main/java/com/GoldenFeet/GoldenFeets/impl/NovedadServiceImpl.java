package com.GoldenFeet.GoldenFeets.impl;

import com.GoldenFeet.GoldenFeets.entity.Novedad;
import com.GoldenFeet.GoldenFeets.repository.NovedadRepository;
import com.GoldenFeet.GoldenFeets.service.NovedadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NovedadServiceImpl implements NovedadService {

    private final NovedadRepository novedadRepository;

    @Override
    @Transactional
    public Novedad guardar(Novedad novedad) {
        return novedadRepository.save(novedad);
    }
}