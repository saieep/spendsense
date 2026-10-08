package com.spendsense.service;

import com.spendsense.dto.CsvImportError;
import com.spendsense.dto.CsvImportResult;
import com.spendsense.model.Category;
import com.spendsense.model.Expense;
import com.spendsense.repository.CategoryRepository;
import com.spendsense.repository.ExpenseRepository;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CsvImportService {

    private final CsvImportParser csvImportParser;
    private final CategoryRepository categoryRepository;
    private final ExpenseRepository expenseRepository;

    public CsvImportService(
            CsvImportParser csvImportParser,
            CategoryRepository categoryRepository,
            ExpenseRepository expenseRepository) {
        this.csvImportParser = csvImportParser;
        this.categoryRepository = categoryRepository;
        this.expenseRepository = expenseRepository;
    }

    @Transactional
    public CsvImportResult importExpenses(Long userId, byte[] csvContent) {
        Map<String, Long> categoriesByName = loadCategoriesByName(userId);
        Long otherCategoryId = categoriesByName.get("other");
        if (otherCategoryId == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Default Other category not found");
        }

        List<Object> parsedRows;
        try {
            parsedRows = csvImportParser.parse(
                    new InputStreamReader(new java.io.ByteArrayInputStream(csvContent), StandardCharsets.UTF_8));
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to read CSV file", ex);
        }

        int imported = 0;
        int failed = 0;
        List<CsvImportError> errors = new ArrayList<>();

        for (Object parsedRow : parsedRows) {
            if (parsedRow instanceof CsvRowFailure failure) {
                failed++;
                errors.add(new CsvImportError(failure.line(), failure.message()));
                continue;
            }

            CsvRowSuccess success = (CsvRowSuccess) parsedRow;
            ParsedExpenseRow row = success.row();
            Long categoryId = resolveCategoryId(row.categoryName(), categoriesByName, otherCategoryId);

            expenseRepository.save(new Expense(
                    userId,
                    categoryId,
                    row.amount(),
                    row.expenseDate(),
                    row.description(),
                    row.note()));
            imported++;
        }

        return new CsvImportResult(imported, failed, errors);
    }

    private Map<String, Long> loadCategoriesByName(Long userId) {
        Map<String, Long> categoriesByName = new HashMap<>();
        for (Category category : categoryRepository.findByUserIdOrderByNameAsc(userId)) {
            categoriesByName.put(category.getName().toLowerCase(Locale.ROOT), category.getId());
        }
        return categoriesByName;
    }

    private Long resolveCategoryId(String categoryName, Map<String, Long> categoriesByName, Long otherCategoryId) {
        Long categoryId = categoriesByName.get(categoryName.toLowerCase(Locale.ROOT));
        return categoryId != null ? categoryId : otherCategoryId;
    }
}
