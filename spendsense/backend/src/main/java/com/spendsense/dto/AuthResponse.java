package com.spendsense.dto;

public record AuthResponse(String token, String email, Long userId) {
}
