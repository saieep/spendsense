package com.spendsense.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spendsense.gemini")
public class GeminiProperties {

    private boolean enabled = false;
    private boolean devMode = false;
    private String apiKey = "";
    private String model = "gemini-2.5-flash";
    private int categorizeMaxTokens = 200;
    private int insightsMaxTokens = 500;
    /** Dev safety cap — Google AI Studio free tier is ~1500 requests/day. */
    private int dailyRequestLimit = 100;
    /** Dev spacing between calls — free tier is ~15 requests/minute. */
    private long minRequestIntervalMs = 4000;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isDevMode() {
        return devMode;
    }

    public void setDevMode(boolean devMode) {
        this.devMode = devMode;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public int getCategorizeMaxTokens() {
        return categorizeMaxTokens;
    }

    public void setCategorizeMaxTokens(int categorizeMaxTokens) {
        this.categorizeMaxTokens = categorizeMaxTokens;
    }

    public int getInsightsMaxTokens() {
        return insightsMaxTokens;
    }

    public void setInsightsMaxTokens(int insightsMaxTokens) {
        this.insightsMaxTokens = insightsMaxTokens;
    }

    public int getDailyRequestLimit() {
        return dailyRequestLimit;
    }

    public void setDailyRequestLimit(int dailyRequestLimit) {
        this.dailyRequestLimit = dailyRequestLimit;
    }

    public long getMinRequestIntervalMs() {
        return minRequestIntervalMs;
    }

    public void setMinRequestIntervalMs(long minRequestIntervalMs) {
        this.minRequestIntervalMs = minRequestIntervalMs;
    }

    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }
}
