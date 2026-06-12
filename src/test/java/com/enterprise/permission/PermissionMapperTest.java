package com.enterprise.permission;

import com.enterprise.permission.entity.SysRole;
import com.enterprise.permission.mapper.SysPermissionMapper;
import com.enterprise.permission.mapper.SysRoleMapper;
import com.enterprise.permission.mapper.SysRolePermissionMapper;
import com.enterprise.permission.mapper.SysUserRoleMapper;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@MybatisTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class PermissionMapperTest {

    @Autowired
    private SysRoleMapper roleMapper;
    @Autowired
    private SysPermissionMapper permissionMapper;
    @Autowired
    private SysRolePermissionMapper rolePermissionMapper;
    @Autowired
    private SysUserRoleMapper userRoleMapper;

    @Test
    void roleAndPermissionQueriesFilterInactiveRows() {
        assertThat(roleMapper.countByRoleCode("ADMIN")).isEqualTo(1);
        assertThat(roleMapper.findById(1L)).isNotNull();
        assertThat(roleMapper.findById(2L)).isNull();
        assertThat(permissionMapper.countActiveByIds(List.of(1L, 2L))).isEqualTo(2);
        assertThat(permissionMapper.countActiveByIds(List.of(1L, 3L))).isEqualTo(1);
        assertThat(permissionMapper.findAllActive()).hasSize(2);
    }

    @Test
    void relationQueriesReturnDistinctActivePermissionCodes() {
        assertThat(rolePermissionMapper.findPermissionCodesByUserId(1L))
                .containsExactly("permission:list", "role:create");
    }

    @Test
    void relationReplacementUsesLogicalDeleteAndInsert() {
        assertThat(userRoleMapper.deleteByUserId(1L)).isGreaterThan(0);
        assertThat(userRoleMapper.batchInsert(1L, List.of(1L))).isEqualTo(1);

        assertThat(rolePermissionMapper.deleteByRoleId(1L)).isGreaterThan(0);
        assertThat(rolePermissionMapper.batchInsert(1L, List.of(1L))).isEqualTo(1);
    }

    @Test
    void insertRoleAssignsId() {
        SysRole role = new SysRole();
        role.setRoleCode("AUDITOR");
        role.setRoleName("审计员");
        role.setDescription("audit");
        role.setStatus(1);
        role.setDeleted(0);

        roleMapper.insert(role);

        assertThat(role.getId()).isNotNull();
        assertThat(roleMapper.countByRoleCode("AUDITOR")).isEqualTo(1);
    }
}
