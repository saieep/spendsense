package com.spendsense.dto;

import java.math.BigDecimal;

public record DaySpendDto(
        String date,
        BigDecimal amount
) {
}
