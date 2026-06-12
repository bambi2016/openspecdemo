package com.enterprise.permission.mapper;

import com.enterprise.permission.entity.SysRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysRoleMapper {

    int insert(SysRole role);

    SysRole findById(@Param("id") Long id);

    int countByRoleCode(@Param("roleCode") String roleCode);

    int countActiveByIds(@Param("ids") List<Long> ids);
}
