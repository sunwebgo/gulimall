package com.xha.gulimall.cart.controller;

import com.xha.gulimall.cart.interceptor.CartInterceptor;
import com.xha.gulimall.cart.to.UserInfoTO;
import com.xha.gulimall.common.constants.CommonConstants;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpSession;
import java.util.Objects;

@Controller
public class CartController {

    /**
     * 购物车列表页面
     * 浏览器有一个cookie; user-key; 标识用户身份，一个月后过期;
     * 如果第一次使用jd的购物车功能，都会给一个临时的用户身份;
     * 浏览器以后保存，每次访问都会带上这个cookie;
     * 登录: session有
     * 没登录:按照cookie里面带来user-key来做。
     * 第一次:如果没有临时用户，帮忙创建一个临时用户。
     *
     * @return {@link String}
     */
    @GetMapping("/cart.html")
    public String cartListPage(){
//        1.获取到当前线程中的数据
        UserInfoTO userInfoTO = CartInterceptor.threadLocal.get();

        return "";
    }
}
