package com.company.demo.annotation;

import java.lang.annotation.*;

/**
 * 跳过 Token 验证
 * 加在 Controller 方法上，该接口不需要登录即可访问
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PassToken {
}