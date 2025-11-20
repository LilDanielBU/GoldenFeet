package com.GoldenFeet.GoldenFeets.impl;

import com.GoldenFeet.GoldenFeets.dto.CategoriaDTO;
import com.GoldenFeet.GoldenFeets.entity.Categoria;
import com.GoldenFeet.GoldenFeets.repository.CategoriaRepository;
import com.GoldenFeet.GoldenFeets.service.CategoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoriaServiceImpl implements CategoriaService {

    private final CategoriaRepository categoriaRepository;

    @Override
    public List<CategoriaDTO> listarTodas() {
        return categoriaRepository.findAll().stream()
                .map(this::convertirACategoriaDTO)
                .collect(Collectors.toList());
    }

    private CategoriaDTO convertirACategoriaDTO(Categoria categoria) {

        String imagenNombre = categoria.getImagenNombre();
        String imagenUrlFinal = null;

        if (imagenNombre != null && !imagenNombre.isEmpty()) {
            // 1. Comprueba si es una URL externa (de DataLoader)
            if (imagenNombre.startsWith("http://") || imagenNombre.startsWith("https://")) {
                imagenUrlFinal = imagenNombre;
            } else {
                // 2. Si es un archivo local, construye la URL pública
                imagenUrlFinal = ServletUriComponentsBuilder
                        .fromCurrentContextPath()
                        .path("/api/imagenes/") // <-- RUTA PÚBLICA CORREGIDA
                        .path(imagenNombre)
                        .toUriString();
            }
        }

        return new CategoriaDTO(
                // Asumiendo que el DTO espera Integer
                categoria.getIdCategoria().intValue(),
                categoria.getNombre(),
                categoria.getDescripcion(),
                imagenUrlFinal
        );
    }
}