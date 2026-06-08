package com.company.demo.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.demo.annotation.Log;
import com.company.demo.annotation.PassToken;
import com.company.demo.annotation.RateLimit;
import com.company.demo.common.Result;
import com.company.demo.entity.User;
import com.company.demo.service.UserService;
import com.company.demo.utils.SnowflakeIdWorker;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
/**
 * @author jiaolei
 * @date 2026/6/4 15:59
 * @description
 */
@Slf4j
@Tag(name="用户管理",description="用户的增删改查接口")
@RestController
@RequestMapping("/api/user")
public class UserController {
    //添加日志对象
    @Autowired
    private UserService userService;
    @Autowired
    SnowflakeIdWorker snowflakeIdWorker;

    @PassToken
//    @Operation(summary = "查询所有用户")
    @GetMapping("/list")
    public Result<List<User>> list(){
        List<User> allUsers = userService.getAllUsers();
        return Result.success(allUsers);
    }
    @PassToken
//    @Operation(summary = "根据ID查询用户")
    @GetMapping("/{id}")
    public Result<User> getById(@Parameter(description="用户ID") @PathVariable Long id){
        User user = userService.getUserById(id);
        return Result.success(user);
    }
    @RateLimit(key="user:add",permitsPerSecond = 2.0)
    @PassToken
//    @Operation(summary = "新增用户")
    @PostMapping("/add")
    public Result<String> add(@Valid @RequestBody User user){
        userService.addUser(user);
//        userService.addUserWithSnow(user);
        return Result.success("添加成功",null);

    }
    @PassToken
//    @Operation(summary = "修改用户")
    @PutMapping("/update")
    public Result<String> update(@Valid @RequestBody User user){
        if(user.getId()==null){
            return Result.error("用户ID不能为空");
        }
        userService.updateUser(user);
        return Result.success("修改成功",null);

    }
    @PassToken
    @Log("查询用户")
//    @Operation(summary = "删除用户")
    @DeleteMapping("/{id}")
    public Result<String> delete(@Parameter(description = "用户ID") @PathVariable Long id){
        userService.deleteUser(id);
        return Result.success("删除成功",null);

    }
    //分页查询
    @RateLimit(key="user:page",permitsPerSecond=0.1)
    @PassToken
    @Log("分页查询用户")
//    @Operation(summary = "分页查询用户",description="支持按姓名模糊搜索")
    @GetMapping("/page")
    public Result<Page<User>> listUserByPage(
            @Parameter(description = "页码，默认1") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description ="每页条数，默认10")@RequestParam(defaultValue = "10") int pageSize,
            @Parameter(description = "搜索关键词（姓名）")@RequestParam(required = false) String keyword){
        Page<User> page = userService.getUsersPage(pageNum, pageSize, keyword);
        return Result.success(page);
    }
    /*
    * 批量删除用户
    * */

    @PassToken
    @Log("批量删除用户")
    @DeleteMapping("/deleteBatch")
    @Transactional(rollbackFor = Exception.class)
    public Result<String> deleteBatch(Long[] ids){
        userService.deleteBatch(ids);
        return Result.success("批量删除成功",null);
    }
    @PassToken
    @GetMapping("/nextId")
    public Result<Long> nextId() {
        return Result.success(snowflakeIdWorker.nextId());
    }

}
