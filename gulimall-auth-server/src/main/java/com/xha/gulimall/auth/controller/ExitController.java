package com.xha.gulimall.auth.controller;


import com.xha.gulimall.common.constants.CommonConstants;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpSession;

@Controller
public class ExitController {

    /**
     * 用户退出登录
     *
     * @param session 会话
     * @return {@link String}
     */
    @GetMapping("/exit")
    public String userExit(HttpSession session){
        session.removeAttribute(CommonConstants.LOGIN_USER);
        return "redirect:http://gulimall.com";
    }
}
