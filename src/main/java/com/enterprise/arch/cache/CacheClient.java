package com.enterprise.arch.cache;

import java.time.Duration;
import java.util.Collection;
import java.util.Set;

public interface CacheClient {

    Object get(String key);

    void set(String key, Object value, Duration ttl);

    void delete(String key);

    Set<String> keys(String pattern);

    void delete(Collection<String> keys);
}
