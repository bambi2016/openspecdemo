package com.enterprise.user.strategy;

import com.enterprise.common.error.CommonErrorCode;
import com.enterprise.common.exception.BizException;
import com.enterprise.user.dto.LoginDTO;
import com.enterprise.user.vo.LoginVO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LoginContext {

    private final List<LoginStrategy> strategies;

    public LoginContext(List<LoginStrategy> strategies) {
        this.strategies = strategies;
    }

    public LoginVO login(LoginDTO loginDTO) {
        return strategies.stream()
                .filter(strategy -> strategy.supports(loginDTO.getLoginType()))
                .findFirst()
                .orElseThrow(() -> new BizException(CommonErrorCode.PARAM_VALID_ERROR, "不支持的登录类型"))
                .login(loginDTO);
    }
}
