package com.spendsense.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategorizeRequest(
        @NotBlank
        @Size(max = 200)
        String description
) {
}
