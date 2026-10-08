package com.spendsense.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record ExpenseRequest(
        @NotNull
        @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
        BigDecimal amount,
        @NotBlank
        @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "Date must be YYYY-MM-DD")
        String expenseDate,
        @NotBlank
        @Size(max = 200)
        String description,
        @NotNull
        Long categoryId,
        @Size(max = 500)
        String note
) {
}
