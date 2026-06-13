package com.enterprise.arch.cache;

import java.util.function.Supplier;

public interface QueryCacheOperations {

    <T> T getOrLoad(String key, Supplier<T> loader);

    void evict(String key);

    void evictByPattern(String pattern);
}
