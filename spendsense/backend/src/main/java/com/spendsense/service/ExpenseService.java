package com.spendsense.service;

import com.spendsense.dto.ExpenseRequest;
import com.spendsense.dto.ExpenseResponse;
import com.spendsense.model.Category;
import com.spendsense.model.Expense;
import com.spendsense.repository.CategoryRepository;
import com.spendsense.repository.ExpenseRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final CategoryRepository categoryRepository;

    public ExpenseService(ExpenseRepository expenseRepository, CategoryRepository categoryRepository) {
        this.expenseRepository = expenseRepository;
        this.categoryRepository = categoryRepository;
    }

    public List<ExpenseResponse> list(Long userId, String from, String to, Long categoryId) {
        List<Expense> expenses;

        if (from != null && to != null && categoryId != null) {
            expenses = expenseRepository.findByUserIdAndCategoryIdAndExpenseDateBetweenOrderByExpenseDateDesc(
                    userId, categoryId, from, to);
        } else if (from != null && to != null) {
            expenses = expenseRepository.findByUserIdAndExpenseDateBetweenOrderByExpenseDateDesc(userId, from, to);
        } else if (categoryId != null) {
            expenses = expenseRepository.findByUserIdAndCategoryIdOrderByExpenseDateDesc(userId, categoryId);
        } else {
            expenses = expenseRepository.findByUserIdOrderByExpenseDateDesc(userId);
        }

        return expenses.stream()
                .map(expense -> toResponse(expense, userId))
                .toList();
    }

    public ExpenseResponse getById(Long userId, Long expenseId) {
        Expense expense = getOwnedExpense(userId, expenseId);
        return toResponse(expense, userId);
    }

    @Transactional
    public ExpenseResponse create(Long userId, ExpenseRequest request) {
        validateCategoryOwnership(userId, request.categoryId());

        Expense expense = expenseRepository.save(new Expense(
                userId,
                request.categoryId(),
                request.amount(),
                request.expenseDate(),
                request.description().trim(),
                request.note()
        ));

        return toResponse(expense, userId);
    }

    @Transactional
    public ExpenseResponse update(Long userId, Long expenseId, ExpenseRequest request) {
        Expense expense = getOwnedExpense(userId, expenseId);
        validateCategoryOwnership(userId, request.categoryId());

        expense.setCategoryId(request.categoryId());
        expense.setAmount(request.amount());
        expense.setExpenseDate(request.expenseDate());
        expense.setDescription(request.description().trim());
        expense.setNote(request.note());

        return toResponse(expenseRepository.save(expense), userId);
    }

    @Transactional
    public void delete(Long userId, Long expenseId) {
        Expense expense = getOwnedExpense(userId, expenseId);
        expenseRepository.delete(expense);
    }

    private Expense getOwnedExpense(Long userId, Long expenseId) {
        return expenseRepository.findByIdAndUserId(expenseId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Expense not found"));
    }

    private void validateCategoryOwnership(Long userId, Long categoryId) {
        categoryRepository.findByIdAndUserId(categoryId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));
    }

    private ExpenseResponse toResponse(Expense expense, Long userId) {
        String categoryName = categoryRepository.findByIdAndUserId(expense.getCategoryId(), userId)
                .map(Category::getName)
                .orElse("Unknown");
        return ExpenseResponse.from(expense, categoryName);
    }
}
