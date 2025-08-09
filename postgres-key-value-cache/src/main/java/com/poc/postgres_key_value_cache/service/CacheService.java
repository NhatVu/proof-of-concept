package com.poc.postgres_key_value_cache.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.poc.postgres_key_value_cache.repo.CacheRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@AllArgsConstructor
public class CacheService {
    private final CacheRepository repo;
    private final CacheKeyRecorder recorder;
    private final ObjectMapper objectMapper;

    public String getValueForKey(String key) throws JsonProcessingException {
        // 1. Try cache
        recorder.record(key); // record this key as active key
        Optional<String> cachedValue = repo.get(key);
        if (cachedValue.isPresent()) {
            log.info("Found in cache: " + key);
            return cachedValue.get();
        }

        // 2. Fallback to DB
        log.info("⚠ Cache miss. Querying DB for: " + key);
        String dbValue = computeAndStore(key);

        return dbValue;
    }

    public Optional<String> getIfFresh(String key) {
        Optional<String> value = repo.findValueIfFresh(key);
        value.ifPresent(v -> recorder.record(key));
        return value;
    }

    public String computeAndStore(String key) throws JsonProcessingException {
        // Placeholder: expensive compute; replace with real computation
        try {
            TimeUnit.SECONDS.sleep(2); // Sleep for 1 second
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        String result = ("computed-" + key);
        result = objectMapper.writeValueAsString(Map.of("content", result));
        OffsetDateTime expiresAt = OffsetDateTime.now(ZoneOffset.UTC).plusMinutes(10);
        repo.upsert(key, result, expiresAt);
        return result;
    }

    public void refreshIfNeeded(String key, int staleLockSeconds) {
        boolean locked = repo.tryAcquireRefreshLock(key, staleLockSeconds);
        if (!locked) return;

        try {
            // expensive compute
            // Placeholder: expensive compute; replace with real computation
            TimeUnit.SECONDS.sleep(2); // Sleep for 1 second

            String result = ("refreshed-" + key);
            result = objectMapper.writeValueAsString(Map.of("content", result));
            OffsetDateTime expiresAt = OffsetDateTime.now(ZoneOffset.UTC).plusMinutes(10);
            repo.releaseLockAndUpdate(key, result, expiresAt);
        } catch (Exception e) {
            // on error, make sure to release lock
            repo.forceReleaseLock(key);
        }
    }
}
