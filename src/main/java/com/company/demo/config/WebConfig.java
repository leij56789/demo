package com.company.demo.config;


import com.company.demo.interceptor.JwtInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
/**
 * @author jiaolei
 * @date 2026/6/5 14:22
 * @description 注册拦截器
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private JwtInterceptor jwtInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/api/**")           // 拦截所有 /api/** 的请求
                .excludePathPatterns(
                        "/api/auth/login",            // 登录接口放行
                        "/api/auth/verify",           // 验证接口放行（可选）
                        "/swagger-ui/**",              // Swagger 放行
                        "/v3/api-docs/**"              // Swagger 文档放行
                );
    }
}