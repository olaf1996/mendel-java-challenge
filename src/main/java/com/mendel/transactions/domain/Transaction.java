package com.mendel.transactions.domain;

/**
 * Entidad de dominio que representa una transacción.
 * Inmutable (record); parentId es opcional para transacciones raíz.
 */
public record Transaction(
        long id,
        double amount,
        String type,
        Long parentId
) {
    public Transaction {
        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException("type must not be null or blank");
        }
    }
}
