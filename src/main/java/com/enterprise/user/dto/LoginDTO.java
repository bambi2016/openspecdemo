package com.enterprise.user.dto;

import com.enterprise.common.annotation.Password;
import jakarta.validation.constraints.NotBlank;

public class LoginDTO {

    private String loginType = "PASSWORD";
    @NotBlank
    private String username;
    @Password
    @NotBlank
    private String password;

    public String getLoginType() {
        return loginType;
    }

    public void setLoginType(String loginType) {
        this.loginType = loginType;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
