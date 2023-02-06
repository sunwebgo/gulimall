package com.xha.gulimall.order.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

/**
 * feign远程调用请求拦截器
 *
 * @author Xu Huaiang
 * @date 2023/02/06
 */
@Configuration
public class FeignConfig {

    @Bean
    public RequestInterceptor requestInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
//                1.getRequestAttributes获取到原生请求
                ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                HttpServletRequest request = requestAttributes.getRequest();
                if (!Objects.isNull(request)) {
//                2.同步请求头
//                  2.1在原生请求中获取到cookie
                    String cookie = request.getHeader("Cookie");
//                  2.2在新请求中添加cookie
                    template.header("Cookie", cookie);
                }else{
                    System.out.println("请求为空");
                }
            }
        };
    }
}
