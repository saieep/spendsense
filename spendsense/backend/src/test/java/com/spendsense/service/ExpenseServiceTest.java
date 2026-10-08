package com.spendsense.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.spendsense.dto.ExpenseRequest;
import com.spendsense.model.Category;
import com.spendsense.model.Expense;
import com.spendsense.repository.CategoryRepository;
import com.spendsense.repository.ExpenseRepository;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ExpenseService expenseService;

    @Test
    void create_persistsWhenCategoryBelongsToUser() {
        ExpenseRequest request = new ExpenseRequest(
                new BigDecimal("15.00"),
                "2026-06-26",
                "Bus ticket",
                3L,
                null
        );
        Category category = new Category(1L, "Transport", "#3b82f6", true);
        org.springframework.test.util.ReflectionTestUtils.setField(category, "id", 3L);

        when(categoryRepository.findByIdAndUserId(3L, 1L)).thenReturn(Optional.of(category));
        when(expenseRepository.save(any(Expense.class))).thenAnswer(invocation -> {
            Expense saved = invocation.getArgument(0);
            org.springframework.test.util.ReflectionTestUtils.setField(saved, "id", 42L);
            return saved;
        });
        when(categoryRepository.findByIdAndUserId(3L, 1L)).thenReturn(Optional.of(category));

        var response = expenseService.create(1L, request);

        assertThat(response.id()).isEqualTo(42L);
        assertThat(response.categoryName()).isEqualTo("Transport");
        verify(expenseRepository).save(any(Expense.class));
    }

    @Test
    void create_throwsWhenCategoryNotFound() {
        ExpenseRequest request = new ExpenseRequest(
                new BigDecimal("15.00"),
                "2026-06-26",
                "Bus ticket",
                99L,
                null
        );

        when(categoryRepository.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> expenseService.create(1L, request))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));

        verify(expenseRepository, never()).save(any(Expense.class));
    }

    @Test
    void delete_removesOwnedExpense() {
        Expense expense = new Expense(1L, 3L, new BigDecimal("10.00"), "2026-06-26", "Snack", null);
        org.springframework.test.util.ReflectionTestUtils.setField(expense, "id", 7L);

        when(expenseRepository.findByIdAndUserId(7L, 1L)).thenReturn(Optional.of(expense));

        expenseService.delete(1L, 7L);

        verify(expenseRepository).delete(expense);
    }

    @Test
    void getById_returnsExpenseForOwner() {
        Expense expense = new Expense(1L, 3L, new BigDecimal("12.00"), "2026-06-26", "Snack", null);
        org.springframework.test.util.ReflectionTestUtils.setField(expense, "id", 7L);
        Category category = new Category(1L, "Food", "#22c55e", true);
        org.springframework.test.util.ReflectionTestUtils.setField(category, "id", 3L);

        when(expenseRepository.findByIdAndUserId(7L, 1L)).thenReturn(Optional.of(expense));
        when(categoryRepository.findByIdAndUserId(3L, 1L)).thenReturn(Optional.of(category));

        var response = expenseService.getById(1L, 7L);

        assertThat(response.description()).isEqualTo("Snack");
        assertThat(response.categoryName()).isEqualTo("Food");
    }

    @Test
    void tcE03_getById_throwsWhenExpenseBelongsToAnotherUser() {
        when(expenseRepository.findByIdAndUserId(7L, 2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> expenseService.getById(2L, 7L))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }
}
