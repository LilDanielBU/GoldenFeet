package com.GoldenFeet.GoldenFeets.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Excepción personalizada para indicar que un recurso solicitado no fue encontrado.
 * Cuando se lanza desde un Controller, Spring Boot devolverá automáticamente
 * una respuesta HTTP 404 (Not Found) con el mensaje proporcionado.
 */
@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    /**
     * Constructor que acepta un mensaje de error.
     * @param message El mensaje que detalla el error.
     */
    public ResourceNotFoundException(String message) {
        // Se pasa el mensaje al constructor de la clase padre (RuntimeException)
        super(message);
    }
}