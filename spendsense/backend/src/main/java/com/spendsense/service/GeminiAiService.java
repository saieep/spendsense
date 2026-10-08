package com.spendsense.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.spendsense.config.GeminiProperties;
import com.spendsense.config.GeminiRateLimiter;
import com.spendsense.dto.CategorizeResult;
import com.spendsense.dto.CategorySpendInput;
import com.spendsense.dto.InsightsResult;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

@Service
@Profile({"dev", "prod"})
public class GeminiAiService implements AiService {

    private static final Logger log = LoggerFactory.getLogger(GeminiAiService.class);

    private final GeminiProperties geminiProperties;
    private final GeminiRateLimiter rateLimiter;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    public GeminiAiService(
            GeminiProperties geminiProperties,
            GeminiRateLimiter rateLimiter,
            ObjectMapper objectMapper,
            RestClient.Builder restClientBuilder) {
        this.geminiProperties = geminiProperties;
        this.rateLimiter = rateLimiter;
        this.objectMapper = objectMapper;
        this.restClient = restClientBuilder
                .baseUrl("https://generativelanguage.googleapis.com")
                .build();
    }

    @Override
    public CategorizeResult categorize(String description, List<String> categoryNames) {
        var keywordMatch = ExpenseCategorySuggester.matchKeywords(description, categoryNames);
        if (keywordMatch.isPresent()) {
            return keywordMatch.get();
        }

        String prompt = """
                Classify this expense into exactly one category from the list below.
                Use only a category name from the list — no synonyms or new labels.
                Examples: petrol/diesel/fuel/gas → Transport; restaurant/grocery → Food; \
                rent/mortgage → Housing; electricity/internet bill → Utilities.
                Categories: %s
                Description: "%s"
                """.formatted(String.join(", ", categoryNames), description);

        JsonNode json = callGemini(prompt, geminiProperties.getCategorizeMaxTokens(), categorizeSchema(categoryNames));
        String category = json.path("category").asText();
        double confidence = json.path("confidence").asDouble(0.5);
        return new CategorizeResult(ExpenseCategorySuggester.resolveCategory(category, categoryNames), confidence);
    }

    @Override
    public InsightsResult generateInsights(String periodLabel, List<CategorySpendInput> categoryTotals) {
        String totals = categoryTotals.isEmpty()
                ? "no spending recorded"
                : categoryTotals.stream()
                        .map(entry -> entry.categoryName() + ": ₹" + entry.amount())
                        .collect(Collectors.joining(", "));

        String prompt = """
                Personal finance insights for period %s.
                Spending by category (amounts in Indian Rupees): %s
                Use only INR and the ₹ symbol when mentioning money. Never use $ or USD.
                Keep summary to one short sentence. Up to 3 highlights and 2-3 brief suggestions.
                """.formatted(periodLabel, totals);

        JsonNode json = callGemini(prompt, geminiProperties.getInsightsMaxTokens(), insightsSchema());
        return normalizeCurrency(new InsightsResult(
                json.path("summary").asText(),
                readStringList(json.path("highlights")),
                readStringList(json.path("suggestions"))));
    }

    private InsightsResult normalizeCurrency(InsightsResult result) {
        return new InsightsResult(
                normalizeCurrencyText(result.summary()),
                result.highlights().stream().map(this::normalizeCurrencyText).toList(),
                result.suggestions().stream().map(this::normalizeCurrencyText).toList());
    }

    private String normalizeCurrencyText(String text) {
        if (text == null || text.isBlank()) {
            return text == null ? "" : text;
        }
        return text.replace("USD", "INR").replace("$", "₹");
    }

