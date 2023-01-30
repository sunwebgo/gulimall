package com.xha.gulimall.auth.feign;

import com.xha.gulimall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Service
@FeignClient("gulimall-thirdserver")
public interface ThirdServerFeign {


    /**
     * 发送验证码
     *
     * @param phone   电话
     * @param captcha 验证码
     * @return {@link R}
     */
    @PostMapping("/thirdserver/sendcaptcha")
    public R sendCaptcha(@RequestParam("phone") String phone, @RequestParam("captcha") String captcha);
}
