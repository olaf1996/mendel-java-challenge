package com.mendel.transactions.api;

import com.mendel.transactions.api.dto.StatusResponse;
import com.mendel.transactions.api.dto.SumResponse;
import com.mendel.transactions.api.dto.TransactionRequest;
import com.mendel.transactions.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/transactions")
@Tag(name = "Transactions", description = "API de transacciones: crear, listar por tipo, suma transitiva")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Operation(summary = "Crear o actualizar transacción", description = "Body: amount (obligatorio), type (obligatorio), parent_id (opcional). 400 si parent_id no existe o formaría ciclo.")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "400", description = "Validación fallida o parent_id inválido/ciclo")
    @PutMapping("/{transactionId}")
    public ResponseEntity<StatusResponse> putTransaction(
            @PathVariable long transactionId,
            @RequestBody @Valid TransactionRequest request) {
        transactionService.save(transactionId, request);
        return ResponseEntity.ok(StatusResponse.OK);
    }

    @Operation(summary = "IDs por tipo", description = "Lista de ids de transacciones del tipo indicado. [] si no hay ninguna.")
    @GetMapping("/types/{type}")
    public ResponseEntity<List<Long>> getTransactionIdsByType(@PathVariable String type) {
        return ResponseEntity.ok(transactionService.findIdsByType(type));
    }

    @Operation(summary = "Suma transitiva", description = "Monto de la transacción más todos sus descendientes por parent_id. 404 si el id no existe.")
    @ApiResponse(responseCode = "200", description = "OK, body: { \"sum\": number }")
    @ApiResponse(responseCode = "404", description = "Transacción no encontrada")
    @GetMapping("/sum/{transactionId}")
    public ResponseEntity<SumResponse> getTransactionSum(@PathVariable long transactionId) {
        double sum = transactionService.getSum(transactionId);
        return ResponseEntity.ok(new SumResponse(sum));
    }
}
