package com.mendel.transactions.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Body del PUT /transactions/{id}.
 * parent_id es opcional (transacciones raíz).
 */
public record TransactionRequest(
        @NotNull Double amount,
        @NotBlank(message = "type must not be blank") String type,
        @JsonProperty("parent_id") Long parentId
) {}
