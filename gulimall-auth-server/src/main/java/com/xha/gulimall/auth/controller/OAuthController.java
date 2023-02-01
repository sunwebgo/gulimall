package com.xha.gulimall.auth.controller;

import com.xha.gulimall.auth.service.OAuthService;
import lombok.Data;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;


/**
 * 社交登录
 *
 * @author Xu Huaiang
 * @date 2023/01/31
 */
@Data
@Controller
public class OAuthController {

    @Resource
    private OAuthService oAuthService;


    /**
     * gitee oauth
     *
     * @return {@link String}
     */
    @GetMapping("/oauth/gitee")
    public String giteeOAuth(@RequestParam("code") String code, HttpSession session) {
        return oAuthService.giteeOAuth(code,session);
    }
}
