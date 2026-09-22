package com.glassfinder.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record SellStockRequest(

        @NotBlank
        String boxCode,

        @Positive
        Integer quantity,

        String notes
) {
}