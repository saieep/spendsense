package com.spendsense.service;

import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;

@Component
public class CsvImportParser {

    private static final Pattern DATE_PATTERN = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$");
    private static final CSVFormat IMPORT_FORMAT = CSVFormat.DEFAULT.builder()
            .setHeader("date", "amount", "description", "category", "note")
            .setSkipHeaderRecord(true)
            .setIgnoreHeaderCase(true)
            .setTrim(true)
            .build();

    public List<Object> parse(Reader reader) throws IOException {
        List<Object> results = new ArrayList<>();

        try (CSVParser parser = new CSVParser(reader, IMPORT_FORMAT)) {
            for (CSVRecord record : parser) {
                int line = (int) record.getRecordNumber() + 1;
                results.add(parseRecord(record, line));
            }
        }

        return results;
    }

    private Object parseRecord(CSVRecord record, int line) {
        String date = getValue(record, "date");
        String amountRaw = getValue(record, "amount");
        String description = getValue(record, "description");
        String category = getValue(record, "category");
        String note = getValue(record, "note");

        if (date.isBlank()) {
            return new CsvRowFailure(line, "Date is required");
        }
        if (!DATE_PATTERN.matcher(date).matches()) {
            return new CsvRowFailure(line, "Date must be YYYY-MM-DD");
        }
        if (amountRaw.isBlank()) {
            return new CsvRowFailure(line, "Amount is required");
        }

        BigDecimal amount;
        try {
            amount = new BigDecimal(amountRaw);
        } catch (NumberFormatException ex) {
            return new CsvRowFailure(line, "Amount must be a number");
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return new CsvRowFailure(line, "Amount must be greater than 0");
        }
        if (description.isBlank()) {
            return new CsvRowFailure(line, "Description is required");
        }
        if (category.isBlank()) {
            return new CsvRowFailure(line, "Category is required");
        }

        return new CsvRowSuccess(
                line,
                new ParsedExpenseRow(date, amount, description, category, note.isBlank() ? null : note));
    }

    private String getValue(CSVRecord record, String header) {
        if (!record.isMapped(header)) {
            return "";
        }
        String value = record.get(header);
        return value == null ? "" : value.trim();
    }

    public String normalizeCategoryName(String categoryName) {
        return categoryName.trim();
    }

    public boolean isKnownCategory(String categoryName, java.util.Map<String, Long> categoriesByName) {
        return categoriesByName.containsKey(categoryName.toLowerCase(Locale.ROOT));
    }
}
