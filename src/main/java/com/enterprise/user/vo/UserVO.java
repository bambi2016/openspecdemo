package com.enterprise.user.vo;

import com.enterprise.common.util.SensitiveMaskUtils;
import com.enterprise.user.entity.SysUser;

public class UserVO {

    private Long id;
    private String username;
    private String nickname;
    private String phone;
    private String email;

    public static UserVO from(SysUser user) {
        if (user == null) {
            return null;
        }
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setPhone(user.getPhone() == null ? null : SensitiveMaskUtils.maskPhone(user.getPhone()));
        vo.setEmail(user.getEmail() == null ? null : SensitiveMaskUtils.maskEmail(user.getEmail()));
        return vo;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
