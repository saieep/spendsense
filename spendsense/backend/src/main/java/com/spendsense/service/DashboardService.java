package com.spendsense.service;

import com.spendsense.dto.CategorySpendDto;
import com.spendsense.dto.DashboardSummaryResponse;
import com.spendsense.dto.DaySpendDto;
import com.spendsense.model.Category;
import com.spendsense.model.Expense;
import com.spendsense.repository.CategoryRepository;
import com.spendsense.repository.ExpenseRepository;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {

    private final ExpenseRepository expenseRepository;
    private final CategoryRepository categoryRepository;

    public DashboardService(ExpenseRepository expenseRepository, CategoryRepository categoryRepository) {
        this.expenseRepository = expenseRepository;
        this.categoryRepository = categoryRepository;
    }

    public DashboardSummaryResponse getSummary(Long userId, String from, String to) {
        List<Expense> expenses = loadExpenses(userId, from, to);
        Map<Long, Category> categoriesById = loadCategoriesById(userId);

        BigDecimal totalSpent = BigDecimal.ZERO;
        Map<Long, BigDecimal> spendByCategory = new LinkedHashMap<>();
        Map<String, BigDecimal> spendByDay = new LinkedHashMap<>();

        for (Expense expense : expenses) {
            totalSpent = totalSpent.add(expense.getAmount());
            spendByCategory.merge(expense.getCategoryId(), expense.getAmount(), BigDecimal::add);
            spendByDay.merge(expense.getExpenseDate(), expense.getAmount(), BigDecimal::add);
        }

        List<CategorySpendDto> byCategory = spendByCategory.entrySet().stream()
                .sorted(Map.Entry.<Long, BigDecimal>comparingByValue().reversed())
                .map(entry -> toCategorySpend(entry.getKey(), entry.getValue(), categoriesById))
                .toList();

        List<DaySpendDto> byDay = spendByDay.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> new DaySpendDto(entry.getKey(), entry.getValue()))
                .toList();

        return new DashboardSummaryResponse(totalSpent, expenses.size(), byCategory, byDay);
    }

    private List<Expense> loadExpenses(Long userId, String from, String to) {
        if (from != null && to != null) {
            return expenseRepository.findByUserIdAndExpenseDateBetweenOrderByExpenseDateDesc(userId, from, to);
        }
        return expenseRepository.findByUserIdOrderByExpenseDateDesc(userId);
    }

    private Map<Long, Category> loadCategoriesById(Long userId) {
        Map<Long, Category> categoriesById = new LinkedHashMap<>();
        for (Category category : categoryRepository.findByUserIdOrderByNameAsc(userId)) {
            categoriesById.put(category.getId(), category);
        }
        return categoriesById;
    }

    private CategorySpendDto toCategorySpend(Long categoryId, BigDecimal amount, Map<Long, Category> categoriesById) {
        Category category = categoriesById.get(categoryId);
        if (category == null) {
            return new CategorySpendDto(categoryId, "Unknown", "#64748b", amount);
        }
        return new CategorySpendDto(categoryId, category.getName(), category.getColor(), amount);
    }
}
