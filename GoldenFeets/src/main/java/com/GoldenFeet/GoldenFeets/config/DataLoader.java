package com.GoldenFeet.GoldenFeets.config;

import com.GoldenFeet.GoldenFeets.entity.Categoria;
// import com.GoldenFeet.GoldenFeets.entity.Inventario; // <-- 1. ELIMINADO
import com.GoldenFeet.GoldenFeets.entity.Producto;
import com.GoldenFeet.GoldenFeets.repository.CategoriaRepository;
// import com.GoldenFeet.GoldenFeets.repository.InventarioRepository; // <-- 2. ELIMINADO
import com.GoldenFeet.GoldenFeets.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    // private final InventarioRepository inventarioRepository; // <-- 3. ELIMINADO

    @Override
    public void run(String... args) throws Exception {
        // Esta condición evita que los datos se dupliquen cada vez que reinicias la aplicación
        if (categoriaRepository.count() == 0 && productoRepository.count() == 0) {
            System.out.println("Base de datos vacía. Cargando datos de prueba...");

            // --- Crear y Guardar Categorías ---
            Categoria hombre = new Categoria();
            hombre.setNombre("Hombre");
            hombre.setDescripcion("Calzado de alta calidad para hombres.");
            // Asumimos que Categoria también cambió a 'imagenNombre'
            hombre.setImagenNombre("https://i.pinimg.com/736x/de/70/b5/de70b5917ab1ddb0b0de8ba0d4974abe.jpg");

            Categoria mujer = new Categoria();
            mujer.setNombre("Mujer");
            mujer.setDescripcion("Estilo y confort para el pie femenino.");
            mujer.setImagenNombre("https://i.pinimg.com/736x/1b/04/05/1b040589e2335d668eeddb51a3c2173c.jpg");

            Categoria ninos = new Categoria();
            ninos.setNombre("Niños");
            ninos.setDescripcion("Calzado duradero y divertido para los más pequeños.");
            ninos.setImagenNombre("https://i.pinimg.com/736x/d0/8c/07/d08c071136d14f88c1e943d6fbaed59f.jpg");

            categoriaRepository.saveAll(List.of(hombre, mujer, ninos));


            // --- 6. Lógica de inventario ELIMINADA ---
            // Ya no es necesaria, el stock está en la tabla productos.

            System.out.println("¡Datos de prueba cargados correctamente!");
        }
    }
}