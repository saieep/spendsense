package com.spendsense.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spendsense.AbstractIntegrationTest;
import com.spendsense.AuthTestSupport;
import com.spendsense.dto.ExpenseRequest;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@Import(AuthTestSupport.class)
class ExpenseControllerTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthTestSupport authTestSupport;

    @Test
    void tcE01_createExpenseWithValidData() throws Exception {
        String token = authTestSupport.registerAndGetToken("expense-create");
        Long foodCategoryId = findCategoryIdByName(token, "Food");
        ExpenseRequest request = new ExpenseRequest(
                new BigDecimal("24.50"),
                "2026-06-26",
                "Lunch at cafe",
                foodCategoryId,
                "Team lunch"
        );

        mockMvc.perform(post("/api/expenses")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.amount").value(24.50))
                .andExpect(jsonPath("$.description").value("Lunch at cafe"))
                .andExpect(jsonPath("$.categoryId").value(foodCategoryId.intValue()))
                .andExpect(jsonPath("$.categoryName").value("Food"));
    }

    @Test
    void tcE02_createExpenseWithZeroAmount() throws Exception {
        String token = authTestSupport.registerAndGetToken("expense-invalid");
        Long foodCategoryId = findCategoryIdByName(token, "Food");
        ExpenseRequest request = new ExpenseRequest(
                BigDecimal.ZERO,
                "2026-06-26",
                "Invalid expense",
                foodCategoryId,
                null
        );

        mockMvc.perform(post("/api/expenses")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void tcE03_userCannotReadOtherUsersExpense() throws Exception {
        String tokenA = authTestSupport.registerAndGetToken("expense-user-a");
        Long foodCategoryId = findCategoryIdByName(tokenA, "Food");
        Long expenseId = createExpense(tokenA, foodCategoryId, "2026-06-01", "Coffee", "5.00");

        String tokenB = authTestSupport.registerAndGetToken("expense-user-b");

        mockMvc.perform(get("/api/expenses/" + expenseId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenB))
                .andExpect(status().isNotFound());
    }

    @Test
    void tcE04_filterByDateRange() throws Exception {
        String token = authTestSupport.registerAndGetToken("expense-filter");
        Long foodCategoryId = findCategoryIdByName(token, "Food");

        createExpense(token, foodCategoryId, "2026-06-01", "Early month", "10.00");
        createExpense(token, foodCategoryId, "2026-06-15", "Mid month", "20.00");
        createExpense(token, foodCategoryId, "2026-06-30", "Late month", "30.00");

        mockMvc.perform(get("/api/expenses")
                        .param("from", "2026-06-10")
                        .param("to", "2026-06-20")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].description").value("Mid month"));
    }

    @Test
    void tcCSV01_exportReturnsCorrectHeadersAndRowCount() throws Exception {
        String token = authTestSupport.registerAndGetToken("csv-export");
        Long foodCategoryId = findCategoryIdByName(token, "Food");

        createExpense(token, foodCategoryId, "2026-06-10", "Lunch", "25.00");
        createExpense(token, foodCategoryId, "2026-06-12", "Dinner", "35.00");
        createExpense(token, foodCategoryId, "2026-07-01", "Out of range", "99.00");

        MvcResult result = mockMvc.perform(get("/api/expenses/export")
                        .param("from", "2026-06-01")
                        .param("to", "2026-06-30")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, containsString("text/csv")))
                .andReturn();

        String csv = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        String[] lines = csv.trim().split("\\R");
        assertThat(lines[0]).isEqualTo("date,amount,description,category,note");
        assertThat(lines).hasSize(3);
        assertThat(csv).contains("Lunch");
        assertThat(csv).contains("Dinner");
        assertThat(csv).doesNotContain("Out of range");
    }

    @Test
    void tcCSV02_validCsvImportsAllRows() throws Exception {
        String token = authTestSupport.registerAndGetToken("csv-import-valid");
        byte[] csv = new ClassPathResource("test-data/valid-expenses.csv").getContentAsByteArray();

        mockMvc.perform(multipart("/api/expenses/import")
                        .file(new MockMultipartFile("file", "valid-expenses.csv", "text/csv", csv))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imported").value(3))
                .andExpect(jsonPath("$.failed").value(0))
                .andExpect(jsonPath("$.errors").isEmpty());

        mockMvc.perform(get("/api/expenses")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    void tcCSV03_invalidRowsReturnPartialImportAndErrors() throws Exception {
        String token = authTestSupport.registerAndGetToken("csv-import-invalid");
        byte[] csv = new ClassPathResource("test-data/invalid-expenses.csv").getContentAsByteArray();

        mockMvc.perform(multipart("/api/expenses/import")
                        .file(new MockMultipartFile("file", "invalid-expenses.csv", "text/csv", csv))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imported").value(3))
                .andExpect(jsonPath("$.failed").value(3))
                .andExpect(jsonPath("$.errors.length()").value(3));

        mockMvc.perform(get("/api/expenses")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[?(@.description == 'Unknown category')].categoryName").value("Other"));
    }

    private Long findCategoryIdByName(String token, String categoryName) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/categories")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode categories = objectMapper.readTree(result.getResponse().getContentAsString());
        for (JsonNode category : categories) {
            if (categoryName.equals(category.get("name").asText())) {
                return category.get("id").asLong();
            }
        }
        throw new IllegalStateException("Category not found: " + categoryName);
    }

    private Long createExpense(
            String token,
            Long categoryId,
            String date,
            String description,
            String amount
    ) throws Exception {
        ExpenseRequest request = new ExpenseRequest(
                new BigDecimal(amount),
                date,
                description,
                categoryId,
                null
        );

        MvcResult result = mockMvc.perform(post("/api/expenses")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }
}
