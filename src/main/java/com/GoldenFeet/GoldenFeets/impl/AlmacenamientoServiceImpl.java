package com.GoldenFeet.GoldenFeets.impl;

import com.GoldenFeet.GoldenFeets.service.AlmacenamientoService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class AlmacenamientoServiceImpl implements AlmacenamientoService {

    // Valor inyectado desde application.properties (ej: storage.location=uploads)
    // Para que funcione con C:\tmp\goldenfeet-uploads, debes tener:
    // storage.location=C:/tmp/goldenfeet-uploads
    @Value("${storage.location}")
    private String storageLocation;

    private Path rootLocation;

    @Override
    @PostConstruct // Se ejecuta después de construir el objeto
    public void init() {
        // CORRECCIÓN CLAVE:
        // 1. Usamos Paths.get(storageLocation) para tomar la ruta configurada (C:/tmp/goldenfeet-uploads).
        // 2. Usamos .normalize() para resolver rutas relativas o inconsistentes, asegurando una ruta limpia.
        // 3. Ya NO es necesario usar .toAbsolutePath() si la ruta en properties es absoluta (C:/...).
        // Si la ruta en properties es relativa (ej: "uploads"), entonces toAbsolutePath() sí es vital.

        try {
            this.rootLocation = Paths.get(storageLocation).normalize();

            // Se usa createDirectories para crear la carpeta si no existe (soluciona el problema de "no se crea la carpeta")
            Files.createDirectories(this.rootLocation);
            System.out.println("✅ Ruta de almacenamiento inicializada: " + this.rootLocation.toAbsolutePath().toString());
        } catch (IOException e) {
            System.err.println("❌ ERROR: No se pudo inicializar el almacenamiento de archivos. Verifique que la ruta exista o los permisos de Windows.");
            // Lanzamos la excepción para detener el inicio de la app si el almacenamiento falla.
            throw new RuntimeException("No se pudo inicializar el almacenamiento de archivos en: " + storageLocation, e);
        }
    }

    @Override
    public String almacenarArchivo(MultipartFile archivo) {
        if (archivo.isEmpty()) {
            // Manejar un archivo vacío devolviendo null en lugar de lanzar una excepción,
            // permitiendo que el ProductoService decida si es un error.
            return null;
        }

        // Generamos un nombre único para evitar colisiones y mantener la extensión
        String nombreOriginal = archivo.getOriginalFilename();

        // CRÍTICO: Manejo seguro de la extensión para evitar NullPointer y errores de substring.
        String extension = "";
        if (nombreOriginal != null && nombreOriginal.lastIndexOf(".") != -1) {
            extension = nombreOriginal.substring(nombreOriginal.lastIndexOf("."));
        }

        String nombreUnico = UUID.randomUUID().toString() + extension;

        try (InputStream inputStream = archivo.getInputStream()) {
            Path archivoDestino = this.rootLocation.resolve(nombreUnico);

            // CORRECCIÓN: Usamos this.rootLocation.resolve(nombreUnico) para la ruta de destino
            Files.copy(inputStream, archivoDestino, StandardCopyOption.REPLACE_EXISTING);

            return nombreUnico; // Devolvemos el nombre único
        } catch (IOException e) {
            System.err.println("❌ ERROR al copiar el archivo al disco.");
            throw new RuntimeException("Falló al almacenar el archivo.", e);
        }
    }

    @Override
    public Path cargar(String nombreArchivo) {
        // Cargar busca el archivo dentro de la ruta raíz
        return this.rootLocation.resolve(nombreArchivo);
    }

    @Override
    public Resource cargarComoRecurso(String nombreArchivo) {
        try {
            Path archivo = cargar(nombreArchivo);
            Resource recurso = new UrlResource(archivo.toUri());

            if (recurso.exists() && recurso.isReadable()) {
                return recurso;
            } else {
                // Lanzamos una excepción para que el controlador pueda manejar el 404
                throw new RuntimeException("El archivo no existe o no es legible: " + nombreArchivo);
            }
        } catch (MalformedURLException e) {
            // Manejamos específicamente errores de URL
            throw new RuntimeException("Error en la URL del recurso para: " + nombreArchivo, e);
        }
    }

    @Override
    public void eliminarArchivo(String nombreArchivo) {
        if (nombreArchivo == null || nombreArchivo.isEmpty()) {
            return;
        }
        try {
            Path archivo = cargar(nombreArchivo);
            Files.deleteIfExists(archivo);
        } catch (IOException e) {
            // Solo imprimimos el error si falla la eliminación (Advertencia)
            System.err.println("Advertencia: Error al eliminar el archivo: " + nombreArchivo + " " + e.getMessage());
        }
    }
}