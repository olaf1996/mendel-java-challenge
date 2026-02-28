package com.mendel.transactions.repository;

import com.mendel.transactions.domain.Transaction;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementación en memoria del repositorio de transacciones.
 * Thread-safe: usa ConcurrentHashMap e índices concurrentes.
 */
@Repository
public class InMemoryTransactionRepository implements TransactionRepository {

    private final ConcurrentHashMap<Long, Transaction> byId = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Set<Long>> byType = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, Set<Long>> parentToChildren = new ConcurrentHashMap<>();

    @Override
    public void save(Transaction transaction) {
        Optional<Transaction> existing = Optional.ofNullable(byId.get(transaction.id()));
        existing.ifPresent(this::removeFromIndices);
        byId.put(transaction.id(), transaction);
        addToIndices(transaction);
    }

    @Override
    public Optional<Transaction> findById(long id) {
        return Optional.ofNullable(byId.get(id));
    }

    @Override
    public List<Long> findIdsByType(String type) {
        Set<Long> ids = byType.get(type);
        return ids == null ? List.of() : new ArrayList<>(ids);
    }

    @Override
    public List<Transaction> findByParentId(long parentId) {
        Set<Long> childIds = parentToChildren.get(parentId);
        if (childIds == null || childIds.isEmpty()) {
            return List.of();
        }
        List<Transaction> result = new ArrayList<>();
        for (Long id : childIds) {
            Transaction t = byId.get(id);
            if (t != null) {
                result.add(t);
            }
        }
        return result;
    }

    private void removeFromIndices(Transaction t) {
        byType.computeIfPresent(t.type(), (k, set) -> {
            set.remove(t.id());
            return set.isEmpty() ? null : set;
        });
        if (t.parentId() != null) {
            parentToChildren.computeIfPresent(t.parentId(), (k, set) -> {
                set.remove(t.id());
                return set.isEmpty() ? null : set;
            });
        }
    }

    private void addToIndices(Transaction t) {
        byType.computeIfAbsent(t.type(), k -> ConcurrentHashMap.newKeySet()).add(t.id());
        if (t.parentId() != null) {
            parentToChildren.computeIfAbsent(t.parentId(), k -> ConcurrentHashMap.newKeySet()).add(t.id());
        }
    }
}
