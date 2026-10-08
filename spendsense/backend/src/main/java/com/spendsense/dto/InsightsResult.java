package com.spendsense.dto;

import java.util.List;

public record InsightsResult(String summary, List<String> highlights, List<String> suggestions) {}
