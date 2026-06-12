package com.enterprise.permission.error;

import com.enterprise.common.error.ErrorCode;

public enum PermissionErrorCode implements ErrorCode {
    PERMISSION_CODE_EXISTS(30001, "权限标识已存在"),
    ROLE_CODE_EXISTS(30002, "角色编码已存在"),
    ROLE_HAS_USERS(30003, "角色已绑定用户"),
    PERMISSION_CODE_INVALID(30004, "权限标识格式非法");

    private final int code;
    private final String message;

    PermissionErrorCode(int code, String message) {
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
