package com.spendsense.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spendsense.dto.InsightsResult;
import com.spendsense.model.InsightCache;
import com.spendsense.repository.InsightCacheRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

@ExtendWith(MockitoExtension.class)
class InsightServiceTest {

    @Mock
    private InsightCacheRepository insightCacheRepository;

    @Mock
    private DashboardService dashboardService;

    @Mock
    private AiService aiService;

    @Mock
    private TransactionTemplate transactionTemplate;

    private InsightService insightService;

    @BeforeEach
    void setUp() {
        insightService = new InsightService(
                insightCacheRepository,
                dashboardService,
                aiService,
                new ObjectMapper(),
                transactionTemplate);
        doAnswer(invocation -> {
            Consumer<TransactionStatus> consumer = invocation.getArgument(0);
            consumer.accept(null);
            return null;
        }).when(transactionTemplate).executeWithoutResult(any());
    }

    @Test
    void getInsights_updatesStaleCacheInsteadOfInsertingDuplicate() {
        Long userId = 1L;
        String from = "2026-06-01";
        String to = "2026-06-30";
        String staleCreatedAt = Instant.now().minus(25, ChronoUnit.HOURS).toString();
        InsightCache staleCache = new InsightCache(
                userId, from, to, "{\"summary\":\"old\"}", staleCreatedAt);

        when(insightCacheRepository.findByUserIdAndPeriodStartAndPeriodEnd(userId, from, to))
                .thenReturn(Optional.of(staleCache));
        when(dashboardService.getSummary(userId, from, to))
                .thenReturn(new com.spendsense.dto.DashboardSummaryResponse(
                        java.math.BigDecimal.TEN, 1, List.of(), List.of()));
        when(aiService.generateInsights(any(), any()))
                .thenReturn(new InsightsResult("fresh summary", List.of("highlight"), List.of("tip")));

        var response = insightService.getInsights(userId, from, to);

        assertThat(response.cached()).isFalse();
        assertThat(response.summary()).isEqualTo("fresh summary");

        InOrder inOrder = inOrder(insightCacheRepository);
        inOrder.verify(insightCacheRepository).deleteByUserIdAndPeriodStartAndPeriodEnd(userId, from, to);
        inOrder.verify(insightCacheRepository).flush();
        inOrder.verify(insightCacheRepository).save(org.mockito.ArgumentMatchers.any(InsightCache.class));
    }
}
