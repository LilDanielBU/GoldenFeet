package com.GoldenFeet.GoldenFeets.impl;

import com.GoldenFeet.GoldenFeets.dto.CategoriaDTO;
import com.GoldenFeet.GoldenFeets.entity.Categoria;
import com.GoldenFeet.GoldenFeets.repository.CategoriaRepository;
import com.GoldenFeet.GoldenFeets.service.CategoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder; // Importante para construir URLs

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
        // 1. Obtenemos el nombre o URL crudo desde la base de datos
        String imagen = categoria.getImagenNombre();
        String imagenUrlFinal = null;

        // 2. Lógica inteligente: ¿Es una URL de internet o un archivo local?
        if (imagen != null && !imagen.isEmpty()) {
            if (imagen.startsWith("http://") || imagen.startsWith("https://")) {
                // Es una URL externa (ej: Pinterest), se deja tal cual
                imagenUrlFinal = imagen;
            } else {
                // Es un archivo local subido, construimos la URL completa del servidor
                imagenUrlFinal = ServletUriComponentsBuilder
                        .fromCurrentContextPath()
                        .path("/api/imagenes/")
                        .path(imagen)
                        .toUriString();
            }
        }

        return new CategoriaDTO(
                categoria.getIdCategoria(),
                categoria.getNombre(),
                categoria.getDescripcion(),
                imagenUrlFinal // Pasamos la URL procesada
        );
    }
}