package com.spendsense.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spendsense.AbstractIntegrationTest;
import com.spendsense.AuthTestSupport;
import com.spendsense.dto.CategoryRequest;
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
class CategoryControllerTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthTestSupport authTestSupport;

    @Test
    void tcC01_newUserGetsDefaultCategories() throws Exception {
        String token = authTestSupport.registerAndGetToken("categories-default");

        mockMvc.perform(get("/api/categories")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(10))
                .andExpect(jsonPath("$[?(@.name == 'Food')].isDefault").value(true));
    }

    @Test
    void tcC02_createCustomCategory() throws Exception {
        String token = authTestSupport.registerAndGetToken("categories-create");
        CategoryRequest request = new CategoryRequest("Pets", "#ff00aa");

        mockMvc.perform(post("/api/categories")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Pets"))
                .andExpect(jsonPath("$.color").value("#ff00aa"))
                .andExpect(jsonPath("$.isDefault").value(false));
    }

    @Test
    void tcC03_deleteCustomCategoryWithoutExpenses() throws Exception {
        String token = authTestSupport.registerAndGetToken("categories-delete");
        CategoryRequest request = new CategoryRequest("Gifts", "#abcdef");

        String response = mockMvc.perform(post("/api/categories")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long categoryId = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(delete("/api/categories/" + categoryId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isNoContent());
    }
}
