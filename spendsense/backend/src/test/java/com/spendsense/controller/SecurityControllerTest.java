package com.spendsense.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spendsense.AbstractIntegrationTest;
import com.spendsense.AuthTestSupport;
import com.spendsense.dto.CategoryRequest;
import com.spendsense.dto.ExpenseRequest;
import com.spendsense.dto.InsightsRequest;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
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
class SecurityControllerTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthTestSupport authTestSupport;

    @Test
    void tcS01_allDataQueriesScopedByAuthenticatedUserId() throws Exception {
        String tokenA = authTestSupport.registerAndGetToken("security-user-a");
        Long foodCategoryIdA = findCategoryIdByName(tokenA, "Food");
        Long expenseIdA = createExpense(tokenA, foodCategoryIdA, "2026-07-01", "User A lunch", "42.00");
        Long customCategoryIdA = createCategory(tokenA, "User A Pets", "#ff00aa");

        String tokenB = authTestSupport.registerAndGetToken("security-user-b");

        mockMvc.perform(get("/api/expenses")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        mockMvc.perform(get("/api/expenses/" + expenseIdA)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenB))
                .andExpect(status().isNotFound());

        ExpenseRequest updateRequest = new ExpenseRequest(
                new BigDecimal("99.00"),
                "2026-07-01",
                "Hijacked",
                findCategoryIdByName(tokenB, "Food"),
                null
        );
        mockMvc.perform(put("/api/expenses/" + expenseIdA)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound());

        mockMvc.perform(delete("/api/expenses/" + expenseIdA)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenB))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/categories")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.name == 'User A Pets')]").doesNotExist());

        CategoryRequest categoryUpdate = new CategoryRequest("Stolen", "#000000");
        mockMvc.perform(put("/api/categories/" + customCategoryIdA)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryUpdate)))
                .andExpect(status().isNotFound());

        mockMvc.perform(delete("/api/categories/" + customCategoryIdA)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenB))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/dashboard/summary")
                        .param("from", "2026-07-01")
                        .param("to", "2026-07-31")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalSpent").value(0))
                .andExpect(jsonPath("$.expenseCount").value(0));

        InsightsRequest insightsRequest = new InsightsRequest("2026-07-01", "2026-07-31");
        mockMvc.perform(post("/api/ai/insights")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(insightsRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary").exists());

        MvcResult exportResult = mockMvc.perform(get("/api/expenses/export")
                        .param("from", "2026-07-01")
                        .param("to", "2026-07-31")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenB))
                .andExpect(status().isOk())
                .andReturn();

        String csv = exportResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(csv.trim().split("\\R")).hasSize(1);
        assertThat(csv).doesNotContain("User A lunch");

        mockMvc.perform(get("/api/expenses")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].description").value("User A lunch"));
    }

    @Test
    void corsPreflightAllowsConfiguredOrigin() throws Exception {
        mockMvc.perform(options("/api/health")
                        .header(HttpHeaders.ORIGIN, "http://localhost:5173")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:5173"))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, containsString("GET")));
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

    private Long createCategory(String token, String name, String color) throws Exception {
        CategoryRequest request = new CategoryRequest(name, color);
        MvcResult result = mockMvc.perform(post("/api/categories")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
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
