package com.GoldenFeet.GoldenFeets.config; // Asegúrate de que este paquete coincida con el de tus otros archivos en esa carpeta

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Esto le dice a Spring: "Cuando alguien pida /images/nombre-foto.jpg,
        // búscalo en la carpeta 'uploads' del disco duro".

        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:./uploads/");

        // NOTA: El "./" significa "en la carpeta raíz del proyecto".
        // Si tus fotos están en otro lado, avísame para darte la ruta correcta.
    }
}