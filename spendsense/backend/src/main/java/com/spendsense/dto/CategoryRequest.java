package com.spendsense.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CategoryRequest(
        @NotBlank @Size(max = 50) String name,
        @NotBlank
        @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Color must be a hex value like #22c55e")
        String color
) {
}
