package com.spendsense.service;

import com.spendsense.dto.CategoryRequest;
import com.spendsense.dto.CategoryResponse;
import com.spendsense.model.Category;
import com.spendsense.repository.CategoryRepository;
import com.spendsense.repository.ExpenseRepository;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CategoryService {

    static final List<Map.Entry<String, String>> DEFAULT_CATEGORIES = List.of(
            Map.entry("Food", "#22c55e"),
            Map.entry("Transport", "#3b82f6"),
            Map.entry("Housing", "#a855f7"),
            Map.entry("Utilities", "#f59e0b"),
            Map.entry("Entertainment", "#ec4899"),
            Map.entry("Health", "#ef4444"),
            Map.entry("Shopping", "#06b6d4"),
            Map.entry("Travel", "#8b5cf6"),
            Map.entry("Education", "#14b8a6"),
            Map.entry("Other", "#64748b")
    );

    private final CategoryRepository categoryRepository;
    private final ExpenseRepository expenseRepository;

    public CategoryService(CategoryRepository categoryRepository, ExpenseRepository expenseRepository) {
        this.categoryRepository = categoryRepository;
        this.expenseRepository = expenseRepository;
    }

    @Transactional
    public void seedDefaults(Long userId) {
        if (categoryRepository.countByUserId(userId) > 0) {
            return;
        }

        for (Map.Entry<String, String> entry : DEFAULT_CATEGORIES) {
            categoryRepository.save(new Category(userId, entry.getKey(), entry.getValue(), true));
        }
    }

    public List<CategoryResponse> listForUser(Long userId) {
        return categoryRepository.findByUserIdOrderByNameAsc(userId).stream()
                .map(CategoryResponse::from)
                .toList();
    }

    @Transactional
    public CategoryResponse create(Long userId, CategoryRequest request) {
        if (categoryRepository.existsByUserIdAndNameIgnoreCase(userId, request.name())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Category already exists");
        }

        Category category = categoryRepository.save(
                new Category(userId, request.name().trim(), request.color(), false)
        );
        return CategoryResponse.from(category);
    }

    @Transactional
    public CategoryResponse update(Long userId, Long categoryId, CategoryRequest request) {
        Category category = getOwnedCategory(userId, categoryId);

        if (categoryRepository.existsByUserIdAndNameIgnoreCaseAndIdNot(userId, request.name(), categoryId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Category already exists");
        }

        category.setName(request.name().trim());
        category.setColor(request.color());
        return CategoryResponse.from(categoryRepository.save(category));
    }

    @Transactional
    public void delete(Long userId, Long categoryId) {
        Category category = getOwnedCategory(userId, categoryId);

        if (expenseRepository.existsByCategoryId(categoryId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Category is linked to expenses");
        }

        categoryRepository.delete(category);
    }

    private Category getOwnedCategory(Long userId, Long categoryId) {
        return categoryRepository.findByIdAndUserId(categoryId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));
    }
}
