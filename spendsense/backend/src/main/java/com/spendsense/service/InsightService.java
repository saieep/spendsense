package com.spendsense.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spendsense.dto.CategorySpendInput;
import com.spendsense.dto.DashboardSummaryResponse;
import com.spendsense.dto.InsightsResponse;
import com.spendsense.dto.InsightsResult;
import com.spendsense.model.InsightCache;
import com.spendsense.repository.InsightCacheRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;

@Service
public class InsightService {

    private static final Duration CACHE_TTL = Duration.ofHours(24);

    private final InsightCacheRepository insightCacheRepository;
    private final DashboardService dashboardService;
    private final AiService aiService;
    private final ObjectMapper objectMapper;
    private final TransactionTemplate transactionTemplate;

    public InsightService(
            InsightCacheRepository insightCacheRepository,
            DashboardService dashboardService,
            AiService aiService,
            ObjectMapper objectMapper,
            TransactionTemplate transactionTemplate) {
        this.insightCacheRepository = insightCacheRepository;
        this.dashboardService = dashboardService;
        this.aiService = aiService;
        this.objectMapper = objectMapper;
        this.transactionTemplate = transactionTemplate;
    }

    public InsightsResponse getInsights(Long userId, String from, String to) {
        var cached = insightCacheRepository.findByUserIdAndPeriodStartAndPeriodEnd(userId, from, to);
        if (cached.isPresent() && isFresh(cached.get())) {
            return InsightsResponse.from(deserialize(cached.get().getContentJson()), true);
        }

        DashboardSummaryResponse summary = dashboardService.getSummary(userId, from, to);
        List<CategorySpendInput> categoryTotals = summary.byCategory().stream()
                .map(entry -> new CategorySpendInput(entry.categoryName(), entry.amount()))
                .toList();

        String periodLabel = from + " to " + to;
        InsightsResult result = aiService.generateInsights(periodLabel, categoryTotals);
        transactionTemplate.executeWithoutResult(status -> saveCache(userId, from, to, result));
        return InsightsResponse.from(result, false);
    }

    private void saveCache(Long userId, String from, String to, InsightsResult result) {
        try {
            String contentJson = objectMapper.writeValueAsString(result);
            String createdAt = Instant.now().toString();
            insightCacheRepository.deleteByUserIdAndPeriodStartAndPeriodEnd(userId, from, to);
            insightCacheRepository.flush();
            insightCacheRepository.save(new InsightCache(userId, from, to, contentJson, createdAt));
        } catch (JsonProcessingException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to cache insights", ex);
        }
    }

    private boolean isFresh(InsightCache cache) {
        Instant createdAt = Instant.parse(cache.getCreatedAt());
        return createdAt.isAfter(Instant.now().minus(CACHE_TTL));
    }

    private InsightsResult deserialize(String contentJson) {
        try {
            return objectMapper.readValue(contentJson, InsightsResult.class);
        } catch (JsonProcessingException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to read cached insights", ex);
        }
    }
}
