package com.spendsense.service;

import com.spendsense.dto.ExpenseResponse;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CsvExportService {

    private static final CSVFormat EXPORT_FORMAT = CSVFormat.DEFAULT.builder()
            .setHeader("date", "amount", "description", "category", "note")
            .build();

    private final ExpenseService expenseService;

    public CsvExportService(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    public byte[] exportExpenses(Long userId, String from, String to) {
        List<ExpenseResponse> expenses = expenseService.list(userId, from, to, null);

        try (StringWriter writer = new StringWriter();
                CSVPrinter printer = new CSVPrinter(writer, EXPORT_FORMAT)) {
            for (ExpenseResponse expense : expenses) {
                printer.printRecord(
                        expense.expenseDate(),
                        expense.amount(),
                        expense.description(),
                        expense.categoryName(),
                        expense.note() == null ? "" : expense.note());
            }
            return writer.toString().getBytes(StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to export CSV", ex);
        }
    }
}
