package com.spendsense.dto;

import java.math.BigDecimal;
import java.util.List;

public record DashboardSummaryResponse(
        BigDecimal totalSpent,
        int expenseCount,
        List<CategorySpendDto> byCategory,
        List<DaySpendDto> byDay
) {
}
