
package com.GoldenFeet.GoldenFeets;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync; // <--- Importante

@SpringBootApplication
@EnableAsync // <--- Agrega esto aquí
public class GoldenFeetsApplication {
    public static void main(String[] args) {
        SpringApplication.run(GoldenFeetsApplication.class, args);
    }
}