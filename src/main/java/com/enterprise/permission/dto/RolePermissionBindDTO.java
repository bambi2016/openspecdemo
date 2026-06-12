package com.enterprise.permission.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public class RolePermissionBindDTO {

    @NotEmpty
    private List<Long> permissionIds;

    public List<Long> getPermissionIds() {
        return permissionIds;
    }

    public void setPermissionIds(List<Long> permissionIds) {
        this.permissionIds = permissionIds;
    }
}
