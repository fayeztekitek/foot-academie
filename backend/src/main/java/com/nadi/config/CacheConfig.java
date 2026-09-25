package com.nadi.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        // In-memory caches for reference data that changes rarely.
        // "publicTenants" feeds the login academy selector on every visit.
        return new ConcurrentMapCacheManager("publicTenants");
    }
}
