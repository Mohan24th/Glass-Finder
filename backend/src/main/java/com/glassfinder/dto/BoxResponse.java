package com.glassfinder.dto;

import java.time.LocalDateTime;
import java.util.List;

public record BoxResponse(
        Long id,
        String boxCode,
        List<String> models,
        Long currentStock,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}