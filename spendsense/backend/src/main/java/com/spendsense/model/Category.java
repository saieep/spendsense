package com.spendsense.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "INTEGER")
    private Long id;

    @Column(name = "user_id", nullable = false, columnDefinition = "INTEGER")
    private Long userId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String color;

    @Column(name = "is_default", nullable = false, columnDefinition = "INTEGER")
    private boolean isDefault;

    @Column(name = "created_at", nullable = false, columnDefinition = "TEXT")
    private String createdAt;

    protected Category() {
    }

    public Category(Long userId, String name, String color, boolean isDefault) {
        this.userId = userId;
        this.name = name;
        this.color = color;
        this.isDefault = isDefault;
    }

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now().toString();
        }
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getColor() {
        return color;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setColor(String color) {
        this.color = color;
    }
}
