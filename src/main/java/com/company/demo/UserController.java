package com.company.demo;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.demo.common.BusinessException;
import com.company.demo.common.Result;
import com.company.demo.entity.User;
import com.company.demo.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
/**
 * @author jiaolei
 * @date 2026/6/4 15:59
 * @description
 */
@Tag(name="用户管理",description="用户的增删改查接口")
@RestController
@RequestMapping("/api/user")
public class UserController {
    @Autowired
    private UserService userService;
    @Operation(summary = "查询所有用户")
    @GetMapping("/list")
    public Result<List<User>> list(){
        List<User> allUsers = userService.getAllUsers();
        return Result.success(allUsers);
    }
    @Operation(summary = "根据ID查询用户")
    @GetMapping("/{id}")
    public Result<User> getById(@Parameter(description="用户ID") @PathVariable Long id){
        User user = userService.getUserById(id);
        return Result.success();
    }
    @Operation(summary = "新增用户")
    @PostMapping("/add")
    public Result<String> add(@Valid @RequestBody User user){
        userService.addUser(user);
        return Result.success("添加成功",null);

    }
    @Operation(summary = "修改用户")
    @PutMapping("/update")
    public Result<String> update(@Valid @RequestBody User user){
        if(user.getId()==null){
            return Result.error("用户ID不能为空");
        }
        userService.updateUser(user);
        return Result.success("修改成功",null);

    }
    @Operation(summary = "删除用户")
    @DeleteMapping("/{id}")
    public Result<String> delete(@Parameter(description = "用户ID") @PathVariable Long id){
        userService.deleteUser(id);
        return Result.success("删除成功",null);

    }
    //分页查询
    @Operation(summary = "分页查询用户",description="支持按姓名模糊搜索")
    @GetMapping("/page")
    public Result<Page<User>> listUserByPage(
            @Parameter(description = "页码，默认1") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description ="每页条数，默认10")@RequestParam(defaultValue = "10") int pageSize,
            @Parameter(description = "搜索关键词（姓名）")@RequestParam(required = false) String keyword){
        Page<User> page = userService.getUsersPage(pageNum, pageSize, keyword);
        return Result.success(page);

    }
}
