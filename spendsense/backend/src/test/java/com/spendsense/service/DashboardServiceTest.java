package com.spendsense.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.spendsense.model.Category;
import com.spendsense.model.Expense;
import com.spendsense.repository.CategoryRepository;
import com.spendsense.repository.ExpenseRepository;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private DashboardService dashboardService;

    @Test
    void getSummary_aggregatesTotalsAndByCategory() {
        Category food = category(3L, "Food", "#22c55e");
        Category transport = category(4L, "Transport", "#3b82f6");

        List<Expense> expenses = List.of(
                expense(1L, 3L, "25.00", "2026-06-10"),
                expense(1L, 3L, "35.00", "2026-06-12"),
                expense(1L, 4L, "10.00", "2026-06-11")
        );

        when(expenseRepository.findByUserIdAndExpenseDateBetweenOrderByExpenseDateDesc(
                1L, "2026-06-01", "2026-06-30")).thenReturn(expenses);
        when(categoryRepository.findByUserIdOrderByNameAsc(1L)).thenReturn(List.of(food, transport));

        var summary = dashboardService.getSummary(1L, "2026-06-01", "2026-06-30");

        assertThat(summary.totalSpent()).isEqualByComparingTo("70.00");
        assertThat(summary.expenseCount()).isEqualTo(3);
        assertThat(summary.byCategory()).hasSize(2);
        assertThat(summary.byCategory().get(0).categoryName()).isEqualTo("Food");
        assertThat(summary.byCategory().get(0).amount()).isEqualByComparingTo("60.00");
        assertThat(summary.byCategory().get(1).categoryName()).isEqualTo("Transport");
        assertThat(summary.byCategory().get(1).amount()).isEqualByComparingTo("10.00");
    }

    @Test
    void getSummary_aggregatesByDayInAscendingDateOrder() {
        Category food = category(3L, "Food", "#22c55e");

        List<Expense> expenses = List.of(
                expense(1L, 3L, "12.00", "2026-06-08"),
                expense(1L, 3L, "5.00", "2026-06-05"),
                expense(1L, 3L, "18.00", "2026-06-05")
        );

        when(expenseRepository.findByUserIdAndExpenseDateBetweenOrderByExpenseDateDesc(
                1L, "2026-06-01", "2026-06-30")).thenReturn(expenses);
        when(categoryRepository.findByUserIdOrderByNameAsc(1L)).thenReturn(List.of(food));

        var summary = dashboardService.getSummary(1L, "2026-06-01", "2026-06-30");

        assertThat(summary.byDay()).hasSize(2);
        assertThat(summary.byDay().get(0).date()).isEqualTo("2026-06-05");
        assertThat(summary.byDay().get(0).amount()).isEqualByComparingTo("23.00");
        assertThat(summary.byDay().get(1).date()).isEqualTo("2026-06-08");
        assertThat(summary.byDay().get(1).amount()).isEqualByComparingTo("12.00");
    }

    @Test
    void getSummary_returnsEmptyAggregationsWhenNoExpenses() {
        when(expenseRepository.findByUserIdAndExpenseDateBetweenOrderByExpenseDateDesc(
                1L, "2026-06-01", "2026-06-30")).thenReturn(List.of());
        when(categoryRepository.findByUserIdOrderByNameAsc(1L)).thenReturn(List.of());

        var summary = dashboardService.getSummary(1L, "2026-06-01", "2026-06-30");

        assertThat(summary.totalSpent()).isEqualByComparingTo("0");
        assertThat(summary.expenseCount()).isZero();
        assertThat(summary.byCategory()).isEmpty();
        assertThat(summary.byDay()).isEmpty();
    }

    @Test
    void getSummary_usesUnknownCategoryWhenCategoryMissing() {
        List<Expense> expenses = List.of(expense(1L, 99L, "15.00", "2026-06-10"));

        when(expenseRepository.findByUserIdOrderByExpenseDateDesc(1L)).thenReturn(expenses);
        when(categoryRepository.findByUserIdOrderByNameAsc(1L)).thenReturn(List.of());

        var summary = dashboardService.getSummary(1L, null, null);

        assertThat(summary.byCategory()).hasSize(1);
        assertThat(summary.byCategory().get(0).categoryName()).isEqualTo("Unknown");
        assertThat(summary.byCategory().get(0).color()).isEqualTo("#64748b");
    }

    private Category category(Long id, String name, String color) {
        Category category = new Category(1L, name, color, true);
        org.springframework.test.util.ReflectionTestUtils.setField(category, "id", id);
        return category;
    }

    private Expense expense(Long userId, Long categoryId, String amount, String date) {
        return new Expense(userId, categoryId, new BigDecimal(amount), date, "Test", null);
    }
}
