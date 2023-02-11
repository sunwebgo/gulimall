package com.xha.gulimall.common.to.member;

import lombok.Data;

@Data
public class UserRegisterTO {
    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 电话
     */
    private String phone;

}
