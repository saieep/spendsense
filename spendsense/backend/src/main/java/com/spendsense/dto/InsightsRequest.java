package com.spendsense.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record InsightsRequest(
        @NotBlank
        @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "Date must be YYYY-MM-DD")
        String from,
        @NotBlank
        @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "Date must be YYYY-MM-DD")
        String to
) {
}
