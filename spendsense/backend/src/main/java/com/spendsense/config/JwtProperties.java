package com.spendsense.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spendsense.jwt")
public record JwtProperties(String secret, long expirationMs) {
}
