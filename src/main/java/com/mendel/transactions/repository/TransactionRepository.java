package com.mendel.transactions.repository;

import com.mendel.transactions.domain.Transaction;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de persistencia de transacciones (DIP).
 * La implementación puede ser en memoria, base de datos, etc.
 */
public interface TransactionRepository {

    void save(Transaction transaction);

    Optional<Transaction> findById(long id);

    /**
     * IDs de todas las transacciones del tipo dado, en orden de inserción no garantizado.
     */
    List<Long> findIdsByType(String type);

    /**
     * Transacciones cuyo parent_id es el indicado (hijos directos).
     * Usado por el servicio para calcular la suma transitiva.
     */
    List<Transaction> findByParentId(long parentId);
}
