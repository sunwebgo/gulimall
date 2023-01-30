package com.xha.gulimall.auth.feign;

import com.xha.gulimall.common.to.UserLoginTO;
import com.xha.gulimall.common.to.UserRegisterTO;
import com.xha.gulimall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Service
@FeignClient("gulimall-member")
public interface MemberFeign {
    /**
     * 用户注册
     *
     * @return {@link R}
     */
    @PostMapping("/member/member/register")
    public R userRegister(@RequestBody UserRegisterTO userRegisterTO);

    /**
     * 用户登录
     *
     * @param userLoginTO 用户登录
     * @return {@link R}
     */
    @PostMapping("/member/member/login")
    public R userLogin(@RequestBody UserLoginTO userLoginTO);
}
