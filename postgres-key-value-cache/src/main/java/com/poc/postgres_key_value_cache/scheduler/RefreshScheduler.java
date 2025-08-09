package com.poc.postgres_key_value_cache.scheduler;

import com.poc.postgres_key_value_cache.repo.CacheRepository;
import com.poc.postgres_key_value_cache.service.CacheService;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Random;

@Component
@AllArgsConstructor
public class RefreshScheduler {
    private final CacheRepository repo;
    private final CacheService service;
    private final Random rnd = new Random();

    // run every minute with small jitter
    @Scheduled(cron = "0 * * * * *")
    public void runBatchRefresh() {
        try {
            Thread.sleep(rnd.nextInt(20) * 1000L); // 0-20s jitter
        } catch (InterruptedException ignored) {}

        List<String> candidates = repo.selectRefreshCandidateKeys(50);
        for (String key : candidates) {
            service.refreshIfNeeded(key, 300);
        }

        // LRU cleanup: once every hour (example)
        // Could be another scheduled method; simplified here
        repo.deleteOldKeys(7);
    }
}
