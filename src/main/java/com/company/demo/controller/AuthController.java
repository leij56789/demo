package com.company.demo.controller;

import com.company.demo.annotation.PassToken;
import com.company.demo.common.Result;
import com.company.demo.entity.LoginRequest;
import com.company.demo.entity.LoginResponse;
import com.company.demo.utils.JwtUtil;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
/**
 * @author jiaolei
 * @date 2026/6/5 14:15
 * @description 
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 登录接口
     */
    @PassToken
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        String username = loginRequest.getUsername();
        String password = loginRequest.getPassword();

        log.info("登录请求: username={}", username);

        // TODO: 实际项目中应该查询数据库验证用户名和密码
        // 这里简单模拟：用户名以 "admin" 开头，密码为 "123456"
        if (!username.startsWith("admin") || !"123456".equals(password)) {
            log.warn("登录失败: username={}", username);
            return Result.error(401, "用户名或密码错误");
        }

        // 生成 Token
        String token = jwtUtil.generateToken(username);
        Long expireTime = jwtUtil.getExpirationDateFromToken(token).getTime();

        LoginResponse response = new LoginResponse(token, username, expireTime);
        log.info("登录成功: username={}", username);

        return Result.success(response);
    }

    /**
     * 验证 Token 是否有效
     */
    @PassToken
    @GetMapping("/verify")
    public Result<Boolean> verifyToken(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Result.error(401, "Token 格式错误");
        }
        String token = authHeader.substring(7);
        boolean isValid = jwtUtil.validateToken(token);
        return Result.success(isValid);
    }
}