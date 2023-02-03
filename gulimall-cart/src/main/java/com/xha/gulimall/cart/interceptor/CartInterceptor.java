package com.xha.gulimall.cart.interceptor;

import cn.hutool.core.lang.UUID;
import cn.hutool.json.JSONUtil;
import com.xha.gulimall.cart.to.UserInfoTO;
import com.xha.gulimall.common.constants.CommonConstants;
import com.xha.gulimall.cart.constants.CookieConstants;
import com.xha.gulimall.common.to.MemberTO;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

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

public class CartInterceptor implements HandlerInterceptor {

    public static ThreadLocal<UserInfoTO> threadLocal = new ThreadLocal<UserInfoTO>();

    /**
     * 前处理
     *
     * @param request  请求
     * @param response 响应
     * @param handler  处理程序
     * @return boolean
     * @throws Exception 异常
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession();
        UserInfoTO userInfoTO = new UserInfoTO();
//        1.从session中获取到当前登录用户
        String userInfoStr = JSONUtil.toJsonStr(session.getAttribute(CommonConstants.LOGIN_USER));
        MemberTO loginUser = JSONUtil.toBean(userInfoStr, MemberTO.class);
        if (!Objects.isNull(loginUser)){
//        2.当前有用户登录
            userInfoTO.setUserId(loginUser.getId());
        }
//        3.当前没有用户登录从cookie中获取到临时用户的user-key
        Cookie[] cookies = request.getCookies();
        if (cookies != null && cookies.length > 0){
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(CookieConstants.TEMPORARY_USER)){
                    userInfoTO.setUserKey(cookie.getValue());
                    userInfoTO.setTempUser(true);
                }
            }
        }

//        无论用户有没有登录都分配一个临时用户
        if (StringUtils.isEmpty(userInfoTO.getUserKey())){
            String uuid = UUID.randomUUID().toString();
            userInfoTO.setUserKey(uuid);
        }

//        4.向当前线程中存放数据
        threadLocal.set(userInfoTO);
//        5.放行所有
        return true;
    }

    /**
     * 处理后，浏览器保存cookie
     *
     * @param request      请求
     * @param response     响应
     * @param handler      处理程序
     * @param modelAndView 模型和视图
     * @throws Exception 异常
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
//        1.判断cookie当中是否有user-key
        UserInfoTO userInfoTO = threadLocal.get();
        if (!userInfoTO.isTempUser()){
//        2.浏览器保存cookie
//          2.1创建cookie
            Cookie cookie = new Cookie(CookieConstants.TEMPORARY_USER, threadLocal.get().getUserKey());
//          2.2设置cookie的作用域
            cookie.setDomain(CookieConstants.COOKIE_DOMAIN);
//          2.3设置cookie的过期时间
            cookie.setMaxAge(CookieConstants.COOKIE_EXPIRE_TIME);
            response.addCookie(cookie);
        }

    }
}
