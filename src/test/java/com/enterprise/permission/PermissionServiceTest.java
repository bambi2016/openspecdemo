package com.enterprise.permission;

import com.enterprise.arch.auth.LoginUser;
import com.enterprise.arch.auth.LoginUserContext;
import com.enterprise.common.exception.BizException;
import com.enterprise.permission.dto.RoleCreateDTO;
import com.enterprise.permission.dto.RolePermissionBindDTO;
import com.enterprise.permission.dto.UserRoleBindDTO;
import com.enterprise.permission.entity.SysRole;
import com.enterprise.permission.mapper.SysPermissionMapper;
import com.enterprise.permission.mapper.SysRoleMapper;
import com.enterprise.permission.mapper.SysRolePermissionMapper;
import com.enterprise.permission.mapper.SysUserRoleMapper;
import com.enterprise.permission.service.impl.PermissionServiceImpl;
import com.enterprise.user.mapper.SysUserMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PermissionServiceTest {

    private final SysRoleMapper roleMapper = mock(SysRoleMapper.class);
    private final SysPermissionMapper permissionMapper = mock(SysPermissionMapper.class);
    private final SysRolePermissionMapper rolePermissionMapper = mock(SysRolePermissionMapper.class);
    private final SysUserRoleMapper userRoleMapper = mock(SysUserRoleMapper.class);
    private final SysUserMapper userMapper = mock(SysUserMapper.class);
    private final PermissionServiceImpl permissionService = new PermissionServiceImpl(
            roleMapper, permissionMapper, rolePermissionMapper, userRoleMapper, userMapper);

    @AfterEach
    void clearContext() {
        LoginUserContext.clear();
    }

    @Test
    void createRoleSucceedsAndRejectsDuplicateRoleCode() {
        RoleCreateDTO dto = roleCreateDTO();
        when(roleMapper.countByRoleCode("ADMIN")).thenReturn(0);

        assertThat(permissionService.createRole(dto).getRoleCode()).isEqualTo("ADMIN");
        verify(roleMapper).insert(any(SysRole.class));

        when(roleMapper.countByRoleCode("ADMIN")).thenReturn(1);
        assertThatThrownBy(() -> permissionService.createRole(dto))
                .isInstanceOf(BizException.class)
                .extracting("code")
                .isEqualTo(30002);
    }

    @Test
    void bindRolePermissionsValidatesRoleAndPermissionsBeforeWriting() {
        RolePermissionBindDTO dto = new RolePermissionBindDTO();
        dto.setPermissionIds(List.of(1L, 2L));

        when(roleMapper.findById(9L)).thenReturn(null);
        assertThatThrownBy(() -> permissionService.bindRolePermissions(9L, dto))
                .isInstanceOf(BizException.class)
                .extracting("code")
                .isEqualTo(10005);
        verify(rolePermissionMapper, never()).deleteByRoleId(anyLong());

        when(roleMapper.findById(9L)).thenReturn(new SysRole());
        when(permissionMapper.countActiveByIds(List.of(1L, 2L))).thenReturn(1);
        assertThatThrownBy(() -> permissionService.bindRolePermissions(9L, dto))
                .isInstanceOf(BizException.class)
                .extracting("code")
                .isEqualTo(10005);

        when(permissionMapper.countActiveByIds(List.of(1L, 2L))).thenReturn(2);
        permissionService.bindRolePermissions(9L, dto);
        verify(rolePermissionMapper).deleteByRoleId(9L);
        verify(rolePermissionMapper).batchInsert(9L, List.of(1L, 2L));
    }

    @Test
    void bindUserRolesValidatesUserAndRoles() {
        UserRoleBindDTO dto = new UserRoleBindDTO();
        dto.setRoleIds(List.of(1L));

        when(userMapper.countById(7L)).thenReturn(0);
        assertThatThrownBy(() -> permissionService.bindUserRoles(7L, dto))
                .isInstanceOf(BizException.class)
                .extracting("code")
                .isEqualTo(10005);

        when(userMapper.countById(7L)).thenReturn(1);
        when(roleMapper.countActiveByIds(List.of(1L))).thenReturn(1);
        permissionService.bindUserRoles(7L, dto);
        verify(userRoleMapper).deleteByUserId(7L);
        verify(userRoleMapper).batchInsert(7L, List.of(1L));
    }

    @Test
    void currentPermissionCodesUsesContextAndDeduplicates() {
        assertThatThrownBy(permissionService::currentPermissionCodes)
                .isInstanceOf(BizException.class)
                .extracting("code")
                .isEqualTo(10001);

        LoginUserContext.set(new LoginUser(1L, "admin"));
        when(rolePermissionMapper.findPermissionCodesByUserId(1L))
                .thenReturn(List.of("role:create", "role:create", "permission:list"));

        assertThat(permissionService.currentPermissionCodes()).containsExactly("role:create", "permission:list");
        assertThat(permissionService.findPermissionCodesByUserId(1L)).isEqualTo(Set.of("role:create", "permission:list"));
    }

    private RoleCreateDTO roleCreateDTO() {
        RoleCreateDTO dto = new RoleCreateDTO();
        dto.setRoleCode("ADMIN");
        dto.setRoleName("管理员");
        dto.setDescription("admin");
        return dto;
    }
}
