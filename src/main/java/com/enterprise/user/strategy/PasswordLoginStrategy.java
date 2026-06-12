package com.enterprise.user.strategy;

import com.enterprise.arch.jwt.JwtUtils;
import com.enterprise.common.exception.BizException;
import com.enterprise.common.util.PasswordUtils;
import com.enterprise.user.dto.LoginDTO;
import com.enterprise.user.entity.SysUser;
import com.enterprise.user.enums.LoginType;
import com.enterprise.user.error.UserErrorCode;
import com.enterprise.user.mapper.SysUserMapper;
import com.enterprise.user.vo.LoginVO;
import com.enterprise.user.vo.UserVO;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class PasswordLoginStrategy implements LoginStrategy {

    private final SysUserMapper userMapper;
    private final JwtUtils jwtUtils;

    public PasswordLoginStrategy(SysUserMapper userMapper, JwtUtils jwtUtils) {
        this.userMapper = userMapper;
        this.jwtUtils = jwtUtils;
    }

    @Override
    public boolean supports(String loginType) {
        return loginType == null || LoginType.PASSWORD.name().equalsIgnoreCase(loginType);
    }

    @Override
    public LoginVO login(LoginDTO loginDTO) {
        SysUser user = userMapper.findByUsername(loginDTO.getUsername());
        if (user == null || !PasswordUtils.matches(loginDTO.getPassword(), user.getPassword())) {
            throw new BizException(UserErrorCode.USERNAME_OR_PASSWORD_ERROR);
        }
        if (Integer.valueOf(0).equals(user.getStatus())) {
            throw new BizException(UserErrorCode.USER_DISABLED);
        }
        LocalDateTime now = LocalDateTime.now();
        userMapper.updateLastLoginTime(user.getId(), now);
        user.setLastLoginTime(now);
        String token = jwtUtils.generateToken(user.getId(), user.getUsername());
        return new LoginVO(token, jwtUtils.getExpirationSeconds(), UserVO.from(user));
    }
}
