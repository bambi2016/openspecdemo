package com.enterprise.arch.cache;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

public final class CacheKeyUtils {

    private CacheKeyUtils() {
    }

    public static String currentUser(Long userId) {
        return "cache:user:me:user:" + requireUserId(userId);
    }

    public static String currentPermissionCodes(Long userId) {
        return "cache:permission:codes:user:" + requireUserId(userId);
    }

    public static String userCachePattern(Long userId) {
        return "cache:user:*:user:" + requireUserId(userId) + "*";
    }

    public static String userPermissionCachePattern(Long userId) {
        return "cache:permission:*:user:" + requireUserId(userId) + "*";
    }

    public static String userScopedQuery(String module, String operation, Long userId, Map<String, ?> queryParams) {
        String normalized = normalize(queryParams);
        return "cache:%s:%s:user:%d:q:%s".formatted(module, operation, requireUserId(userId), sha256Short(normalized));
    }

    private static Long requireUserId(Long userId) {
        return Objects.requireNonNull(userId, "userId must not be null");
    }

    private static String normalize(Map<String, ?> queryParams) {
        if (queryParams == null || queryParams.isEmpty()) {
            return "";
        }
        TreeMap<String, Object> sorted = new TreeMap<>();
        sorted.putAll(queryParams);
        StringBuilder builder = new StringBuilder();
        sorted.forEach((key, value) -> builder.append(key).append('=').append(value).append(';'));
        return builder.toString();
    }

    private static String sha256Short(String source) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(source.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (int i = 0; i < 8 && i < bytes.length; i++) {
                hex.append("%02x".formatted(bytes[i]));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is not available", ex);
        }
    }
}
