package com.enterprise.user.controller;

import com.enterprise.common.annotation.Anonymous;
import com.enterprise.common.response.Result;
import com.enterprise.user.dto.LoginDTO;
import com.enterprise.user.dto.PasswordUpdateDTO;
import com.enterprise.user.dto.RegisterDTO;
import com.enterprise.user.service.UserService;
import com.enterprise.user.vo.LoginVO;
import com.enterprise.user.vo.UserVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Anonymous
    @PostMapping("/register")
    public Result<UserVO> register(@Valid @RequestBody RegisterDTO registerDTO) {
        return Result.success(userService.register(registerDTO));
    }

    @Anonymous
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO loginDTO) {
        return Result.success(userService.login(loginDTO));
    }

    @GetMapping("/me")
    public Result<UserVO> currentUser() {
        return Result.success(userService.currentUser());
    }

    @PutMapping("/password")
    public Result<Void> updatePassword(@Valid @RequestBody PasswordUpdateDTO passwordUpdateDTO) {
        userService.updatePassword(passwordUpdateDTO);
        return Result.success();
    }
}
