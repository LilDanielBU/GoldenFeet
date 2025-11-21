// Archivo: src/main/java/com/GoldenFeet/GoldenFeets/controller/ArchivoController.java
package com.GoldenFeet.GoldenFeets.controller;

import com.GoldenFeet.GoldenFeets.service.AlmacenamientoService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController // Usamos @RestController ya que solo sirve datos
@RequestMapping("/api/imagenes") // Ruta base CLARA y pública
@RequiredArgsConstructor
public class ArchivoController {

    private final AlmacenamientoService almacenamientoService;

    // La URL COMPLETA será: http://localhost:8080/api/imagenes/{nombreArchivo}
    @GetMapping("/{nombreArchivo:.+}")
    public ResponseEntity<Resource> servirArchivo(@PathVariable String nombreArchivo) {
        try {
            Resource archivo = almacenamientoService.cargarComoRecurso(nombreArchivo);
            return ResponseEntity.ok()
                    // Indica que el archivo debe mostrarse 'inline' (en la página)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + archivo.getFilename() + "\"")
                    .body(archivo);
        } catch (Exception e) {
            // Si el archivo no existe, devuelve 404
            return ResponseEntity.notFound().build();
        }
    }
}