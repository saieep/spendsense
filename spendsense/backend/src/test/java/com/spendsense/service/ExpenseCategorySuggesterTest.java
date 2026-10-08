package com.spendsense.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class ExpenseCategorySuggesterTest {

  private static final List<String> ALPHABETICAL_CATEGORIES =
      List.of(
          "Education",
          "Entertainment",
          "Food",
          "Health",
          "Housing",
          "Other",
          "Shopping",
          "Transport",
          "Travel",
          "Utilities");

  @Test
  void matchKeywords_petrolReturnsTransport() {
    var result = ExpenseCategorySuggester.matchKeywords("petrol fill", ALPHABETICAL_CATEGORIES);

    assertThat(result).isPresent();
    assertThat(result.get().category()).isEqualTo("Transport");
  }

  @Test
  void matchKeywords_dieselReturnsTransport() {
    var result = ExpenseCategorySuggester.matchKeywords("diesel", ALPHABETICAL_CATEGORIES);

    assertThat(result).isPresent();
    assertThat(result.get().category()).isEqualTo("Transport");
  }

  @Test
  void resolveCategory_unknownSuggestionFallsBackToOtherNotFirstCategory() {
    assertThat(ExpenseCategorySuggester.resolveCategory("Fuel", ALPHABETICAL_CATEGORIES))
        .isEqualTo("Other");
  }
}
