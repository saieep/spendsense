package com.spendsense.dto;

import java.util.List;

public record InsightsResponse(
        String summary,
        List<String> highlights,
        List<String> suggestions,
        boolean cached
) {

    public static InsightsResponse from(InsightsResult result, boolean cached) {
        return new InsightsResponse(
                result.summary(),
                result.highlights(),
                result.suggestions(),
                cached);
    }
}
