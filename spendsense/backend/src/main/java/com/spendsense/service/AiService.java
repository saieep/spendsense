package com.spendsense.service;

import com.spendsense.dto.CategorizeResult;
import com.spendsense.dto.CategorySpendInput;
import com.spendsense.dto.InsightsResult;
import java.util.List;

public interface AiService {

    CategorizeResult categorize(String description, List<String> categoryNames);

    InsightsResult generateInsights(String periodLabel, List<CategorySpendInput> categoryTotals);
}
