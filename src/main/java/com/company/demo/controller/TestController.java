package com.company.demo.controller;

import com.company.demo.annotation.PassToken;
import com.company.demo.common.Result;
import com.company.demo.entity.User;
import com.company.demo.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

/**
 * @author jiaolei
 * @date 2026-06-07 15:36
 * @description TODO
 */
@Slf4j
@Tag(name="测试",description="测试")
@RestController
@RequestMapping("/api")
public class TestController {
    @Autowired
    UserService userService;

    @PassToken
    @GetMapping("/test/async")
    public Result<String> testAsync() {
        log.info("主线程开始: {}", Thread.currentThread().getName());

        // 异步执行任务
        userService.sendWelcomeMessage(new User());

        log.info("主线程结束（不等待异步任务）");
        return Result.success("异步任务已提交");
    }

    @PassToken
    @GetMapping("/test/batch")
    public Result<List<User>> testBatch(@RequestParam String ids) throws Exception {
        List<Long> userIds = Arrays.stream(ids.split(","))
                .map(Long::parseLong)
                .toList();

        List<User> users = userService.batchGetUsers(userIds);
        return Result.success(users);
    }
}