package com.spendsense.controller;

import com.spendsense.dto.CategorizeRequest;
import com.spendsense.dto.CategorizeResult;
import com.spendsense.dto.InsightsRequest;
import com.spendsense.dto.InsightsResponse;
import com.spendsense.security.SecurityUtils;
import com.spendsense.service.AiService;
import com.spendsense.service.CategoryService;
import com.spendsense.service.ExpenseCategorySuggester;
import com.spendsense.service.InsightService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiService aiService;
    private final CategoryService categoryService;
    private final InsightService insightService;
    private final SecurityUtils securityUtils;

    public AiController(
            AiService aiService,
            CategoryService categoryService,
            InsightService insightService,
            SecurityUtils securityUtils) {
        this.aiService = aiService;
        this.categoryService = categoryService;
        this.insightService = insightService;
        this.securityUtils = securityUtils;
    }

    @PostMapping("/categorize")
    public CategorizeResult categorize(@Valid @RequestBody CategorizeRequest request) {
        Long userId = securityUtils.getCurrentUserId();
        List<String> categoryNames = categoryService.listForUser(userId).stream()
                .map(category -> category.name())
                .toList();

        var keywordMatch = ExpenseCategorySuggester.matchKeywords(request.description(), categoryNames);
        if (keywordMatch.isPresent()) {
            return keywordMatch.get();
        }

        return aiService.categorize(request.description(), categoryNames);
    }

    @PostMapping("/insights")
    public InsightsResponse insights(@Valid @RequestBody InsightsRequest request) {
        return insightService.getInsights(
                securityUtils.getCurrentUserId(),
                request.from(),
                request.to());
    }
}
