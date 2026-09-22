package com.glassfinder.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record CreateBoxRequest(

        @NotBlank
        String boxCode,

        @NotEmpty
        List<String> models,

        @Positive
        Integer quantity
) {
}