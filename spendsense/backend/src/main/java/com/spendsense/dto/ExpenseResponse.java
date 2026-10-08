package com.spendsense.dto;

import com.spendsense.model.Expense;
import java.math.BigDecimal;

public record ExpenseResponse(
        Long id,
        BigDecimal amount,
        String expenseDate,
        String description,
        String note,
        Long categoryId,
        String categoryName
) {

    public static ExpenseResponse from(Expense expense, String categoryName) {
        return new ExpenseResponse(
                expense.getId(),
                expense.getAmount(),
                expense.getExpenseDate(),
                expense.getDescription(),
                expense.getNote(),
                expense.getCategoryId(),
                categoryName
        );
    }
}
