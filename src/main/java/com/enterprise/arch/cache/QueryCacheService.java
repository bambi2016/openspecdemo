package com.enterprise.arch.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Set;
import java.util.function.Supplier;

@Service
public class QueryCacheService implements QueryCacheOperations {

    private static final Logger log = LoggerFactory.getLogger(QueryCacheService.class);

    private final CacheClient cacheClient;
    private final Duration defaultTtl;

    public QueryCacheService(CacheClient cacheClient,
                             @Value("${app.cache.redis.ttl-minutes:30}") long ttlMinutes) {
        this.cacheClient = cacheClient;
        this.defaultTtl = Duration.ofMinutes(ttlMinutes);
    }

    @Override
    public <T> T getOrLoad(String key, Supplier<T> loader) {
        T cached = read(key);
        if (cached != null) {
            return cached;
        }

        T loaded = loader.get();
        if (loaded != null) {
            write(key, loaded, defaultTtl);
        }
        return loaded;
    }

    @Override
    public void evict(String key) {
        try {
            cacheClient.delete(key);
        } catch (RuntimeException ex) {
            log.warn("Redis cache eviction failed, key={}", key, ex);
        }
    }

    @Override
    public void evictByPattern(String pattern) {
        try {
            Set<String> keys = cacheClient.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                cacheClient.delete(keys);
            }
        } catch (RuntimeException ex) {
            log.warn("Redis cache pattern eviction failed, pattern={}", pattern, ex);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T read(String key) {
        try {
            return (T) cacheClient.get(key);
        } catch (RuntimeException ex) {
            log.warn("Redis cache read failed, key={}", key, ex);
            return null;
        }
    }

    private void write(String key, Object value, Duration ttl) {
        try {
            cacheClient.set(key, value, ttl);
        } catch (RuntimeException ex) {
            log.warn("Redis cache write failed, key={}", key, ex);
        }
    }
}
