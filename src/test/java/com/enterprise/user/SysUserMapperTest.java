package com.enterprise.user;

import com.enterprise.user.entity.SysUser;
import com.enterprise.user.mapper.SysUserMapper;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;

import static org.assertj.core.api.Assertions.assertThat;

@MybatisTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class SysUserMapperTest {

    @Autowired
    private SysUserMapper userMapper;

    @Test
    void findsExistingUserAndFiltersDeletedRows() {
        assertThat(userMapper.countByUsername("admin")).isEqualTo(1);
        assertThat(userMapper.countByUsername("deleted")).isZero();
        assertThat(userMapper.findByUsername("admin").getStatus()).isEqualTo(1);
    }

    @Test
    void insertAndUpdateUser() {
        SysUser user = new SysUser();
        user.setUsername("trinity");
        user.setPassword("encoded");
        user.setNickname("Trinity");
        user.setPhone("13600000000");
        user.setEmail("trinity@example.com");
        user.setStatus(1);
        user.setDeleted(0);

        userMapper.insert(user);
        assertThat(user.getId()).isNotNull();
        assertThat(userMapper.updatePassword(user.getId(), "changed")).isEqualTo(1);
        assertThat(userMapper.findById(user.getId()).getPassword()).isEqualTo("changed");
    }
}
