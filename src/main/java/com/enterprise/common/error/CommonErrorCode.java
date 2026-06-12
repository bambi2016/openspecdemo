package com.enterprise.common.error;

public enum CommonErrorCode implements ErrorCode {
    SUCCESS(0, "SUCCESS"),
    TOKEN_EMPTY(10001, "未携带登录凭证"),
    TOKEN_INVALID(10002, "凭证失效"),
    PERMISSION_DENIED(10003, "无操作权限"),
    PARAM_VALID_ERROR(10004, "参数校验失败"),
    DATA_NOT_FOUND(10005, "数据不存在"),
    SYSTEM_ERROR(10000, "系统异常");

    private final int code;
    private final String message;

    CommonErrorCode(int code, String message) {
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
