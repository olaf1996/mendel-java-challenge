package com.mendel.transactions.api.dto;

/**
 * Respuesta de PUT /transactions/{id}.
 */
public record StatusResponse(String status) {

    public static final StatusResponse OK = new StatusResponse("ok");
}
