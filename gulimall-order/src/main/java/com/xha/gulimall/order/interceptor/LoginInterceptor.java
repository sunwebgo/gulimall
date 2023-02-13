package com.xha.gulimall.order.interceptor;

import cn.hutool.core.text.AntPathMatcher;
import com.xha.gulimall.common.constants.CommonConstants;
import com.xha.gulimall.common.to.member.MemberTO;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Objects;

@Component
public class LoginInterceptor implements HandlerInterceptor {

    public static ThreadLocal<MemberTO> threadLoginUser = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();
//        放行路径
        AntPathMatcher antPathMatcher = new AntPathMatcher();
        boolean getOrder = antPathMatcher.match("/order/order/getOrderById/**", requestURI);
//        boolean payedNotify = antPathMatcher.match("/payed/notify", requestURI);
        if (getOrder){
            return true;
        }

//        1.获取到当前登录的用户
        Object loginUser = request.getSession().getAttribute(CommonConstants.LOGIN_USER);
        if (Objects.isNull(loginUser)) {
//        2.当前没有用户登录，跳转到登录页面
            request.getSession().setAttribute("msg", "请先进行登录");
            response.sendRedirect("http://auth.gulimall.com/login.html");
            return false;
        } else {
            threadLoginUser.set((MemberTO) loginUser);
            return true;
        }
    }
}
