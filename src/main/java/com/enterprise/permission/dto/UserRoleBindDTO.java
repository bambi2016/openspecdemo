package com.enterprise.permission.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public class UserRoleBindDTO {

    @NotEmpty
    private List<Long> roleIds;

    public List<Long> getRoleIds() {
        return roleIds;
    }

    public void setRoleIds(List<Long> roleIds) {
        this.roleIds = roleIds;
    }
}
