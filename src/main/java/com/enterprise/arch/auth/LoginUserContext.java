package com.enterprise.arch.auth;

public final class LoginUserContext {

    private static final ThreadLocal<LoginUser> HOLDER = new ThreadLocal<>();

    private LoginUserContext() {
    }

    public static void set(LoginUser loginUser) {
        HOLDER.set(loginUser);
    }

    public static LoginUser get() {
        return HOLDER.get();
    }

    public static Long getUserId() {
        LoginUser loginUser = get();
        return loginUser == null ? null : loginUser.getUserId();
    }

    public static void clear() {
        HOLDER.remove();
    }
}
