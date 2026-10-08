package com.spendsense.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spendsense.AbstractIntegrationTest;
import com.spendsense.AuthTestSupport;
import com.spendsense.dto.ExpenseRequest;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@Import(AuthTestSupport.class)
class DashboardControllerTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthTestSupport authTestSupport;

    @Test
    void tcD01_summaryReturnsCorrectTotalsAndByCategory() throws Exception {
        String token = authTestSupport.registerAndGetToken("dashboard-totals");
        Long foodCategoryId = findCategoryIdByName(token, "Food");
        Long transportCategoryId = findCategoryIdByName(token, "Transport");

        createExpense(token, foodCategoryId, "2026-06-10", "Lunch", "25.00");
        createExpense(token, foodCategoryId, "2026-06-12", "Dinner", "35.00");
        createExpense(token, transportCategoryId, "2026-06-11", "Bus", "10.00");

        mockMvc.perform(get("/api/dashboard/summary")
                        .param("from", "2026-06-01")
                        .param("to", "2026-06-30")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalSpent").value(70.0))
                .andExpect(jsonPath("$.expenseCount").value(3))
                .andExpect(jsonPath("$.byCategory.length()").value(2))
                .andExpect(jsonPath("$.byCategory[?(@.categoryName == 'Food')].amount").value(60.0))
                .andExpect(jsonPath("$.byCategory[?(@.categoryName == 'Transport')].amount").value(10.0));
    }

    @Test
    void tcD02_byDayAggregationMatchesSeededExpenses() throws Exception {
        String token = authTestSupport.registerAndGetToken("dashboard-byday");
        Long foodCategoryId = findCategoryIdByName(token, "Food");

        createExpense(token, foodCategoryId, "2026-06-05", "Breakfast", "12.00");
        createExpense(token, foodCategoryId, "2026-06-05", "Coffee", "5.00");
        createExpense(token, foodCategoryId, "2026-06-08", "Lunch", "18.00");

        mockMvc.perform(get("/api/dashboard/summary")
                        .param("from", "2026-06-01")
                        .param("to", "2026-06-30")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.byDay.length()").value(2))
                .andExpect(jsonPath("$.byDay[0].date").value("2026-06-05"))
                .andExpect(jsonPath("$.byDay[0].amount").value(17.0))
                .andExpect(jsonPath("$.byDay[1].date").value("2026-06-08"))
                .andExpect(jsonPath("$.byDay[1].amount").value(18.0));
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

    private void createExpense(
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

        mockMvc.perform(post("/api/expenses")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }
}
