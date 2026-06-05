package com.company.demo.interceptor;

import com.company.demo.annotation.PassToken;
import com.company.demo.common.Result;
import com.company.demo.utils.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
/**
 * @author jiaolei
 * @date 2026/6/5 14:20
 * @description JWT拦截器
 */
@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 如果不是映射到方法，直接放行
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;

        // 检查方法上是否有 @PassToken 注解
        PassToken passToken = handlerMethod.getMethodAnnotation(PassToken.class);
        if (passToken != null) {
            return true; // 有注解，跳过验证
        }

        // 检查类上是否有 @PassToken 注解
        PassToken classPassToken = handlerMethod.getBeanType().getAnnotation(PassToken.class);
        if (classPassToken != null) {
            return true;
        }

        // 从请求头中获取 token
        String token = request.getHeader("Authorization");

        // token 不存在或格式错误
        if (token == null || !token.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            Result<Object> result = Result.error(401, "未登录或 token 无效");
            response.getWriter().write(objectMapper.writeValueAsString(result));
            return false;
        }

        // 去掉 Bearer 前缀
        token = token.substring(7);

        // 验证 token 是否有效
        if (!jwtUtil.validateToken(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            Result<Object> result = Result.error(401, "token 已过期，请重新登录");
            response.getWriter().write(objectMapper.writeValueAsString(result));
            return false;
        }

        // 验证通过，将用户信息存入 request（可选，方便后续获取）
        String username = jwtUtil.getUsernameFromToken(token);
        request.setAttribute("currentUser", username);

        return true;
    }
}