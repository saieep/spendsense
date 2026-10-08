package com.spendsense.dto;

import java.math.BigDecimal;

public record CategorySpendDto(
        Long categoryId,
        String categoryName,
        String color,
        BigDecimal amount
) {
}
