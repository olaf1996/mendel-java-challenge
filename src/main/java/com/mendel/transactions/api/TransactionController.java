package com.mendel.transactions.api;

import com.mendel.transactions.api.dto.StatusResponse;
import com.mendel.transactions.api.dto.TransactionRequest;
import com.mendel.transactions.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PutMapping("/{transactionId}")
    public ResponseEntity<StatusResponse> putTransaction(
            @PathVariable long transactionId,
            @RequestBody @Valid TransactionRequest request) {
        transactionService.save(transactionId, request);
        return ResponseEntity.ok(StatusResponse.OK);
    }

    @GetMapping("/types/{type}")
    public ResponseEntity<List<Long>> getTransactionIdsByType(@PathVariable String type) {
        return ResponseEntity.ok(transactionService.findIdsByType(type));
    }
}
