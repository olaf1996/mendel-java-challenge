package com.mendel.transactions.service;

import com.mendel.transactions.api.exception.BadRequestException;
import com.mendel.transactions.api.exception.TransactionNotFoundException;
import com.mendel.transactions.api.dto.TransactionRequest;
import com.mendel.transactions.domain.Transaction;
import com.mendel.transactions.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class TransactionService {

    private final TransactionRepository repository;

    public TransactionService(TransactionRepository repository) {
        this.repository = repository;
    }

    /**
     * Crea o actualiza una transacción por id.
     * Rechaza si parent_id no existe o si se formaría un ciclo.
     */
    public void save(long transactionId, TransactionRequest request) {
        Long parentId = request.parentId();
        if (parentId != null) {
            if (repository.findById(parentId).isEmpty()) {
                throw new BadRequestException("Parent transaction does not exist: " + parentId);
            }
            validateNoCycle(transactionId, parentId);
        }
        Transaction transaction = new Transaction(
                transactionId,
                request.amount(),
                request.type(),
                parentId
        );
        repository.save(transaction);
    }

    private void validateNoCycle(long transactionId, long parentId) {
        Long current = parentId;
        while (current != null) {
            if (current == transactionId) {
                throw new BadRequestException("Cycle detected: parent_id would create a cycle");
            }
            Transaction t = repository.findById(current).orElse(null);
            current = t != null ? t.parentId() : null;
        }
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
                Optional<Transaction> opt = repository.findById(id);
                if (opt.isPresent()) {
                    Transaction t = opt.get();
                    sum += t.amount();
                    nextLevel.addAll(repository.findByParentId(id).stream()
                            .map(Transaction::id)
                            .toList());
                }
            }
            currentLevel = nextLevel;
        }
        return sum;
    }
}
