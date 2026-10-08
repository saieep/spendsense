package com.spendsense.dto;

import java.util.List;

public record CsvImportResult(int imported, int failed, List<CsvImportError> errors) {}
