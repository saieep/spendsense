package com.spendsense.service;

import com.spendsense.dto.CategorizeResult;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public final class ExpenseCategorySuggester {

    private ExpenseCategorySuggester() {}

    public static Optional<CategorizeResult> matchKeywords(String description, List<String> categoryNames) {
        String normalized = description == null ? "" : description.toLowerCase(Locale.ROOT);
        if (normalized.isBlank()) {
            return Optional.empty();
        }

        if (containsAny(normalized, "uber", "lyft", "taxi", "metro", "bus", "train", "parking", "toll")) {
            return Optional.of(result("Transport", categoryNames, 0.91));
        }
        if (containsAny(
                normalized,
                "petrol",
                "diesel",
                "gas station",
                "gasoline",
                "fuel",
                "filling",
                "hp petrol",
                "indian oil",
                "bharat petroleum",
                "shell",
                "bp ",
                " cng",
                " lpg")) {
            return Optional.of(result("Transport", categoryNames, 0.92));
        }
        if (containsWord(normalized, "gas")) {
            return Optional.of(result("Transport", categoryNames, 0.9));
        }
        if (containsAny(normalized, "grocery", "whole foods", "restaurant", "cafe", "coffee", "starbucks", "food")) {
            return Optional.of(result("Food", categoryNames, 0.88));
        }
        if (containsAny(normalized, "netflix", "spotify", "movie", "cinema", "game")) {
            return Optional.of(result("Entertainment", categoryNames, 0.86));
        }
        if (containsAny(normalized, "rent", "mortgage", "landlord", "housing")) {
            return Optional.of(result("Housing", categoryNames, 0.87));
        }
        if (containsAny(normalized, "electric", "water bill", "internet", "wifi", "utility", "utilities")) {
            return Optional.of(result("Utilities", categoryNames, 0.86));
        }
        if (containsAny(normalized, "pharmacy", "doctor", "hospital", "clinic", "medical")) {
            return Optional.of(result("Health", categoryNames, 0.86));
        }
        if (containsAny(normalized, "flight", "hotel", "airbnb", "travel", "vacation")) {
            return Optional.of(result("Travel", categoryNames, 0.87));
        }
        if (containsWord(normalized, "course")
                || containsWord(normalized, "tuition")
                || containsAny(normalized, "school", "college", "university")
                || containsWord(normalized, "textbook")) {
            return Optional.of(result("Education", categoryNames, 0.86));
        }
        if (containsAny(normalized, "amazon", "mall", "shopping", "store")) {
            return Optional.of(result("Shopping", categoryNames, 0.84));
        }

        return Optional.empty();
    }

    public static String resolveCategory(String suggested, List<String> categoryNames) {
        if (suggested != null && categoryNames.contains(suggested)) {
            return suggested;
        }
        if (suggested != null) {
            Optional<String> caseInsensitive = categoryNames.stream()
                    .filter(name -> name.equalsIgnoreCase(suggested))
                    .findFirst();
            if (caseInsensitive.isPresent()) {
                return caseInsensitive.get();
            }
        }
        return categoryNames.stream()
                .filter(name -> name.equalsIgnoreCase("Other"))
                .findFirst()
                .orElseGet(() -> categoryNames.isEmpty()
                        ? "Other"
                        : categoryNames.get(categoryNames.size() - 1));
    }

    private static CategorizeResult result(String preferred, List<String> categoryNames, double confidence) {
        return new CategorizeResult(resolveCategory(preferred, categoryNames), confidence);
    }

    private static boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private static boolean containsWord(String text, String word) {
        int index = text.indexOf(word);
        while (index >= 0) {
            boolean startOk = index == 0 || !Character.isLetterOrDigit(text.charAt(index - 1));
            int end = index + word.length();
            boolean endOk = end >= text.length() || !Character.isLetterOrDigit(text.charAt(end));
            if (startOk && endOk) {
                return true;
            }
            index = text.indexOf(word, index + 1);
        }
        return false;
    }
}
