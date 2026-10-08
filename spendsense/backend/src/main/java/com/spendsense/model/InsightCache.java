package com.spendsense.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "insight_cache")
public class InsightCache {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "INTEGER")
    private Long id;

    @Column(name = "user_id", nullable = false, columnDefinition = "INTEGER")
    private Long userId;

    @Column(name = "period_start", nullable = false, columnDefinition = "TEXT")
    private String periodStart;

    @Column(name = "period_end", nullable = false, columnDefinition = "TEXT")
    private String periodEnd;

    @Column(name = "content_json", nullable = false, columnDefinition = "TEXT")
    private String contentJson;

    @Column(name = "created_at", nullable = false, columnDefinition = "TEXT")
    private String createdAt;

    protected InsightCache() {
    }

    public InsightCache(Long userId, String periodStart, String periodEnd, String contentJson, String createdAt) {
        this.userId = userId;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.contentJson = contentJson;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getPeriodStart() {
        return periodStart;
    }

    public String getPeriodEnd() {
        return periodEnd;
    }

    public String getContentJson() {
        return contentJson;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void updateContent(String contentJson, String createdAt) {
        this.contentJson = contentJson;
        this.createdAt = createdAt;
    }
}
