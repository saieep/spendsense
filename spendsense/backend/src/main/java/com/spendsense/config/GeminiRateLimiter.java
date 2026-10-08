package com.spendsense.config;

import java.time.LocalDate;
import java.time.ZoneOffset;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
@Profile({"dev", "prod"})
public class GeminiRateLimiter {

    private final GeminiProperties properties;
    private LocalDate countDate = LocalDate.now(ZoneOffset.UTC);
    private int dailyRequestCount;
    private long lastRequestAtMillis;

    public GeminiRateLimiter(GeminiProperties properties) {
        this.properties = properties;
    }

    public void checkAllowed() {
        if (!properties.isDevMode()) {
            return;
        }

        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        if (!today.equals(countDate)) {
            countDate = today;
            dailyRequestCount = 0;
        }

        if (dailyRequestCount >= properties.getDailyRequestLimit()) {
            throw new ResponseStatusException(
                    HttpStatus.TOO_MANY_REQUESTS,
                    "Gemini dev daily limit reached ("
                            + properties.getDailyRequestLimit()
                            + " requests). Resets at UTC midnight. Use MockAiService in tests.");
        }

        long minInterval = properties.getMinRequestIntervalMs();
        if (minInterval > 0 && lastRequestAtMillis > 0) {
            long elapsed = System.currentTimeMillis() - lastRequestAtMillis;
            if (elapsed < minInterval) {
                throw new ResponseStatusException(
                        HttpStatus.TOO_MANY_REQUESTS,
                        "Gemini dev rate limit: wait "
                                + (minInterval - elapsed)
                                + "ms before the next request.");
            }
        }
    }

    public void recordRequest() {
        if (!properties.isDevMode()) {
            return;
        }
        dailyRequestCount++;
        lastRequestAtMillis = System.currentTimeMillis();
    }

    public int getDailyRequestCount() {
        return dailyRequestCount;
    }
}
