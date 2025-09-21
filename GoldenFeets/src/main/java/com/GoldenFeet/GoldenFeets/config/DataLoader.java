package com.GoldenFeet.GoldenFeets.config;

import com.GoldenFeet.GoldenFeets.entity.Categoria;
import com.GoldenFeet.GoldenFeets.entity.Producto;
import com.GoldenFeet.GoldenFeets.repository.CategoriaRepository;
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

    @Override
    public void run(String... args) throws Exception {
        // Esta condición evita que los datos se dupliquen cada vez que reinicias la aplicación
        if (categoriaRepository.count() == 0 && productoRepository.count() == 0) {
            System.out.println("Base de datos vacía. Cargando datos de prueba...");

            // --- Crear y Guardar Categorías ---
            Categoria hombre = new Categoria();
            hombre.setNombre("Hombre");
            hombre.setDescripcion("Calzado de alta calidad para hombres.");

            Categoria mujer = new Categoria();
            mujer.setNombre("Mujer");
            mujer.setDescripcion("Estilo y confort para el pie femenino.");

            Categoria ninos = new Categoria();
            ninos.setNombre("Niños");
            ninos.setDescripcion("Calzado duradero y divertido para los más pequeños.");

            // Guardamos las categorías para que obtengan un ID
            categoriaRepository.saveAll(List.of(hombre, mujer, ninos));

            // --- Crear Productos ---
            Producto p1 = new Producto();
            p1.setNombre("Zapato Casual ");
            p1.setDescripcion("Zapato casual para hombre, perfecto para eventos casuales.");
            p1.setPrecio(new BigDecimal("299900"));
            p1.setStock(50);
            p1.setCategoria(hombre);
            p1.setImagenUrl("https://i.pinimg.com/736x/de/70/b5/de70b5917ab1ddb0b0de8ba0d4974abe.jpg");
            p1.setDestacado(true); // Aparecerá en el carrusel
            p1.setRating(4.5);

            Producto p2 = new Producto();
            p2.setNombre("Zapatilla Dunk");
            p2.setDescripcion("Zapatilla para hombre que ofrecen el aspecto icónico del Dunk con un estilo de perfil bajo.");
            p2.setPrecio(new BigDecimal("450000"));
            p2.setStock(30);
            p2.setCategoria(hombre);
            p2.setImagenUrl("https://i.pinimg.com/736x/91/64/db/9164db9be79f3cfb9ce97854d09455ae.jpg");
            p2.setDestacado(true); // Aparecerá en el carrusel
            p2.setRating(4.8);

            Producto p3 = new Producto();
            p3.setNombre("Zapatilla Running Pro ");
            p3.setDescripcion("Zapatilla profesional para running con máxima amortiguación.");
            p3.setPrecio(new BigDecimal("250000"));
            p3.setStock(40);
            p3.setCategoria(mujer);
            p3.setImagenUrl("https://i.pinimg.com/736x/1b/04/05/1b040589e2335d668eeddb51a3c2173c.jpg");
            p3.setDestacado(true); // Aparecerá en el carrusel
            p3.setRating(4.6);

            Producto p4 = new Producto();
            p4.setNombre("Zapato Infantil");
            p4.setDescripcion("Zapato cómodo y resistente para las aventuras diarias de los niños.");
            p4.setPrecio(new BigDecimal("120000"));
            p4.setStock(100);
            p4.setCategoria(ninos);
            p4.setImagenUrl("https://i.pinimg.com/736x/d0/8c/07/d08c071136d14f88c1e943d6fbaed59f.jpg");
            p4.setDestacado(false); // No aparecerá en el carrusel
            p4.setRating(4.2);

            // Guardamos todos los productos a la vez
            productoRepository.saveAll(List.of(p1, p2, p3, p4));

            System.out.println("¡Datos de prueba cargados!");
        }
    }
}