package com.company.demo.service;

import cn.hutool.core.lang.ClassScanner;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.demo.common.BusinessException;
import com.company.demo.entity.User;
import com.company.demo.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
/**
 * @author jiaolei
 * @date 2026/6/4 18:03
 * @description 
 */
@Service
public class UserService {
    @Autowired
    private UserMapper userMapper;
    public List<User> getAllUsers(){
        return userMapper.selectList(null);
    }
    public User getUserById(Long id){
        User user = userMapper.selectById(id);
        if(user==null){
            throw new BusinessException("用户不存在");
        }
        return user;
    }
    public boolean addUser(User user){
        return userMapper.insert(user)>0;
    }
    public boolean updateUser(User user){
        if(user.getId()==null){
            throw new BusinessException("用户id不能为空");
        }
        if(userMapper.selectById(user.getId())==null){
            throw new BusinessException("用户不存在");
        }
        return userMapper.updateById(user)>0;
    }
    public boolean deleteUser(Long id){
        if(userMapper.selectById(id)==null){
            throw new BusinessException("用户不存在");
        }
        return userMapper.deleteById(id)>0;
    }

    public boolean existUser(Long id) {
        return userMapper.selectById(id)!=null;
    }
    //分页查询用户
    public Page<User> getUsersPage(int pageNum,int pageSize,String keyword){
        Page<User> page = new Page<>(pageNum, pageSize);
        //条件构造器
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        //name like '%keyword%'
        wrapper.like(StrUtil.isNotBlank(keyword),User::getName,keyword);
        wrapper.orderByDesc(User::getId);
        return userMapper.selectPage(page,wrapper);
    }
}
