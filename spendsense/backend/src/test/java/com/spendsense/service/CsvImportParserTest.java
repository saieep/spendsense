package com.spendsense.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.StringReader;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CsvImportParserTest {

    private CsvImportParser parser;

    @BeforeEach
    void setUp() {
        parser = new CsvImportParser();
    }

    @Test
    void tcCSV04_rejectsNegativeAmountAndBadDate() throws Exception {
        String csv = """
                date,amount,description,category,note
                2026-06-10,-4.50,Negative,Food,
                not-a-date,12.00,Bad date,Food,
                """;

        List<Object> results = parser.parse(new StringReader(csv));

        assertThat(results).hasSize(2);
        assertThat(results.get(0)).isInstanceOf(CsvRowFailure.class);
        assertThat(((CsvRowFailure) results.get(0)).message()).contains("greater than 0");
        assertThat(results.get(1)).isInstanceOf(CsvRowFailure.class);
        assertThat(((CsvRowFailure) results.get(1)).message()).contains("YYYY-MM-DD");
    }

    @Test
    void parse_acceptsValidRow() throws Exception {
        String csv = """
                date,amount,description,category,note
                2026-06-10,12.50,Lunch,Food,Team lunch
                """;

        List<Object> results = parser.parse(new StringReader(csv));

        assertThat(results).hasSize(1);
        assertThat(results.get(0)).isInstanceOf(CsvRowSuccess.class);
        ParsedExpenseRow row = ((CsvRowSuccess) results.get(0)).row();
        assertThat(row.expenseDate()).isEqualTo("2026-06-10");
        assertThat(row.amount()).isEqualByComparingTo("12.50");
        assertThat(row.description()).isEqualTo("Lunch");
        assertThat(row.categoryName()).isEqualTo("Food");
        assertThat(row.note()).isEqualTo("Team lunch");
    }
}