    private JsonNode callGemini(String prompt, int maxOutputTokens, ObjectNode responseSchema) {
        if (!geminiProperties.isEnabled()) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Gemini is disabled. Run with spring profile 'dev' for local AI (see backend/run-dev.ps1).");
        }
        if (!geminiProperties.isConfigured()) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Gemini is not configured. Set GEMINI_API_KEY in spendsense/.env");
        }

        rateLimiter.checkAllowed();

        ObjectNode requestBody = objectMapper.createObjectNode();
        ArrayNode contents = requestBody.putArray("contents");
        ObjectNode content = contents.addObject();
        ArrayNode parts = content.putArray("parts");
        parts.addObject().put("text", prompt);

        ObjectNode generationConfig = requestBody.putObject("generationConfig");
        generationConfig.put("maxOutputTokens", maxOutputTokens);
        generationConfig.put("responseMimeType", "application/json");
        generationConfig.set("responseSchema", responseSchema);
        generationConfig.putObject("thinkingConfig").put("thinkingBudget", 0);

        try {
            JsonNode response = restClient.post()
                    .uri("/v1beta/models/{model}:generateContent?key={apiKey}",
                            geminiProperties.getModel(),
                            geminiProperties.getApiKey())
                    .body(requestBody)
                    .retrieve()
                    .body(JsonNode.class);

            rateLimiter.recordRequest();

            if (geminiProperties.isDevMode()) {
                log.info(
                        "Gemini dev request completed (model={}, daily count={})",
                        geminiProperties.getModel(),
                        rateLimiter.getDailyRequestCount());
            }

            if (response == null) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Empty Gemini response");
            }

            String text = extractResponseText(response);
            if (text.isBlank()) {
                log.warn("Gemini returned no text. Response: {}", response);
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Gemini returned no content");
            }

            return parseJsonText(text);
        } catch (JsonProcessingException ex) {
            log.error("Gemini returned invalid JSON: {}", ex.getOriginalMessage());
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Gemini returned invalid JSON. Try again.",
                    ex);
        } catch (RestClientResponseException ex) {
            throw mapHttpError(ex);
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Gemini call failed", ex);
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Failed to call Gemini: " + ex.getMessage(),
                    ex);
        }
    }

    private ResponseStatusException mapHttpError(RestClientResponseException ex) {
        int status = ex.getStatusCode().value();
        log.warn("Gemini HTTP {}: {}", status, ex.getResponseBodyAsString());

        if (status == 429) {
            return new ResponseStatusException(
                    HttpStatus.TOO_MANY_REQUESTS,
                    "Gemini free-tier quota reached. Wait about a minute and try again, "
                            + "or check usage at https://ai.dev/rate-limit");
        }
        if (status == 403) {
            return new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Gemini API key is invalid or lacks access. Check GEMINI_API_KEY in spendsense/.env");
        }
        return new ResponseStatusException(
                HttpStatus.BAD_GATEWAY,
                "Gemini API error (" + status + "). Try again later.");
    }

    private ObjectNode categorizeSchema(List<String> categoryNames) {
        ObjectNode schema = objectMapper.createObjectNode();
        schema.put("type", "object");
        ObjectNode properties = schema.putObject("properties");
        ObjectNode category = properties.putObject("category");
        category.put("type", "string");
        ArrayNode enumValues = category.putArray("enum");
        categoryNames.forEach(enumValues::add);
        properties.putObject("confidence").put("type", "number");
        schema.putArray("required").add("category").add("confidence");
        schema.putArray("propertyOrdering").add("category").add("confidence");
        return schema;
    }

    private ObjectNode insightsSchema() {
        ObjectNode schema = objectMapper.createObjectNode();
        schema.put("type", "object");
        ObjectNode properties = schema.putObject("properties");
        properties.putObject("summary").put("type", "string");
        ObjectNode highlights = properties.putObject("highlights");
        highlights.put("type", "array");
        highlights.putObject("items").put("type", "string");
        highlights.put("maxItems", 3);
        ObjectNode suggestions = properties.putObject("suggestions");
        suggestions.put("type", "array");
        suggestions.putObject("items").put("type", "string");
        suggestions.put("minItems", 2);
        suggestions.put("maxItems", 3);
        schema.putArray("required").add("summary").add("highlights").add("suggestions");
        schema.putArray("propertyOrdering").add("summary").add("highlights").add("suggestions");
        return schema;
    }

    private String extractResponseText(JsonNode response) {
        JsonNode candidates = response.path("candidates");
        if (!candidates.isArray() || candidates.isEmpty()) {
            return "";
        }
        String finishReason = candidates.path(0).path("finishReason").asText("");
        if ("MAX_TOKENS".equals(finishReason)) {
            log.warn("Gemini response truncated (MAX_TOKENS). Response: {}", response);
        }
        JsonNode parts = candidates.path(0).path("content").path("parts");
        if (!parts.isArray()) {
            return "";
        }
        StringBuilder text = new StringBuilder();
        for (JsonNode part : parts) {
            if (part.path("thought").asBoolean(false)) {
                continue;
            }
            if (part.has("text")) {
                text.append(part.path("text").asText());
            }
        }
        return text.toString();
    }

    private JsonNode parseJsonText(String text) throws JsonProcessingException {
        String trimmed = text.trim();
        if (trimmed.startsWith("```")) {
            trimmed = trimmed.replaceFirst("^```(?:json)?\\s*", "").replaceFirst("\\s*```\\s*$", "");
        }
        return objectMapper.readTree(trimmed);
    }

    private List<String> readStringList(JsonNode node) {
        if (!node.isArray()) {
            return List.of();
        }
        List<String> values = new ArrayList<>();
        node.forEach(item -> values.add(item.asText()));
        return values;
    }
}
