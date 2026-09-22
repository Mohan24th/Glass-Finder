package com.glassfinder.dto;

import java.util.List;

public record SearchResult(
        String boxCode,
        List<String> models,
        int currentStock
) {
}