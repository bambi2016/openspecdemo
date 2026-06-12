package com.enterprise.common.util;

public final class SensitiveMaskUtils {

    private SensitiveMaskUtils() {
    }

    public static String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return "***";
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    public static String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "***";
        }
        String[] parts = email.split("@", 2);
        String name = parts[0];
        if (name.length() <= 2) {
            return name.charAt(0) + "***@" + parts[1];
        }
        return name.charAt(0) + "***" + name.charAt(name.length() - 1) + "@" + parts[1];
    }

    public static String maskPassword(String password) {
        return password == null ? null : "******";
    }

    public static String maskToken(String token) {
        if (token == null || token.length() <= 12) {
            return "***";
        }
        return token.substring(0, 6) + "***" + token.substring(token.length() - 6);
    }

    public static String maskText(String value) {
        if (value == null) {
            return null;
        }
        String masked = value.replaceAll("(?i)(password\\s*[:=]\\s*)[^,}\\s]+", "$1******");
        masked = masked.replaceAll("(?i)(authorization\\s*[:=]\\s*Bearer\\s+)[^,}\\s]+", "$1******");
        return masked;
    }
}
