package com.spendsense.controller;

import com.spendsense.dto.CsvImportResult;
import com.spendsense.dto.ExpenseRequest;
import com.spendsense.dto.ExpenseResponse;
import com.spendsense.security.SecurityUtils;
import com.spendsense.service.CsvExportService;
import com.spendsense.service.CsvImportService;
import com.spendsense.service.ExpenseService;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;
    private final CsvExportService csvExportService;
    private final CsvImportService csvImportService;
    private final SecurityUtils securityUtils;

    public ExpenseController(
            ExpenseService expenseService,
            CsvExportService csvExportService,
            CsvImportService csvImportService,
            SecurityUtils securityUtils) {
        this.expenseService = expenseService;
        this.csvExportService = csvExportService;
        this.csvImportService = csvImportService;
        this.securityUtils = securityUtils;
    }

    @GetMapping
    public List<ExpenseResponse> list(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false) Long categoryId
    ) {
        return expenseService.list(securityUtils.getCurrentUserId(), from, to, categoryId);
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> export(
            @RequestParam String from,
            @RequestParam String to
    ) {
        byte[] csv = csvExportService.exportExpenses(securityUtils.getCurrentUserId(), from, to);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"expenses.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CsvImportResult importCsv(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "CSV file is required");
        }

        try {
            return csvImportService.importExpenses(securityUtils.getCurrentUserId(), file.getBytes());
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to read CSV file", ex);
        }
    }

    @GetMapping("/{id}")
    public ExpenseResponse getById(@PathVariable Long id) {
        return expenseService.getById(securityUtils.getCurrentUserId(), id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ExpenseResponse create(@Valid @RequestBody ExpenseRequest request) {
        return expenseService.create(securityUtils.getCurrentUserId(), request);
    }

    @PutMapping("/{id}")
    public ExpenseResponse update(@PathVariable Long id, @Valid @RequestBody ExpenseRequest request) {
        return expenseService.update(securityUtils.getCurrentUserId(), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        expenseService.delete(securityUtils.getCurrentUserId(), id);
    }
}
