package com.spendsense.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.spendsense.dto.CategorySpendInput;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AiServiceTest {

    private MockAiService aiService;

    @BeforeEach
    void setUp() {
        aiService = new MockAiService();
    }

    @Test
    void tcAI01_uberRideReturnsTransport() {
        var result = aiService.categorize(
                "Uber ride",
                List.of("Food", "Transport", "Shopping", "Entertainment"));

        assertThat(result.category()).isEqualTo("Transport");
        assertThat(result.confidence()).isGreaterThanOrEqualTo(0.9);
    }

    @Test
    void categorize_foodKeywordsReturnFoodCategory() {
        var result = aiService.categorize(
                "Whole Foods grocery run",
                List.of("Food", "Transport"));

        assertThat(result.category()).isEqualTo("Food");
        assertThat(result.confidence()).isGreaterThan(0.8);
    }

    @Test
    void categorize_petrolReturnsTransport() {
        var result = aiService.categorize(
                "petrol",
                List.of("Education", "Food", "Transport", "Other"));

        assertThat(result.category()).isEqualTo("Transport");
        assertThat(result.confidence()).isGreaterThan(0.8);
    }

    @Test
    void categorize_dieselReturnsTransport() {
        var result = aiService.categorize(
                "diesel",
                List.of("Education", "Food", "Transport", "Other"));

        assertThat(result.category()).isEqualTo("Transport");
    }

    @Test
    void categorize_unknownDescriptionFallsBackToOther() {
        var result = aiService.categorize(
                "miscellaneous payment",
                List.of("Education", "Food", "Transport", "Other"));

        assertThat(result.category()).isEqualTo("Other");
    }

    @Test
    void generateInsights_returnsSummaryHighlightsAndSuggestions() {
        var insights = aiService.generateInsights(
                "June 2026",
                List.of(
                        new CategorySpendInput("Food", new BigDecimal("420.00")),
                        new CategorySpendInput("Transport", new BigDecimal("180.50"))));

        assertThat(insights.summary()).contains("June 2026");
        assertThat(insights.highlights()).isNotEmpty();
        assertThat(insights.suggestions()).hasSizeGreaterThanOrEqualTo(2);
    }
}
