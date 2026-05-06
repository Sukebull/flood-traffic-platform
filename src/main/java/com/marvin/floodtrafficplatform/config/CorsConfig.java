package com.marvin.floodtrafficplatform.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 给所有以 /api/ 开头的接口（甚至 /** 所有接口）发放跨域通行证
        registry.addMapping("/**")
                // 允许所有来源（你的 Vue 前端 localhost:3002 就能畅通无阻了）
                // 注意：Spring Boot 2.4 以上版本推荐使用 allowedOriginPatterns 代替 allowedOrigins
                .allowedOriginPatterns("*")
                // 允许的前端请求方法
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                // 允许所有的请求头
                .allowedHeaders("*")
                // 允许前端携带凭证（如 Cookie）
                .allowCredentials(true)
                // 预检请求（OPTIONS）的缓存时间，单位是秒，避免每次请求都发预检
                .maxAge(3600);
    }
}