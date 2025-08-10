package com.poc.postgres_key_value_cache;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PostgresKeyValueCacheApplication {

	public static void main(String[] args) {
		SpringApplication.run(PostgresKeyValueCacheApplication.class, args);
	}

}
