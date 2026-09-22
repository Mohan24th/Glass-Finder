package com.glassfinder.dto;

import java.time.LocalDateTime;

public record StockHistoryResponse(
        Long id,
        String boxCode,
        String transactionType,
        Integer quantity,
        String notes,
        LocalDateTime createdAt,
        Long stockAfterTransaction
) {
}