package com.enterprise.user.service.impl;

import com.enterprise.arch.auth.LoginUserContext;
import com.enterprise.arch.cache.CacheKeyUtils;
import com.enterprise.arch.cache.QueryCacheOperations;
import com.enterprise.common.error.CommonErrorCode;
import com.enterprise.common.exception.BizException;
import com.enterprise.common.util.PasswordUtils;
import com.enterprise.user.dto.LoginDTO;
import com.enterprise.user.dto.PasswordUpdateDTO;
import com.enterprise.user.dto.RegisterDTO;
import com.enterprise.user.entity.SysUser;
import com.enterprise.user.error.UserErrorCode;
import com.enterprise.user.mapper.SysUserMapper;
import com.enterprise.user.service.UserService;
import com.enterprise.user.strategy.LoginContext;
import com.enterprise.user.vo.LoginVO;
import com.enterprise.user.vo.UserVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {

    private final SysUserMapper userMapper;
    private final LoginContext loginContext;
    private final QueryCacheOperations queryCacheOperations;

    public UserServiceImpl(SysUserMapper userMapper, LoginContext loginContext, QueryCacheOperations queryCacheOperations) {
        this.userMapper = userMapper;
        this.loginContext = loginContext;
        this.queryCacheOperations = queryCacheOperations;
    }

    @Override
    @Transactional
    public UserVO register(RegisterDTO registerDTO) {
        if (userMapper.countByUsername(registerDTO.getUsername()) > 0) {
            throw new BizException(UserErrorCode.USERNAME_EXISTS);
        }
        SysUser user = new SysUser();
        user.setUsername(registerDTO.getUsername());
        user.setPassword(PasswordUtils.encode(registerDTO.getPassword()));
        user.setNickname(registerDTO.getNickname());
        user.setPhone(registerDTO.getPhone());
        user.setEmail(registerDTO.getEmail());
        user.setStatus(1);
        user.setDeleted(0);
        userMapper.insert(user);
        return UserVO.from(user);
    }

    @Override
    public LoginVO login(LoginDTO loginDTO) {
        return loginContext.login(loginDTO);
    }

    @Override
    public UserVO currentUser() {
        Long userId = LoginUserContext.getUserId();
        if (userId == null) {
            throw new BizException(CommonErrorCode.TOKEN_EMPTY);
        }
        return queryCacheOperations.getOrLoad(CacheKeyUtils.currentUser(userId), () -> loadCurrentUser(userId));
    }

    private UserVO loadCurrentUser(Long userId) {
        SysUser user = userMapper.findById(userId);
        if (user == null) {
            throw new BizException(CommonErrorCode.DATA_NOT_FOUND);
        }
        if (Integer.valueOf(0).equals(user.getStatus())) {
            throw new BizException(UserErrorCode.USER_DISABLED);
        }
        return UserVO.from(user);
    }

    @Override
    @Transactional
    public void updatePassword(PasswordUpdateDTO passwordUpdateDTO) {
        Long userId = LoginUserContext.getUserId();
        if (userId == null) {
            throw new BizException(CommonErrorCode.TOKEN_EMPTY);
        }
        SysUser user = userMapper.findById(userId);
        if (user == null) {
            throw new BizException(CommonErrorCode.DATA_NOT_FOUND);
        }
        if (!PasswordUtils.matches(passwordUpdateDTO.getOldPassword(), user.getPassword())) {
            throw new BizException(UserErrorCode.OLD_PASSWORD_ERROR);
        }
        userMapper.updatePassword(userId, PasswordUtils.encode(passwordUpdateDTO.getNewPassword()));
        queryCacheOperations.evictByPattern(CacheKeyUtils.userCachePattern(userId));
    }
}
