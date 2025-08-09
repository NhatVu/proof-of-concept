package com.poc.postgres_key_value_cache.service;

import org.springframework.stereotype.Component;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class CacheKeyRecorder {
    private final ConcurrentHashMap<String, Boolean> accessed = new ConcurrentHashMap<>();

    public void record(String key) {
        accessed.put(key, Boolean.TRUE);
    }

    public void remove(String key){
        accessed.remove(key);
    }

    public Set<String> keySet(){
        return accessed.keySet();
    }

}
