package com.company.demo;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.demo.common.BusinessException;
import com.company.demo.common.Result;
import com.company.demo.entity.User;
import com.company.demo.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping("/list")
    public Result<List<User>> list(){
        List<User> allUsers = userService.getAllUsers();
        return Result.success(allUsers);
    }
    @GetMapping("/{id}")
    public Result<User> getById(@PathVariable Long id){
        User user = userService.getUserById(id);
        return Result.success();
    }
    @PostMapping("/add")
    public Result<String> add(@Valid @RequestBody User user){
        userService.addUser(user);
        return Result.success("添加成功",null);

    }
    @PutMapping("/update")
    public Result<String> update(@Valid @RequestBody User user){
        if(user.getId()==null){
            return Result.error("用户ID不能为空");
        }
        userService.updateUser(user);
        return Result.success("修改成功",null);

    }
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id){
        userService.deleteUser(id);
        return Result.success("删除成功",null);

    }
    //分页查询
    @GetMapping("/page")
    public Result<Page<User>> listUserByPage(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword){
        Page<User> page = userService.getUsersPage(pageNum, pageSize, keyword);
        return Result.success(page);

    }
}
