package com.spendsense.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "expenses")
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "INTEGER")
    private Long id;

    @Column(name = "user_id", nullable = false, columnDefinition = "INTEGER")
    private Long userId;

    @Column(name = "category_id", nullable = false, columnDefinition = "INTEGER")
    private Long categoryId;

    @Column(nullable = false, columnDefinition = "REAL")
    private BigDecimal amount;

    @Column(name = "expense_date", nullable = false, columnDefinition = "TEXT")
    private String expenseDate;

    @Column(nullable = false)
    private String description;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(name = "ai_suggested_category", columnDefinition = "TEXT")
    private String aiSuggestedCategory;

    protected Expense() {
    }

    public Expense(
            Long userId,
            Long categoryId,
            BigDecimal amount,
            String expenseDate,
            String description,
            String note
    ) {
        this.userId = userId;
        this.categoryId = categoryId;
        this.amount = amount;
        this.expenseDate = expenseDate;
        this.description = description;
        this.note = note;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getExpenseDate() {
        return expenseDate;
    }

    public String getDescription() {
        return description;
    }

    public String getNote() {
        return note;
    }

    public String getAiSuggestedCategory() {
        return aiSuggestedCategory;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public void setExpenseDate(String expenseDate) {
        this.expenseDate = expenseDate;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
