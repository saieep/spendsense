package com.spendsense.service;

import java.math.BigDecimal;

public record ParsedExpenseRow(
        String expenseDate,
        BigDecimal amount,
        String description,
        String categoryName,
        String note
) {}
