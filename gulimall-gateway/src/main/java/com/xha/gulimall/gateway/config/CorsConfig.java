package com.xha.gulimall.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {

    @Bean
    public CorsWebFilter corsWebFilter() {
        UrlBasedCorsConfigurationSource corsConfig = new UrlBasedCorsConfigurationSource();
        CorsConfiguration corsConfiguration = new CorsConfiguration();

        // 1.允许任何跨域请求的域名
        corsConfiguration.addAllowedOrigin("*");
        // 2.允许任何请求方法
        corsConfiguration.addAllowedMethod("*");
        // 3.允许任何请求头
        corsConfiguration.addAllowedHeader("*");
        // 4.允许携带cookie
        corsConfiguration.setAllowCredentials(true);

        corsConfig.registerCorsConfiguration("/**", corsConfiguration);
        return new CorsWebFilter(corsConfig);
    }

}
