package com.xha.gulimall.thirdserver.controller;

import com.xha.gulimall.common.utils.R;
import com.xha.gulimall.thirdserver.Component.SendMessageComponent;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/thirdserver")
public class SendCaptchaController {

    @Resource
    private SendMessageComponent sendMessageComponent;

    /**
     * 发送验证码
     *
     * @param phone 电话
     * @return {@link R}
     */
    @PostMapping("/sendcaptcha")
    public R sendCaptcha(@RequestParam("phone") String phone, @RequestParam("captcha") String captcha){
        sendMessageComponent.sendMessage(phone,captcha);
        return R.ok();
    }
}
