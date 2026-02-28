package com.mendel.transactions.service;

import com.mendel.transactions.api.exception.TransactionNotFoundException;
import com.mendel.transactions.api.dto.TransactionRequest;
import com.mendel.transactions.domain.Transaction;
import com.mendel.transactions.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TransactionService {

    private final TransactionRepository repository;

    public TransactionService(TransactionRepository repository) {
        this.repository = repository;
    }

    /**
     * Crea o actualiza una transacción por id.
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

    /**
     * Suma transitiva: monto de la transacción más todos sus descendientes por parent_id.
     * @throws TransactionNotFoundException si el id no existe
     */
    public double getSum(long transactionId) {
        Transaction root = repository.findById(transactionId)
                .orElseThrow(() -> new TransactionNotFoundException(transactionId));
        double sum = root.amount();
        List<Long> currentLevel = repository.findByParentId(transactionId).stream()
                .map(Transaction::id)
                .toList();
        while (!currentLevel.isEmpty()) {
            List<Long> nextLevel = new ArrayList<>();
            for (Long id : currentLevel) {
                repository.findById(id).ifPresent(t -> {
                    sum += t.amount();
                    nextLevel.addAll(repository.findByParentId(id).stream()
                            .map(Transaction::id)
                            .toList());
                });
            }
            currentLevel = nextLevel;
        }
        return sum;
    }
}
