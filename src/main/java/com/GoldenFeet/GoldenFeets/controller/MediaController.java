package com.GoldenFeet.GoldenFeets.controller;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File; // Importación necesaria
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/imagenes")
public class MediaController {

    // Usamos la ruta exacta que reporta tu log, reemplazando '/' por el separador nativo
    // Nota: Mantenemos el String para que el Paths.get lo maneje, pero aseguramos la consistencia.
    private final Path rutaAlmacenamiento = Paths.get("C:" + File.separator + "tmp" + File.separator + "goldenfeet-uploads");
    // O simplemente: private final Path rutaAlmacenamiento = Paths.get("C:\\tmp\\goldenfeet-uploads");

    @GetMapping("/{filename:.+}")
    public ResponseEntity<Resource> servirImagen(@PathVariable String filename) {
        if (filename == null || filename.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        try {
            Path archivo = rutaAlmacenamiento.resolve(filename);
            Resource recurso = new UrlResource(archivo.toUri());

            // CORRECCIÓN CLAVE: El archivo debe EXISTIR Y ser LEGIBLE
            if (recurso.exists() && recurso.isReadable()) {

                String contentType = "image/jpeg";
                if (filename.toLowerCase().endsWith(".png")) contentType = "image/png";
                if (filename.toLowerCase().endsWith(".gif")) contentType = "image/gif"; // Añadir otros tipos si es necesario

                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .body(recurso);
            } else {
                // Si el archivo no existe o no es legible (Permisos/Nombre de archivo incorrecto)
                System.err.println("IMAGEN NO ENCONTRADA EN DISCO: " + archivo.toString());
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            System.err.println("ERROR MEDIA CONTROLLER: URL mal formada para archivo: " + filename);
            return ResponseEntity.badRequest().build();
        }
    }
}