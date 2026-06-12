package com.enterprise.user.service;

import com.enterprise.user.dto.LoginDTO;
import com.enterprise.user.dto.PasswordUpdateDTO;
import com.enterprise.user.dto.RegisterDTO;
import com.enterprise.user.vo.LoginVO;
import com.enterprise.user.vo.UserVO;

public interface UserService {

    UserVO register(RegisterDTO registerDTO);

    LoginVO login(LoginDTO loginDTO);

    UserVO currentUser();

    void updatePassword(PasswordUpdateDTO passwordUpdateDTO);
}
