package com.enterprise.permission.service.impl;

import com.enterprise.arch.auth.LoginUserContext;
import com.enterprise.common.error.CommonErrorCode;
import com.enterprise.common.exception.BizException;
import com.enterprise.permission.dto.RoleCreateDTO;
import com.enterprise.permission.dto.RolePermissionBindDTO;
import com.enterprise.permission.dto.UserRoleBindDTO;
import com.enterprise.permission.entity.SysRole;
import com.enterprise.permission.error.PermissionErrorCode;
import com.enterprise.permission.mapper.SysPermissionMapper;
import com.enterprise.permission.mapper.SysRoleMapper;
import com.enterprise.permission.mapper.SysRolePermissionMapper;
import com.enterprise.permission.mapper.SysUserRoleMapper;
import com.enterprise.permission.service.PermissionService;
import com.enterprise.permission.vo.PermissionVO;
import com.enterprise.permission.vo.RoleVO;
import com.enterprise.user.mapper.SysUserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class PermissionServiceImpl implements PermissionService {

    private final SysRoleMapper roleMapper;
    private final SysPermissionMapper permissionMapper;
    private final SysRolePermissionMapper rolePermissionMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysUserMapper userMapper;

    public PermissionServiceImpl(SysRoleMapper roleMapper,
                                 SysPermissionMapper permissionMapper,
                                 SysRolePermissionMapper rolePermissionMapper,
                                 SysUserRoleMapper userRoleMapper,
                                 SysUserMapper userMapper) {
        this.roleMapper = roleMapper;
        this.permissionMapper = permissionMapper;
        this.rolePermissionMapper = rolePermissionMapper;
        this.userRoleMapper = userRoleMapper;
        this.userMapper = userMapper;
    }

    @Override
    @Transactional
    public RoleVO createRole(RoleCreateDTO roleCreateDTO) {
        if (roleMapper.countByRoleCode(roleCreateDTO.getRoleCode()) > 0) {
            throw new BizException(PermissionErrorCode.ROLE_CODE_EXISTS);
        }
        SysRole role = new SysRole();
        role.setRoleCode(roleCreateDTO.getRoleCode());
        role.setRoleName(roleCreateDTO.getRoleName());
        role.setDescription(roleCreateDTO.getDescription());
        role.setStatus(1);
        role.setDeleted(0);
        roleMapper.insert(role);
        return RoleVO.from(role);
    }

    @Override
    @Transactional
    public void bindRolePermissions(Long roleId, RolePermissionBindDTO bindDTO) {
        if (roleMapper.findById(roleId) == null) {
            throw new BizException(CommonErrorCode.DATA_NOT_FOUND);
        }
        List<Long> permissionIds = bindDTO.getPermissionIds();
        if (permissionMapper.countActiveByIds(permissionIds) != permissionIds.size()) {
            throw new BizException(CommonErrorCode.DATA_NOT_FOUND);
        }
        rolePermissionMapper.deleteByRoleId(roleId);
        rolePermissionMapper.batchInsert(roleId, permissionIds);
    }

    @Override
    @Transactional
    public void bindUserRoles(Long userId, UserRoleBindDTO bindDTO) {
        if (userMapper.countById(userId) == 0) {
            throw new BizException(CommonErrorCode.DATA_NOT_FOUND);
        }
        List<Long> roleIds = bindDTO.getRoleIds();
        if (roleMapper.countActiveByIds(roleIds) != roleIds.size()) {
            throw new BizException(CommonErrorCode.DATA_NOT_FOUND);
        }
        userRoleMapper.deleteByUserId(userId);
        userRoleMapper.batchInsert(userId, roleIds);
    }

    @Override
    public List<String> currentPermissionCodes() {
        Long userId = LoginUserContext.getUserId();
        if (userId == null) {
            throw new BizException(CommonErrorCode.TOKEN_EMPTY);
        }
        return List.copyOf(findPermissionCodesByUserId(userId));
    }

    @Override
    public Set<String> findPermissionCodesByUserId(Long userId) {
        return new LinkedHashSet<>(rolePermissionMapper.findPermissionCodesByUserId(userId));
    }

    @Override
    public List<PermissionVO> listPermissions() {
        return permissionMapper.findAllActive().stream().map(PermissionVO::from).toList();
    }
}
