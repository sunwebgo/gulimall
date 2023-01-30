package com.xha.gulimall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xha.gulimall.common.to.UserLoginTO;
import com.xha.gulimall.common.to.UserRegisterTO;
import com.xha.gulimall.common.utils.PageUtils;
import com.xha.gulimall.common.utils.R;
import com.xha.gulimall.member.entity.MemberEntity;
import com.xha.gulimall.member.exception.PhoneExitException;
import com.xha.gulimall.member.exception.UsernameExitException;

import java.util.Map;

/**
 * 会员
 *
 * @author XuHuaianag
 * @email 2533694604@qq.com
 * @date 2022-12-29 16:44:37
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void userRegister(UserRegisterTO userRegisterTO);

    void checkPhoneUnique(String phone) throws PhoneExitException;

    void checkUsernameUnique(String username) throws UsernameExitException;

    R userLogin(UserLoginTO userLoginTO);
}

