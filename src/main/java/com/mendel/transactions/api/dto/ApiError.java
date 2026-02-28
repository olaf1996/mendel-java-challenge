package com.mendel.transactions.api.dto;

/**
 * Cuerpo estándar de error para respuestas 4xx.
 */
public record ApiError(int status, String error, String message) {}
