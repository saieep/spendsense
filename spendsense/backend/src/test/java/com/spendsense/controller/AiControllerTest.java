package com.spendsense.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spendsense.AbstractIntegrationTest;
import com.spendsense.AuthTestSupport;
import com.spendsense.dto.CategorizeRequest;
import com.spendsense.dto.InsightsRequest;
import com.spendsense.dto.ExpenseRequest;
import java.math.BigDecimal;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@Import(AuthTestSupport.class)
class AiControllerTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthTestSupport authTestSupport;

    @Test
    void categorize_petrolReturnsTransport() throws Exception {
        String token = authTestSupport.registerAndGetToken("ai-petrol");
        CategorizeRequest request = new CategorizeRequest("petrol");

        mockMvc.perform(post("/api/ai/categorize")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.category").value("Transport"));
    }

    @Test
    void categorize_dieselReturnsTransport() throws Exception {
        String token = authTestSupport.registerAndGetToken("ai-diesel");
        CategorizeRequest request = new CategorizeRequest("diesel");

        mockMvc.perform(post("/api/ai/categorize")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.category").value("Transport"));
    }

    @Test
    void tcAI02_categorizeReturnsValidCategory() throws Exception {
        String token = authTestSupport.registerAndGetToken("ai-categorize");
        CategorizeRequest request = new CategorizeRequest("Uber ride to airport");

        mockMvc.perform(post("/api/ai/categorize")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.category").value("Transport"))
                .andExpect(jsonPath("$.confidence").isNumber());
    }

    @Test
    void categorize_requiresAuthentication() throws Exception {
        CategorizeRequest request = new CategorizeRequest("Uber ride");

        mockMvc.perform(post("/api/ai/categorize")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void categorize_rejectsBlankDescription() throws Exception {
        String token = authTestSupport.registerAndGetToken("ai-categorize-invalid");
        CategorizeRequest request = new CategorizeRequest("");

        mockMvc.perform(post("/api/ai/categorize")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void tcAI03_insightsReturnsSummaryAndCachesOnRepeat() throws Exception {
        String token = authTestSupport.registerAndGetToken("ai-insights");
        Long foodCategoryId = findCategoryIdByName(token, "Food");
        createExpense(token, foodCategoryId, "2026-06-10", "Lunch", "50.00");
        createExpense(token, foodCategoryId, "2026-06-15", "Dinner", "30.00");

        InsightsRequest request = new InsightsRequest("2026-06-01", "2026-06-30");
        String body = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/ai/insights")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary").isNotEmpty())
                .andExpect(jsonPath("$.highlights").isArray())
                .andExpect(jsonPath("$.suggestions").isNotEmpty())
                .andExpect(jsonPath("$.cached").value(false));

        mockMvc.perform(post("/api/ai/insights")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary").isNotEmpty())
                .andExpect(jsonPath("$.cached").value(true));
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
