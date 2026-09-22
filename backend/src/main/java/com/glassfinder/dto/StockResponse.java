package com.glassfinder.dto;

public record StockResponse(
        String boxCode,
        String transactionType,
        Integer quantity,
        Long currentStock,
        String message
) {
}