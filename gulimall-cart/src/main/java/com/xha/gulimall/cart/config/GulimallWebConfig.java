package com.xha.gulimall.cart.config;


import com.xha.gulimall.cart.interceptor.CartInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class GulimallWebConfig implements WebMvcConfigurer {


    /**
     * 添加拦截器
     *
     * @param registry 注册表
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new CartInterceptor()).addPathPatterns("/**");
    }

    /**
     * 添加视图控制器
     *
     * @param registry 注册表
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/cart.html").setViewName("cart");
    }
}
