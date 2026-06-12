package com.enterprise.permission.controller;

import com.enterprise.common.annotation.Perm;
import com.enterprise.common.response.Result;
import com.enterprise.permission.dto.RoleCreateDTO;
import com.enterprise.permission.dto.RolePermissionBindDTO;
import com.enterprise.permission.dto.UserRoleBindDTO;
import com.enterprise.permission.service.PermissionService;
import com.enterprise.permission.vo.PermissionVO;
import com.enterprise.permission.vo.RoleVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/permission")
public class PermissionController {

    private final PermissionService permissionService;

    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @Perm("role:create")
    @PostMapping("/roles")
    public Result<RoleVO> createRole(@Valid @RequestBody RoleCreateDTO roleCreateDTO) {
        return Result.success(permissionService.createRole(roleCreateDTO));
    }

    @Perm("role:bind-permission")
    @PutMapping("/roles/{roleId}/permissions")
    public Result<Void> bindRolePermissions(@PathVariable Long roleId, @Valid @RequestBody RolePermissionBindDTO bindDTO) {
        permissionService.bindRolePermissions(roleId, bindDTO);
        return Result.success();
    }

    @Perm("user:bind-role")
    @PutMapping("/users/{userId}/roles")
    public Result<Void> bindUserRoles(@PathVariable Long userId, @Valid @RequestBody UserRoleBindDTO bindDTO) {
        permissionService.bindUserRoles(userId, bindDTO);
        return Result.success();
    }

    @GetMapping("/current")
    public Result<List<String>> currentPermissions() {
        return Result.success(permissionService.currentPermissionCodes());
    }

    @Perm("permission:list")
    @GetMapping("/permissions")
    public Result<List<PermissionVO>> listPermissions() {
        return Result.success(permissionService.listPermissions());
    }
}
