package com.enterprise.permission.mapper;

import com.enterprise.permission.entity.SysPermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysPermissionMapper {

    SysPermission findById(@Param("id") Long id);

    int countByPermCode(@Param("permCode") String permCode);

    int countActiveByIds(@Param("ids") List<Long> ids);

    List<SysPermission> findAllActive();
}
