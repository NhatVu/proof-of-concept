package com.poc.postgres_key_value_cache.model;

import jakarta.persistence.Column;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class CacheEntryRow {
    private String key;

    @Column(columnDefinition = "jsonb")
    private String value;
    private OffsetDateTime expiresAt;
    private OffsetDateTime lastAccessedAt;
    private boolean isRefreshing;
    private OffsetDateTime refreshStartedAt;
}
