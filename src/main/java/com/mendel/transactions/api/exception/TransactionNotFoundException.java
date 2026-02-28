package com.mendel.transactions.api.exception;

/**
 * Lanzada cuando se solicita una transacción por id que no existe (ej. GET sum).
 * Mapeada a 404 Not Found.
 */
public class TransactionNotFoundException extends RuntimeException {

    public TransactionNotFoundException(long transactionId) {
        super("Transaction not found: " + transactionId);
    }
}
