package com.spendsense.dto;

public record CategoryResponse(Long id, String name, String color, boolean isDefault) {

    public static CategoryResponse from(com.spendsense.model.Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getColor(),
                category.isDefault()
        );
    }
}
