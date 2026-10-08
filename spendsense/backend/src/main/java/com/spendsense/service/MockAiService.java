package com.spendsense.service;

import com.spendsense.dto.CategorizeResult;
import com.spendsense.dto.CategorySpendInput;
import com.spendsense.dto.InsightsResult;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("!dev & !prod")
public class MockAiService implements AiService {

    @Override
    public CategorizeResult categorize(String description, List<String> categoryNames) {
        return ExpenseCategorySuggester.matchKeywords(description, categoryNames)
                .orElseGet(() -> new CategorizeResult(
                        ExpenseCategorySuggester.resolveCategory("Other", categoryNames), 0.5));
    }

    @Override
    public InsightsResult generateInsights(String periodLabel, List<CategorySpendInput> categoryTotals) {
        String topCategory = categoryTotals.isEmpty()
                ? "none"
                : categoryTotals.get(0).categoryName();

        String topAmount = categoryTotals.isEmpty()
                ? ""
                : " (₹" + categoryTotals.get(0).amount() + ")";

        return new InsightsResult(
                "In " + periodLabel + ", your top spending category was " + topCategory + topAmount + ".",
                List.of(
                        "Top category: " + topCategory + topAmount,
                        "Tracked " + categoryTotals.size() + " categories with spending"),
                List.of(
                        "Review recurring subscriptions in Entertainment",
                        "Set a weekly food budget to smooth grocery spikes"));
    }
}
