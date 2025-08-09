package com.poc.postgres_key_value_cache.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/hello")
public class HelloController {
    @GetMapping
    public Map<String, String> helloWorld(){
        Map<String, String> res = Map.of("content", "Hello world");
        return res;
    }
}
