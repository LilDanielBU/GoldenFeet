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
    @Value("${storage.location}")
    private String storageLocation;

    private Path rootLocation;

    @Override
    @PostConstruct // Se ejecuta después de construir el objeto
    public void init() {
        // --- CORRECCIÓN CLAVE ---
        // Asegura que la ruta sea absoluta y se normalice, evitando problemas de directorio.
        // Si storageLocation es "uploads", esto resuelve la ruta desde la raíz del proyecto.
        rootLocation = Paths.get(storageLocation).toAbsolutePath().normalize();

        try {
            // Se usa createDirectories para crear la carpeta si no existe
            Files.createDirectories(rootLocation);
            System.out.println("Ruta de almacenamiento inicializada: " + rootLocation.toString());
        } catch (IOException e) {
            throw new RuntimeException("No se pudo inicializar el almacenamiento de archivos en: " + rootLocation.toString(), e);
        }
    }

    @Override
    public String almacenarArchivo(MultipartFile archivo) {
        if (archivo.isEmpty()) {
            // Se puede optar por devolver null o lanzar una excepción, aquí se mantiene la excepción.
            throw new RuntimeException("No se puede almacenar un archivo vacío.");
        }

        // Generamos un nombre único para evitar colisiones y mantener la extensión
        String nombreOriginal = archivo.getOriginalFilename();
        // Evita NullPointerException si nombreOriginal es null o vacío
        String extension = (nombreOriginal != null && nombreOriginal.contains("."))
                ? nombreOriginal.substring(nombreOriginal.lastIndexOf("."))
                : "";

        String nombreUnico = UUID.randomUUID().toString() + extension;

        try (InputStream inputStream = archivo.getInputStream()) {
            Path archivoDestino = rootLocation.resolve(nombreUnico);
            Files.copy(inputStream, archivoDestino, StandardCopyOption.REPLACE_EXISTING);
            return nombreUnico; // Devolvemos el nombre único
        } catch (IOException e) {
            throw new RuntimeException("Falló al almacenar el archivo.", e);
        }
    }

    @Override
    public Path cargar(String nombreArchivo) {
        // Cargar busca el archivo dentro de la ruta raíz
        return rootLocation.resolve(nombreArchivo);
    }

    @Override
    public Resource cargarComoRecurso(String nombreArchivo) {
        try {
            Path archivo = cargar(nombreArchivo);
            Resource recurso = new UrlResource(archivo.toUri());

            if (recurso.exists() && recurso.isReadable()) { // Usamos AND para asegurar ambas
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
            // Solo imprimimos el error, no lanzamos una excepción fatal si falla la eliminación
            System.err.println("Advertencia: Error al eliminar el archivo: " + nombreArchivo + " " + e.getMessage());
        }
    }
}