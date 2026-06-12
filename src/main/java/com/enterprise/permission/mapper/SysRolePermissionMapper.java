package com.enterprise.permission.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysRolePermissionMapper {

    int deleteByRoleId(@Param("roleId") Long roleId);

    int batchInsert(@Param("roleId") Long roleId, @Param("permissionIds") List<Long> permissionIds);

    List<String> findPermissionCodesByUserId(@Param("userId") Long userId);
}
