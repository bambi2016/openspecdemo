package com.enterprise.permission.vo;

import com.enterprise.permission.entity.SysPermission;

public class PermissionVO {

    private Long id;
    private String permCode;
    private String permName;
    private String description;

    public static PermissionVO from(SysPermission permission) {
        PermissionVO vo = new PermissionVO();
        vo.setId(permission.getId());
        vo.setPermCode(permission.getPermCode());
        vo.setPermName(permission.getPermName());
        vo.setDescription(permission.getDescription());
        return vo;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPermCode() {
        return permCode;
    }

    public void setPermCode(String permCode) {
        this.permCode = permCode;
    }

    public String getPermName() {
        return permName;
    }

    public void setPermName(String permName) {
        this.permName = permName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
