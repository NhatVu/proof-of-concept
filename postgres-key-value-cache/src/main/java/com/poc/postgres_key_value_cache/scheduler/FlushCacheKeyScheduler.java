package com.poc.postgres_key_value_cache.scheduler;

import com.poc.postgres_key_value_cache.repo.CacheRepository;
import com.poc.postgres_key_value_cache.service.CacheKeyRecorder;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@AllArgsConstructor
@Slf4j
public class FlushCacheKeyScheduler {
    private final CacheKeyRecorder cacheKeyRecorder;
    private final CacheRepository repo;

    // flush every minute
    @Scheduled(cron = "${app.flushCacheKey.cron}")
    public void flush() throws InterruptedException {
        log.info("Running job on thread: " + Thread.currentThread().getName());
        List<String> keys = new ArrayList<>(cacheKeyRecorder.keySet());
        if (keys.isEmpty()) return;
        keys.forEach(cacheKeyRecorder::remove);
        repo.batchUpdateLastAccessed(keys);
    }
}
