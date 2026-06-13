package com.enterprise.arch;

import com.enterprise.arch.cache.CacheClient;
import com.enterprise.arch.cache.CacheKeyUtils;
import com.enterprise.arch.cache.QueryCacheService;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class QueryCacheServiceTest {

    @Test
    void getOrLoadWritesLoadedValueWithThirtyMinuteTtlOnMiss() {
        FakeCacheClient cacheClient = new FakeCacheClient();
        QueryCacheService queryCacheService = new QueryCacheService(cacheClient, 30);
        AtomicInteger loads = new AtomicInteger();

        String value = queryCacheService.getOrLoad("cache:test", () -> {
            loads.incrementAndGet();
            return "database";
        });

        assertThat(value).isEqualTo("database");
        assertThat(loads).hasValue(1);
        assertThat(cacheClient.values).containsEntry("cache:test", "database");
        assertThat(cacheClient.ttls).containsEntry("cache:test", Duration.ofMinutes(30));
    }

    @Test
    void getOrLoadReturnsCachedValueWithoutCallingLoader() {
        FakeCacheClient cacheClient = new FakeCacheClient();
        QueryCacheService queryCacheService = new QueryCacheService(cacheClient, 30);
        cacheClient.values.put("cache:test", "cached");

        String value = queryCacheService.getOrLoad("cache:test", () -> {
            throw new AssertionError("loader should not be called on cache hit");
        });

        assertThat(value).isEqualTo("cached");
    }

    @Test
    void getOrLoadFallsBackToLoaderWhenRedisReadOrWriteFails() {
        FakeCacheClient cacheClient = new FakeCacheClient();
        QueryCacheService queryCacheService = new QueryCacheService(cacheClient, 30);
        cacheClient.failGet = true;
        cacheClient.failSet = true;

        String value = queryCacheService.getOrLoad("cache:test", () -> "database");

        assertThat(value).isEqualTo("database");
    }

    @Test
    void evictByPatternDeletesMatchedKeysAndIgnoresRedisFailure() {
        FakeCacheClient cacheClient = new FakeCacheClient();
        QueryCacheService queryCacheService = new QueryCacheService(cacheClient, 30);
        cacheClient.values.put("cache:a", "a");
        cacheClient.values.put("cache:b", "b");
        cacheClient.values.put("other:c", "c");

        queryCacheService.evictByPattern("cache:*");
        cacheClient.failKeys = true;
        queryCacheService.evictByPattern("cache:missing:*");

        assertThat(cacheClient.values).containsOnlyKeys("other:c");
    }

    @Test
    void cacheKeysSeparateUsersAndQueryParameters() {
        assertThat(CacheKeyUtils.currentUser(1L)).isEqualTo("cache:user:me:user:1");
        assertThat(CacheKeyUtils.currentUser(1L)).isNotEqualTo(CacheKeyUtils.currentUser(2L));
        assertThat(CacheKeyUtils.currentPermissionCodes(1L)).isEqualTo("cache:permission:codes:user:1");

        String first = CacheKeyUtils.userScopedQuery("order", "list", 1L, Map.of("page", 1, "size", 20));
        String sameParamsDifferentOrder = CacheKeyUtils.userScopedQuery("order", "list", 1L, Map.of("size", 20, "page", 1));
        String differentParams = CacheKeyUtils.userScopedQuery("order", "list", 1L, Map.of("page", 2, "size", 20));

        assertThat(first).isEqualTo(sameParamsDifferentOrder);
        assertThat(first).isNotEqualTo(differentParams);
    }

    private static class FakeCacheClient implements CacheClient {
        private final Map<String, Object> values = new HashMap<>();
        private final Map<String, Duration> ttls = new HashMap<>();
        private boolean failGet;
        private boolean failSet;
        private boolean failKeys;

        @Override
        public Object get(String key) {
            if (failGet) {
                throw new RuntimeException("read failed");
            }
            return values.get(key);
        }

        @Override
        public void set(String key, Object value, Duration ttl) {
            if (failSet) {
                throw new RuntimeException("write failed");
            }
            values.put(key, value);
            ttls.put(key, ttl);
        }

        @Override
        public void delete(String key) {
            values.remove(key);
        }

        @Override
        public Set<String> keys(String pattern) {
            if (failKeys) {
                throw new RuntimeException("keys failed");
            }
            String prefix = pattern.substring(0, pattern.indexOf('*'));
            Set<String> matched = new HashSet<>();
            values.keySet().stream().filter(key -> key.startsWith(prefix)).forEach(matched::add);
            return matched;
        }

        @Override
        public void delete(Collection<String> keys) {
            keys.forEach(values::remove);
        }
    }
}
