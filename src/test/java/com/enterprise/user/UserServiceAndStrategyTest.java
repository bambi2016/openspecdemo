package com.enterprise.user;

import com.enterprise.arch.auth.LoginUser;
import com.enterprise.arch.auth.LoginUserContext;
import com.enterprise.arch.cache.CacheKeyUtils;
import com.enterprise.arch.cache.QueryCacheOperations;
import com.enterprise.arch.jwt.JwtProperties;
import com.enterprise.arch.jwt.JwtUtils;
import com.enterprise.common.exception.BizException;
import com.enterprise.common.util.PasswordUtils;
import com.enterprise.user.dto.LoginDTO;
import com.enterprise.user.dto.PasswordUpdateDTO;
import com.enterprise.user.dto.RegisterDTO;
import com.enterprise.user.entity.SysUser;
import com.enterprise.user.mapper.SysUserMapper;
import com.enterprise.user.service.impl.UserServiceImpl;
import com.enterprise.user.strategy.LoginContext;
import com.enterprise.user.strategy.LoginStrategy;
import com.enterprise.user.strategy.PasswordLoginStrategy;
import com.enterprise.user.vo.LoginVO;
import com.enterprise.user.vo.UserVO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserServiceAndStrategyTest {

    private final SysUserMapper userMapper = mock(SysUserMapper.class);
    private final QueryCacheOperations queryCacheOperations = mock(QueryCacheOperations.class);
    private final JwtUtils jwtUtils = new JwtUtils(jwtProperties());
    private final PasswordLoginStrategy passwordStrategy = new PasswordLoginStrategy(userMapper, jwtUtils);
    private final UserServiceImpl userService = new UserServiceImpl(
            userMapper, new LoginContext(List.of(passwordStrategy)), queryCacheOperations);

    @AfterEach
    void clearContext() {
        LoginUserContext.clear();
    }

    @Test
    void registerCreatesUserWithEncodedPassword() {
        RegisterDTO dto = registerDTO();
        when(userMapper.countByUsername("neo")).thenReturn(0);

        UserVO vo = userService.register(dto);

        assertThat(vo.getUsername()).isEqualTo("neo");
        verify(userMapper).insert(any(SysUser.class));
    }

    @Test
    void registerRejectsDuplicateUsername() {
        when(userMapper.countByUsername("neo")).thenReturn(1);

        assertThatThrownBy(() -> userService.register(registerDTO()))
                .isInstanceOf(BizException.class)
                .extracting("code")
                .isEqualTo(20001);
    }

    @Test
    void passwordLoginSucceedsAndUpdatesLastLoginTime() {
        when(userMapper.findByUsername("neo")).thenReturn(user(1L, 1, PasswordUtils.encode("secret123")));
        LoginDTO dto = loginDTO("neo", "secret123");

        LoginVO vo = passwordStrategy.login(dto);

        assertThat(vo.getToken()).isNotBlank();
        assertThat(vo.getUser().getPhone()).isEqualTo("138****0000");
        verify(userMapper).updateLastLoginTime(anyLong(), any());
    }

    @Test
    void passwordLoginRejectsBadPasswordAndDisabledUser() {
        when(userMapper.findByUsername("neo")).thenReturn(user(1L, 1, PasswordUtils.encode("secret123")));
        assertThatThrownBy(() -> passwordStrategy.login(loginDTO("neo", "bad")))
                .isInstanceOf(BizException.class)
                .extracting("code")
                .isEqualTo(20002);

        when(userMapper.findByUsername("neo")).thenReturn(user(1L, 0, PasswordUtils.encode("secret123")));
        assertThatThrownBy(() -> passwordStrategy.login(loginDTO("neo", "secret123")))
                .isInstanceOf(BizException.class)
                .extracting("code")
                .isEqualTo(20003);
    }

    @Test
    void loginContextRejectsUnsupportedLoginType() {
        LoginDTO dto = loginDTO("neo", "secret123");
        dto.setLoginType("SMS");

        assertThatThrownBy(() -> new LoginContext(List.<LoginStrategy>of()).login(dto))
                .isInstanceOf(BizException.class)
                .extracting("code")
                .isEqualTo(10004);
    }

    @Test
    void currentUserReturnsMaskedUserAndRejectsMissingContext() {
        assertThatThrownBy(userService::currentUser)
                .isInstanceOf(BizException.class)
                .extracting("code")
                .isEqualTo(10001);

        LoginUserContext.set(new LoginUser(1L, "neo"));
        cacheMissLoadsCurrentUser(1L);
        when(userMapper.findById(1L)).thenReturn(user(1L, 1, PasswordUtils.encode("secret123")));

        UserVO vo = userService.currentUser();

        assertThat(vo.getEmail()).isEqualTo("n***o@example.com");
    }

    @Test
    void currentUserReturnsCachedValueWithoutQueryingMapper() {
        LoginUserContext.set(new LoginUser(1L, "neo"));
        UserVO cached = new UserVO();
        cached.setId(1L);
        cached.setUsername("neo");
        when(queryCacheOperations.<UserVO>getOrLoad(eq(CacheKeyUtils.currentUser(1L)), any())).thenReturn(cached);

        UserVO vo = userService.currentUser();

        assertThat(vo.getUsername()).isEqualTo("neo");
        verify(userMapper, never()).findById(anyLong());
    }

    @Test
    void updatePasswordChecksOldPasswordAndSavesEncodedNewPassword() {
        LoginUserContext.set(new LoginUser(1L, "neo"));
        when(userMapper.findById(1L)).thenReturn(user(1L, 1, PasswordUtils.encode("old123")));
        PasswordUpdateDTO dto = new PasswordUpdateDTO();
        dto.setOldPassword("old123");
        dto.setNewPassword("new123");

        userService.updatePassword(dto);

        verify(userMapper).updatePassword(anyLong(), anyString());
        verify(queryCacheOperations).evictByPattern(CacheKeyUtils.userCachePattern(1L));

        dto.setOldPassword("bad");
        assertThatThrownBy(() -> userService.updatePassword(dto))
                .isInstanceOf(BizException.class)
                .extracting("code")
                .isEqualTo(20004);
    }

    private RegisterDTO registerDTO() {
        RegisterDTO dto = new RegisterDTO();
        dto.setUsername("neo");
        dto.setPassword("secret123");
        dto.setNickname("Neo");
        dto.setPhone("13800000000");
        dto.setEmail("neo@example.com");
        return dto;
    }

    private LoginDTO loginDTO(String username, String password) {
        LoginDTO dto = new LoginDTO();
        dto.setUsername(username);
        dto.setPassword(password);
        return dto;
    }

    private SysUser user(Long id, int status, String password) {
        SysUser user = new SysUser();
        user.setId(id);
        user.setUsername("neo");
        user.setPassword(password);
        user.setNickname("Neo");
        user.setPhone("13800000000");
        user.setEmail("neo@example.com");
        user.setStatus(status);
        user.setDeleted(0);
        return user;
    }

    private JwtProperties jwtProperties() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret("test-secret-for-user-module");
        properties.setExpirationSeconds(60);
        return properties;
    }

    @SuppressWarnings("unchecked")
    private void cacheMissLoadsCurrentUser(Long userId) {
        when(queryCacheOperations.<UserVO>getOrLoad(eq(CacheKeyUtils.currentUser(userId)), any()))
                .thenAnswer(invocation -> ((Supplier<UserVO>) invocation.getArgument(1)).get());
    }
}
