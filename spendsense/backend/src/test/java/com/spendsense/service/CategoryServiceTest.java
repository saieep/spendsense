package com.spendsense.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.spendsense.dto.CategoryRequest;
import com.spendsense.model.Category;
import com.spendsense.repository.CategoryRepository;
import com.spendsense.repository.ExpenseRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ExpenseRepository expenseRepository;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    void seedDefaults_createsTenCategoriesForNewUser() {
        when(categoryRepository.countByUserId(1L)).thenReturn(0L);
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));

        categoryService.seedDefaults(1L);

        verify(categoryRepository, org.mockito.Mockito.times(10)).save(any(Category.class));

        assertThat(CategoryService.DEFAULT_CATEGORIES).hasSize(10);
    }

    @Test
    void seedDefaults_isSkippedWhenUserAlreadyHasCategories() {
        when(categoryRepository.countByUserId(1L)).thenReturn(5L);

        categoryService.seedDefaults(1L);

        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void delete_succeedsWhenCategoryHasNoExpenses() {
        Category category = new Category(1L, "Gifts", "#abcdef", false);
        org.springframework.test.util.ReflectionTestUtils.setField(category, "id", 9L);

        when(categoryRepository.findByIdAndUserId(9L, 1L)).thenReturn(Optional.of(category));
        when(expenseRepository.existsByCategoryId(9L)).thenReturn(false);

        categoryService.delete(1L, 9L);

        verify(categoryRepository).delete(category);
    }

    @Test
    void delete_throwsWhenCategoryLinkedToExpenses() {
        Category category = new Category(1L, "Food", "#22c55e", true);
        org.springframework.test.util.ReflectionTestUtils.setField(category, "id", 3L);

        when(categoryRepository.findByIdAndUserId(3L, 1L)).thenReturn(Optional.of(category));
        when(expenseRepository.existsByCategoryId(3L)).thenReturn(true);

        assertThatThrownBy(() -> categoryService.delete(1L, 3L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("linked to expenses");

        verify(categoryRepository, never()).delete(any(Category.class));
    }

    @Test
    void create_persistsCustomCategory() {
        CategoryRequest request = new CategoryRequest("Pets", "#ff00aa");
        when(categoryRepository.existsByUserIdAndNameIgnoreCase(1L, "Pets")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> {
            Category saved = invocation.getArgument(0);
            org.springframework.test.util.ReflectionTestUtils.setField(saved, "id", 11L);
            return saved;
        });

        var response = categoryService.create(1L, request);

        assertThat(response.name()).isEqualTo("Pets");
        assertThat(response.isDefault()).isFalse();
    }
}
