package com.GoldenFeet.GoldenFeets.impl;

import com.GoldenFeet.GoldenFeets.entity.Rol;
import com.GoldenFeet.GoldenFeets.repository.RolRepository;
import com.GoldenFeet.GoldenFeets.service.RolService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RolServiceImpl implements RolService {

    private final RolRepository rolRepository;

    @Override
    public List<Rol> listarTodosLosRoles() {
        return rolRepository.findAll();
    }
}