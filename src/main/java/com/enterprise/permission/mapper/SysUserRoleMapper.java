package com.enterprise.permission.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysUserRoleMapper {

    int deleteByUserId(@Param("userId") Long userId);

    int batchInsert(@Param("userId") Long userId, @Param("roleIds") List<Long> roleIds);
}
