package com.enterprise.permission.service;

import com.enterprise.arch.permission.PermissionCodeProvider;
import com.enterprise.permission.dto.RoleCreateDTO;
import com.enterprise.permission.dto.RolePermissionBindDTO;
import com.enterprise.permission.dto.UserRoleBindDTO;
import com.enterprise.permission.vo.PermissionVO;
import com.enterprise.permission.vo.RoleVO;

import java.util.List;

public interface PermissionService extends PermissionCodeProvider {

    RoleVO createRole(RoleCreateDTO roleCreateDTO);

    void bindRolePermissions(Long roleId, RolePermissionBindDTO bindDTO);

    void bindUserRoles(Long userId, UserRoleBindDTO bindDTO);

    List<String> currentPermissionCodes();

    List<PermissionVO> listPermissions();
}
