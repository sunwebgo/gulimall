package com.xha.gulimall.cart.interceptor;

import com.xha.gulimall.cart.to.UserInfoTO;
import com.xha.gulimall.common.constants.CommonConstants;
import com.xha.gulimall.common.to.MemberTO;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Objects;

/**
 * 在执行目标方法之前，判断用户的登录状态，
 *
 * @author Xu Huaiang
 * @date 2023/02/01
 */

@Component
public class CartInterceptor implements HandlerInterceptor {

    public static ThreadLocal<UserInfoTO> threadLocal = new ThreadLocal<UserInfoTO>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession();
        UserInfoTO userInfoTO = new UserInfoTO();
//        1.从session中获取到当前登录用户
        MemberTO loginUser = (MemberTO) session.getAttribute(CommonConstants.LOGIN_USER);
        if (!Objects.isNull(loginUser)){
//        2.当前有用户登录
            userInfoTO.setUserId(loginUser.getId());
        }
//        3.当前没有用户登录从cookie中获取到临时用户的user-key
        Cookie[] cookies = request.getCookies();
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(CommonConstants.LOGIN_USER)){
                userInfoTO.setUserKey(cookie.getValue());
            }
        }

//        4.向当前线程中存放数据
        threadLocal.set(userInfoTO);
//        5.放行所有
        return true;
    }
}
