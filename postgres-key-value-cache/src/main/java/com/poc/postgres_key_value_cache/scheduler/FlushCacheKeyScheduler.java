package com.poc.postgres_key_value_cache.scheduler;

import com.poc.postgres_key_value_cache.repo.CacheRepository;
import com.poc.postgres_key_value_cache.service.CacheKeyRecorder;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@AllArgsConstructor
public class FlushCacheKeyScheduler {
    private final CacheKeyRecorder cacheKeyRecorder;
    private final CacheRepository repo;

    // flush every minute
    @Scheduled(fixedDelayString = "${app.batch.last-access-flush-interval-seconds:60}")
    public void flush() {
        List<String> keys = new ArrayList<>(cacheKeyRecorder.keySet());
        if (keys.isEmpty()) return;
        keys.forEach(cacheKeyRecorder::remove);
        repo.batchUpdateLastAccessed(keys);
    }
}
