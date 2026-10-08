package com.spendsense.config;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

class GeminiRateLimiterTest {

    private GeminiProperties properties;
    private GeminiRateLimiter rateLimiter;

    @BeforeEach
    void setUp() {
        properties = new GeminiProperties();
        properties.setDevMode(true);
        properties.setDailyRequestLimit(2);
        properties.setMinRequestIntervalMs(1000);
        rateLimiter = new GeminiRateLimiter(properties);
    }

    @Test
    void checkAllowed_blocksWhenDailyLimitReached() {
        rateLimiter.recordRequest();
        rateLimiter.recordRequest();

        assertThatThrownBy(() -> rateLimiter.checkAllowed())
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException rse = (ResponseStatusException) ex;
                    org.assertj.core.api.Assertions.assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
                });
    }

    @Test
    void checkAllowed_skipsLimitsWhenNotDevMode() {
        properties.setDevMode(false);
        rateLimiter.recordRequest();
        rateLimiter.recordRequest();
        rateLimiter.recordRequest();

        rateLimiter.checkAllowed();
    }
}
