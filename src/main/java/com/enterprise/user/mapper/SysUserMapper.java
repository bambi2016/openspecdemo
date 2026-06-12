package com.enterprise.user.mapper;

import com.enterprise.user.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

@Mapper
public interface SysUserMapper {

    int insert(SysUser user);

    SysUser findById(@Param("id") Long id);

    SysUser findByUsername(@Param("username") String username);

    int countByUsername(@Param("username") String username);

    int updatePassword(@Param("id") Long id, @Param("password") String password);

    int updateLastLoginTime(@Param("id") Long id, @Param("lastLoginTime") LocalDateTime lastLoginTime);

    int countById(@Param("id") Long id);
}
