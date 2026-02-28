package com.mendel.transactions.service;

import com.mendel.transactions.api.dto.TransactionRequest;
import com.mendel.transactions.domain.Transaction;
import com.mendel.transactions.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TransactionService {

    private final TransactionRepository repository;

    public TransactionService(TransactionRepository repository) {
        this.repository = repository;
    }

    /**
     * Crea o actualiza una transacción por id.
     * Validación de parent_id y ciclos se agrega en paso 11.
     */
    public void save(long transactionId, TransactionRequest request) {
        Transaction transaction = new Transaction(
                transactionId,
                request.amount(),
                request.type(),
                request.parentId()
        );
        repository.save(transaction);
    }

    /**
     * IDs de todas las transacciones del tipo dado.
     */
    public List<Long> findIdsByType(String type) {
        return repository.findIdsByType(type);
    }
}
