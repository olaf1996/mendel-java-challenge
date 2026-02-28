package com.mendel.transactions.api.exception;

/**
 * Lanzada por validaciones de negocio (parent inexistente, ciclo, etc.).
 * Mapeada a 400 Bad Request.
 */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
