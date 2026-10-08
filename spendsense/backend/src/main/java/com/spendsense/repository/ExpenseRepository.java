package com.spendsense.repository;

import com.spendsense.model.Expense;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    Optional<Expense> findByIdAndUserId(Long id, Long userId);

    List<Expense> findByUserIdOrderByExpenseDateDesc(Long userId);

    List<Expense> findByUserIdAndExpenseDateBetweenOrderByExpenseDateDesc(
            Long userId, String from, String to);

    List<Expense> findByUserIdAndCategoryIdOrderByExpenseDateDesc(Long userId, Long categoryId);

    List<Expense> findByUserIdAndCategoryIdAndExpenseDateBetweenOrderByExpenseDateDesc(
            Long userId, Long categoryId, String from, String to);

    boolean existsByCategoryId(Long categoryId);
}
