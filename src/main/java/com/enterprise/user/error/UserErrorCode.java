package com.enterprise.user.error;

import com.enterprise.common.error.ErrorCode;

public enum UserErrorCode implements ErrorCode {
    USERNAME_EXISTS(20001, "用户名已存在"),
    USERNAME_OR_PASSWORD_ERROR(20002, "用户名或密码错误"),
    USER_DISABLED(20003, "用户已禁用"),
    OLD_PASSWORD_ERROR(20004, "旧密码错误");

    private final int code;
    private final String message;

    UserErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
