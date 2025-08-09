package com.poc.postgres_key_value_cache.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.poc.postgres_key_value_cache.service.CacheService;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/cache")
@AllArgsConstructor
public class CacheController {
    private final CacheService cacheService;
    private final ObjectMapper objectMapper;

    @GetMapping
    public Map<String, String> getValue(@RequestParam String key) throws JsonProcessingException {
        String value = cacheService.getValueForKey(key);
        Map<String, String> res = objectMapper.readValue(value, new TypeReference<Map<String, String>>() {});
        return res;
    }
}
