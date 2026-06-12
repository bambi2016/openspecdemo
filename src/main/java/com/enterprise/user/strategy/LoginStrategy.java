package com.enterprise.user.strategy;

import com.enterprise.user.dto.LoginDTO;
import com.enterprise.user.vo.LoginVO;

public interface LoginStrategy {

    boolean supports(String loginType);

    LoginVO login(LoginDTO loginDTO);
}
