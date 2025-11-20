package com.GoldenFeet.GoldenFeets.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Path;

public interface AlmacenamientoService {

    void init(); // Método para crear el directorio

    String almacenarArchivo(MultipartFile archivo);

    Path cargar(String nombreArchivo);

    Resource cargarComoRecurso(String nombreArchivo);

    void eliminarArchivo(String nombreArchivo);
}